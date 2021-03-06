package com.unityTest.testrunner.restImpl;

import com.unityTest.testrunner.entity.Case;
import com.unityTest.testrunner.entity.PLanguage;
import com.unityTest.testrunner.models.api.request.TestCaseInfo;
import com.unityTest.testrunner.models.api.response.page.TestCasePage;
import com.unityTest.testrunner.models.api.response.Author;
import com.unityTest.testrunner.models.api.response.TestCase;
import com.unityTest.testrunner.restApi.CaseApi;
import com.unityTest.testrunner.service.CaseService;
import com.unityTest.testrunner.service.KeycloakService;
import com.unityTest.testrunner.utils.Utils;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

/**
 * Rest controller for /case/* endpoints
 */
@RestController
public class CaseController implements CaseApi {

	@Autowired
	private CaseService caseService;

	@Autowired
	private KeycloakService keycloakService;

	@Override
	public ResponseEntity<TestCase> createTestCase(Principal principal, @Valid TestCaseInfo testCaseInfo) {
		AccessToken accessToken = Utils.getAuthToken(principal); // Extract access token in request
		// Construct author from access token
		Author author = new Author(accessToken.getGivenName(), accessToken.getFamilyName());

		// Save case to repo and return case as TestCase response object
		return new ResponseEntity<>(
				caseService.createCase(testCaseInfo, accessToken.getSubject()).toTestCase(author), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<TestCasePage> getTestCases(
			Pageable pageable,
			Integer id,
			Integer suiteId,
			Integer assignmentId,
			String functionName,
			String lang) {
		// Convert lang to PLanguage
		PLanguage pLanguage = Utils.parsePLanguage(lang);
		// Retrieve results using service
		Page<Case> casePage = caseService.getCases(pageable, id, suiteId, assignmentId, functionName, pLanguage, null);
		TestCasePage page = new TestCasePage(casePage, keycloakService.getConnection(), keycloakService.getRealmName());
		return new ResponseEntity<>(page, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<TestCase> updateTestCase(
			Principal principal,
			Integer caseId,
			@Valid TestCaseInfo testCaseInfo) {
		AccessToken token = Utils.getAuthToken(principal); // Get request token
		Case caseToUpdate = caseService.getCaseById(caseId); // Find case to update
		Author author = new Author(token.getGivenName(), token.getFamilyName()); // Construct author from access token

		// Check if user is owner or admin
		if (!Utils.isAuthorOrAdmin(token, caseToUpdate.getAuthorId()))
			throw new AccessDeniedException("Access Denied");
		// Update case and return updated case
		return new ResponseEntity<>(caseService.updateCase(caseId, testCaseInfo).toTestCase(author), HttpStatus.OK);
	}

	@Override
	public void deleteTestCase(Principal principal, Integer caseId) {
		AccessToken token = Utils.getAuthToken(principal); // Get request token
		Case caseToDelete = caseService.getCaseById(caseId); // Find case to delete

		// Check if user deleting is not the author or admin
		if (!Utils.isAuthorOrAdmin(token, caseToDelete.getAuthorId()))
			throw new AccessDeniedException("Access Denied");
		// If allowed, delete the test case
		caseService.deleteCase(caseId);
	}

	@Override
	@RolesAllowed("ROLE_SYS")
	public void updateVoteScoreOnTestCase(Integer caseId, Integer count) {
		caseService.updateCaseUpvotes(caseId, count);
	}

	@Override
	@RolesAllowed("ROLE_SYS")
	public void incrementCommentCountOnTestCase(Integer caseId) {
		caseService.updateCaseCommentCountBy(caseId, 1);
	}
}
