package com.unityTest.testrunner.exception.code;

public class InvalidFunctionNameException extends RuntimeException {
    public InvalidFunctionNameException(String functionName, String reason) {
        super(String.format("Invalid function name %s. %s", functionName, reason));
    }
}
