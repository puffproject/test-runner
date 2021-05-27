package com.unityTest.testrunner.exception.code;

public class DockerTimeoutException extends RuntimeException {
    public DockerTimeoutException(String image, String tag, int timeout) {
        super(String.format("Docker build timeout for image %s:%s did not complete before %d seconds", image, tag, timeout));
    }

    public DockerTimeoutException(String image, String tag, int timeout, String output) {
        super(String.format("Docker build timeout for image %s:%s did not complete before %d seconds with output: %s", image, tag, timeout, output));
    }
}
