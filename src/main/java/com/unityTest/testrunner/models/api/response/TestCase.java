package com.unityTest.testrunner.models.api.response;

import com.unityTest.testrunner.entity.Suite;
import com.unityTest.testrunner.entity.PLanguage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Models a test case returned in response to a request
 */
@AllArgsConstructor
@ApiModel(value = "TestCase", description = "Models a test case in a test suite")
@Data
public class TestCase {

	@ApiModelProperty(value = "Id", required = true)
	private int id;

	// Test suite that the test case belongs to
	@ApiModelProperty(value = "Test suite", required = true)
	private Suite suite;

	// Description of test case
	@ApiModelProperty(value = "Test case description", example = "Tests boundary case if x = 0")
	private String description;

	// Programming language of test case
	@ApiModelProperty(value = "Language", required = true, example = "JAVA")
	private PLanguage language;

	// Author information of test case
	@ApiModelProperty(value = "Author name", required = true)
	private Author author;

	// Upvote score for the test case
	@ApiModelProperty(value = "Upvote score", required = true)
	private int upvotes;

	@ApiModelProperty(value = "Comment count", required = true)
	private int comments;

	// Statistics for the test case, run count, pass count etc.
	@ApiModelProperty(value = "Test case statistics", required = true)
	private CaseStats stats;

	// Code snippet that runs with the test case
	@ApiModelProperty(value = "Test case code", required = true)
	private String code;
}
