package com.unityTest.testrunner.exception.code;

public class DockerImageBuildFailureException extends RuntimeException {
    public DockerImageBuildFailureException(String image, String tag, int exitCode) {
        super(String.format("Failed to build docker image %s:%s. Build finished with error code %d.", image, tag, exitCode));
    }

    public DockerImageBuildFailureException(String image, String tag, int exitCode, String output) {
        super(String.format("Failed to build docker image %s:%s. Build finished with error code %d and output: %s", image, tag, exitCode, output));
    }
}
