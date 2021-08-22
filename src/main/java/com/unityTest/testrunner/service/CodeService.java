package com.unityTest.testrunner.service;

import com.unityTest.testrunner.constants.DockerConstants;
import com.unityTest.testrunner.entity.*;
import com.unityTest.testrunner.exception.code.DockerImageBuildFailureException;
import com.unityTest.testrunner.exception.code.DockerTimeoutException;
import com.unityTest.testrunner.exception.code.InvalidFunctionNameException;
import com.unityTest.testrunner.exception.code.UnsupportedProgrammingLanguageException;
import com.unityTest.testrunner.models.PLanguage;
import com.unityTest.testrunner.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class CodeService {

	@Autowired
	private DockerService dockerService;

	@Value("${runner.dir}")
	private String workingDirectoryPath;

	@Value("${runner.env}")
	private String envFile;

	@Value("${docker.build.haskell.suite-template}")
	private String haskellTestFilePath;

	/**
	 * Asynchronously run a list of test cases in an isolated environment and send back the results
	 *
	 * @param emitter Emitter to send back results or errors
	 * @param submission Code submission containing source files
	 * @param suite Suite test cases belong to
	 * @param suiteFile Suite file to be used as a base when constructing test case code
	 * @param testCases List of test cases to run against submission source code
	 */
	@Async("threadPoolTaskExecutor")
	public void asyncRunTestCasesInSuite(
			ResponseBodyEmitter emitter,
			Submission submission,
			Suite suite,
			SuiteFile suiteFile,
			List<Case> testCases) {
		// Create directory name dir_${suiteId}_${suiteFileId}_${submissionId}_${timestamp}
		final String NEW_DIR_NAME = String
			.format(
				"dir_%d_%d_%d_%s", suite.getId(), suiteFile.getId(), submission.getId(),
				new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date()));
		final String WORK_DIR = Utils.buildPath(this.workingDirectoryPath, NEW_DIR_NAME);

		String imageName, dockerfilePath, entryFilePath, entryFileName;
		switch (suite.getLanguage()) {
			case PYTHON3:
				imageName = DockerConstants.PYTHON_DOCKER_IMAGE_NAME;
				dockerfilePath = dockerService.getPythonDockerFilePath();
				entryFilePath = dockerService.getPythonEntryFilePath();
				entryFileName = DockerConstants.PYTHON_ENTRY_FILE;
				break;
			case HASKELL:
				imageName = DockerConstants.HASKELL_DOCKER_IMAGE_NAME;
				dockerfilePath = dockerService.getHaskellDockerFilePath();
				entryFilePath = dockerService.getHaskellEntryFilePath();
				entryFileName = DockerConstants.HASKELL_ENTRY_FILE;
				break;
			case JAVA:
				throw new UnsupportedProgrammingLanguageException(PLanguage.JAVA);
			default:
				throw new UnsupportedProgrammingLanguageException(suite.getLanguage());
		}
		try {
			// Create the new working directory
			log.info(String.format("Creating new directory and writing source files to %s", WORK_DIR));
			new File(WORK_DIR).mkdirs();
			// Write all files from submission into directory
			for (SourceFile sourceFile : submission.getSourceFiles()) {
				FileUtils
					.writeByteArrayToFile(
						new File(Utils.buildPath(WORK_DIR, sourceFile.getFileName())), sourceFile.getContent());
			}
			// Copy Dockerfile and entry point into working directory
			FileUtils
				.copyFile(
					new File(getClass().getResource(dockerfilePath).getFile()),
					new File(Utils.buildPath(WORK_DIR, DockerConstants.DOCKERFILE)));
			FileUtils
				.copyFile(
					new File(getClass().getResource(entryFilePath).getFile()),
					new File(Utils.buildPath(WORK_DIR, entryFileName)));
			log.debug(String.format("Copying Dockerfile and entry point file %s into %s", entryFileName, WORK_DIR));

			// Build docker image from source files in directory
			String imageAndTag =
				dockerService.buildDockerImage(imageName, String.valueOf(submission.getId()), WORK_DIR);

			// Write suite file into directory
			File tests = new File(Utils.buildPath(WORK_DIR, suiteFile.getFileName()));

			// Continually write test case to suite file and run in docker container
			// Send results back to emitter
			for (Case testcase : testCases) {
				writeTestCaseToSuiteFile(suite.getLanguage(), tests, suiteFile, testcase);
				// Run test cases in docker container
				emitter
					.send(
						dockerService
							.runTestCaseInDockerContainer(
								testcase, imageAndTag,
								new File(getClass().getResource(envFile).getFile()).getAbsolutePath(), WORK_DIR,
								suiteFile.getFileName()),
						MediaType.APPLICATION_JSON);
			}
			emitter.complete();
		} catch (IOException e) {
			e.printStackTrace();
			emitter.completeWithError(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
			emitter.completeWithError(e);
		} catch (DockerImageBuildFailureException e) {
			e.printStackTrace();
			emitter.completeWithError(e);
		} catch (DockerTimeoutException e) {
			e.printStackTrace();
			emitter.completeWithError(e);
		} catch (Exception e) {
			log.error("Unexpected exception");
			emitter.completeWithError(e);
		} finally {
			// Clean up
			try {
				// Delete the directory after use
				FileUtils.deleteDirectory(new File(WORK_DIR));
			} catch (IOException e) {
				String warnMsg = "Could not find directory to delete %s with exception %s";
				log.warn(String.format(warnMsg, WORK_DIR, e.getLocalizedMessage()));
			}
		}
	}

	public String buildTestCaseCode(PLanguage lang, String functionName, String functionBody) {
		// Check for invalid characters in the function name
		if (!functionName.matches("[a-zA-Z][0-9a-zA-Z_]+"))
			throw new InvalidFunctionNameException(
					functionName, "Function identifier does not match regex [a-zA-Z][0-9a-zA-Z_]+");

		switch (lang) {
			case JAVA:
				// TODO Implement java code builder
				break;
			case HASKELL:
				return buildHUnitTestFunction(functionName, functionBody);
			case PYTHON3:
				return buildPythonTestFunction(functionName, functionBody);
			default:
				throw new IllegalArgumentException("Programming language not supported");
		}
		return null;
	}

	private void writeTestCaseToSuiteFile(PLanguage lang, File testFile, SuiteFile suiteFile, Case caze)
			throws IOException {
		// Open a buffered writer to write the suite file contents with the test case
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(testFile, false));
		// Write suite file content
		bos.write(suiteFile.getContent());
		// Write test case content to file depending on language
		switch (lang) {
			case JAVA:
				// TODO;
				break;
			case HASKELL:
				// Get suite template file
				File file = new File(getClass().getResource(haskellTestFilePath).getFile());
				// try to read suite template file
				Stream<String> fileStream;
				try {
					fileStream = Files.lines(file.toPath(), StandardCharsets.UTF_8);
				} catch (IOException ex) {
					bos.close();
					throw new IOException(ex);
				}
				// Load content as a string
				String testFileString = fileStream.collect(Collectors.joining(System.lineSeparator()));
				// Format string with function name and actual test function
				testFileString = String.format(testFileString, caze.getFunctionName(), caze.getCode());

				// Write to provided suite file
				bos.write(testFileString.getBytes());
				break;
			case PYTHON3:
				bos.write(String.format("\n%s", Utils.indent(caze.getCode(), 1)).getBytes());
				break;
			default:
				bos.close();
				throw new UnsupportedProgrammingLanguageException(lang);
		}
		bos.flush();
		bos.close();
	}

	private String buildPythonTestFunction(String funcName, String body) {
		// Define definition
		final String def = String.format("def test_%s(self):\n", funcName);
		// Create code block by indenting the body by one tab
		return def.concat(Utils.indent(body, 1).concat("\n"));
	}

	private String buildHUnitTestFunction(String funcName, String body) {
		StringBuilder functionBuilder = new StringBuilder();
		functionBuilder.append(String.format("test_%s :: Test\n", funcName));
		functionBuilder.append(String.format("test_%s = %s", funcName, body));
		return functionBuilder.toString();
	}
}
