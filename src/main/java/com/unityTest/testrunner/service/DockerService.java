package com.unityTest.testrunner.service;

import com.unityTest.testrunner.constants.ExitCodes;
import com.unityTest.testrunner.entity.Case;
import com.unityTest.testrunner.exception.code.DockerImageBuildFailureException;
import com.unityTest.testrunner.exception.code.DockerTimeoutException;
import com.unityTest.testrunner.models.ResultStatus;
import com.unityTest.testrunner.models.response.TestResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Service for build and running docker images Used for running test cases in an isolated
 * environment
 */
@Slf4j
@Service
public class DockerService {

	@Value("${docker.build.timeout}")
	private int dockerBuildTimeout;

	@Value("${runner.timeouts.entry-script}")
	private int entryScriptTimeout;

	@Value("${runner.timeouts.docker-container}")
	private int dockerContainerTimeout;

	@Value("${runner.container.memory}")
	private int dockerContainerMemory;

	@Value("${docker.build.dir}")
	private String dockerWorkingDir;

	@Getter
	@Value("${docker.build.python3.dockerfile}")
	private String pythonDockerFilePath;

	@Getter
	@Value("${docker.build.python3.entry}")
	private String pythonEntryFilePath;

	/**
	 * Build a docker image with the name image:tag from the working directory
	 * 
	 * @param image Image name
	 * @param tag Image tag
	 * @param workingDir Working directory from which to run the `docker build` command
	 * @return ${image}:${tag} of the built image
	 * @throws IOException if any of the files, paths or resources cannot be located or are blocked
	 * @throws InterruptedException if the process is prematurely terminated or interrupted
	 * @throws DockerTimeoutException if the docker build command takes longer than the allowed timeout
	 * @throws DockerImageBuildFailureException if the docker build fails with a non-zero exit code
	 */
	public String buildDockerImage(String image, String tag, String workingDir)
			throws IOException, InterruptedException, DockerTimeoutException, DockerImageBuildFailureException {
		// Build command to run
		String[] cmd = new String[] {
			"docker",
			"build",
			"--build-arg",
			String.format("DIR=%s", this.dockerWorkingDir), // Set working directory
			"-t",
			String.format("%s:%s", image, tag), // Set image tag
			"."}; // Run in current dir

		log.info(String.format("Building docker image %s:%s with command: %s", image, tag, String.join(" ", cmd)));
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(new File(workingDir)); // Set working directory
		pb.redirectErrorStream(true); // Combine stderr with stdout

		StopWatch sw = new StopWatch();
		sw.start();
		Process p = pb.start(); // Start the process
		InputStream stream = p.getInputStream(); // Capture the process output
		if (!p.waitFor(this.dockerBuildTimeout, TimeUnit.SECONDS)) {
			p.destroyForcibly(); // kill the process
			stream.close();
			sw.stop();
			throw new DockerTimeoutException(image, tag, this.dockerBuildTimeout);
		} else {
			sw.stop();
			if (p.exitValue() != 0) { // If the process failed with a non-zero exit code
				String output = IOUtils.toString(stream, StandardCharsets.UTF_8);
				stream.close();
				throw new DockerImageBuildFailureException(image, tag, p.exitValue(), output);
			}
			String successMsg = "Successfully built docker image %s:%s in %.3f seconds with exit code 0";
			log.info(String.format(successMsg, image, tag, (float) sw.getTime(TimeUnit.MILLISECONDS) / 1000));
			// Return name of image built
			return String.format("%s:%s", image, tag);
		}
	}

	/**
	 * Run a test case in an isolated docker container
	 * 
	 * @param caze Test case to run
	 * @param image Image from which to build the docker container
	 * @param envPath Resource path to environment file containing env values to pass to container
	 * @param workingDir Working directory from which to run the `docker run` command
	 * @param suiteFileName Name of suite file containing the test case code
	 * @return Result of running the test code contained in the suite file
	 */
	public TestResult runTestCaseInDockerContainer(
			Case caze,
			String image,
			String envPath,
			String workingDir,
			String suiteFileName) {
		final String CONTAINER_NAME = String
			.format(
				"%d_%s_%s", caze.getId(), image.replace(':', '-'),
				new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date()));

		String[] cmd = new String[] {
			"docker",
			"run",
			"--rm",
			"-m",
			String.format("%dM", dockerContainerMemory), // Set container memory
			"--name",
			CONTAINER_NAME, // Set container name
			"--env-file",
			envPath, // Add environment variables
			"-e",
			String.format("TEST_FILE_NAME=%s", suiteFileName),
			"-e",
			String.format("PF_TIMEOUT=%s", this.entryScriptTimeout),
			"-v",
			String.format("%s/%s:%s/%s", workingDir, suiteFileName, this.dockerWorkingDir, suiteFileName), // Set volume
			image};

		String infoMsg = "Running docker image %s with name %s with command: %s";
		log.info(String.format(infoMsg, image, CONTAINER_NAME, String.join(" ", cmd)));

		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(new File(workingDir)); // Set working directory
		pb.redirectErrorStream(true); // Combine stderr with stdout

		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();

			Process p = pb.start(); // Start the process
			InputStream stream = p.getInputStream(); // Capture the process output
			if (!p.waitFor(this.dockerContainerTimeout, TimeUnit.SECONDS)) {
				stream.close(); // close the stream
				stopWatch.stop();
				p.destroyForcibly(); // kill the process
				infoMsg = "Process running docker container for case %d timed out after %d seconds. "
						+ "Terminating container with name %s";
				log.info(String.format(infoMsg, caze.getId(), this.dockerContainerTimeout, CONTAINER_NAME));
				stopDockerContainer(CONTAINER_NAME, 0);
				return new TestResult(caze.getId(), ResultStatus.TIMEOUT_ERROR, "");
			} else {
				String output = IOUtils.toString(stream, StandardCharsets.UTF_8);
				stream.close();
				stopWatch.stop();
				infoMsg = "Finished running test case %d in %.3f seconds";
				log.info(String.format(infoMsg, caze.getId(), (float) stopWatch.getTime(TimeUnit.MILLISECONDS) / 1000));

				switch (p.exitValue()) {
					case ExitCodes.SUCCESS:
						return new TestResult(caze.getId(), ResultStatus.PASS, output);
					case ExitCodes.ERROR:
						return new TestResult(caze.getId(), ResultStatus.FAIL, output);
					case ExitCodes.RUNTIME_ERROR:
						return new TestResult(caze.getId(), ResultStatus.RUNTIME_ERROR, output);
					case ExitCodes.DOCKER_RUN_FAIL:
					case ExitCodes.SEG_FAULT:
						return new TestResult(caze.getId(), ResultStatus.OUT_OF_MEMORY_ERROR, output);
					case ExitCodes.SIGTERM:
					case ExitCodes.SIGKILL:
						return new TestResult(caze.getId(), ResultStatus.TIMEOUT_ERROR, output);
					case ExitCodes.SIGXFSZ:
					case ExitCodes.ILLEGAL:
						return new TestResult(caze.getId(), ResultStatus.CONSTRAINT_VIOLATION_ERROR, output);
					default:
						log.warn(String.format("Unexpected exit code %d in docker container run", p.exitValue()));
						return new TestResult(caze.getId(), ResultStatus.UNEXPECTED_ERROR, output);
				}
			}
		} catch (InterruptedException | IOException e) {
			String warnMsg = "Unexpected IO error when running test case %d with error %s";
			log.warn(String.format(warnMsg, caze.getId(), e.getMessage()));
			return new TestResult(caze.getId(), ResultStatus.IO_ERROR, e.getMessage() + e.getCause());
		}
	}

	public void stopDockerContainer(String name, int timeout) {
		String[] cmd = {"docker", "stop", "-t", String.valueOf(timeout), name};
		String infoMsg = "Stopping docker container %s after a timeout of %d seconds with command: %s";
		log.info(String.format(infoMsg, name, timeout, String.join(" ", cmd)));

		ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true);
		try {
			Process p = pb.start();
			InputStream stream = p.getInputStream(); // Capture the process output
			// Wait for process to finish
			p.waitFor();
			if (p.exitValue() != 0) {
				String output = IOUtils.toString(stream, StandardCharsets.UTF_8);
				String warnMsg = "Failed to stop docker container %s. Command exited with value %d and output\n%s";
				log.warn(String.format(warnMsg, name, p.exitValue(), output));
			} else {
				log.info(String.format("Successfully stopped docker container %s", name));
			}
			stream.close();
		} catch (IOException | InterruptedException e) {
			String warnMsg = "Failed to stop docker container %s. Threw exception %s";
			log.warn(String.format(warnMsg, name, e.getLocalizedMessage()));
			e.printStackTrace();
		}
	}
}
