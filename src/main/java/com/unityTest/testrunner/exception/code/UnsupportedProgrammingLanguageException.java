package com.unityTest.testrunner.exception.code;

import com.unityTest.testrunner.models.PLanguage;

public class UnsupportedProgrammingLanguageException extends RuntimeException {
	public UnsupportedProgrammingLanguageException(PLanguage pLanguage) {
		super(String.format("programming language %s is not supported."));
	}
}
