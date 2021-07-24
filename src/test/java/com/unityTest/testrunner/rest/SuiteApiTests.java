package com.unityTest.testrunner.rest;

import com.unityTest.testrunner.entity.Suite;
import com.unityTest.testrunner.models.PLanguage;
import com.unityTest.testrunner.repository.SuiteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import javax.servlet.Filter;
import static com.unityTest.testrunner.TestUtils.post;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Implements component API tests for Suite API endpoints
 */
@AutoConfigureMockMvc()
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class SuiteApiTests {

	@Autowired
	private Filter springSecurityFilterChain;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private SuiteRepository suiteRepository;

	private final String baseUri = "/suite";

	@AfterEach
	void cleanupDB() {
		suiteRepository.deleteAll();
	}

	// TODO Figure out mocking authentication tokens
	// @BeforeEach
	// void setup() {
	// mockMvc = MockMvcBuilders
	// .webAppContextSetup(context)
	// .defaultRequest(get("/").secure(true).with(testSecurityContext()))
	// .addFilters(springSecurityFilterChain)
	// .apply(springSecurity())
	// .build();
	// }
	// @Test
	// void createSuite_Authenticated() throws Exception {
	// final var principal = mock(Principal.class);
	// when(principal.getName()).thenReturn("sherman");
	//
	// final var account = mock(OidcKeycloakAccount.class);
	//
	// when(account.getRoles()).thenReturn(new HashSet<>(Arrays.asList("USER", "ADMIN", "ROLE_USER",
	// "user", "offline_access", "uma_authorization")));
	// when(account.getPrincipal()).thenReturn(principal);
	//
	// final var authentication = mock(KeycloakAuthenticationToken.class);
	// when(authentication.getAccount()).thenReturn(account);
	//
	// SecurityContextHolder.getContext().setAuthentication(authentication);
	//
	// final Suite suiteToCreate = new Suite(0, 100, "CREATE_SUITE", PLanguage.JAVA, 12, null);
	//
	// // Perform POST request
	// MvcResult result = mockMvc
	// .perform(post(this.baseUri, suiteToCreate))
	// .andExpect(status().isCreated())
	// .andReturn();
	// System.out.println(result.getResponse().getContentAsString());
	// }

	@Test
	void createSuite_ValidArg_SaveSuiteToRepo() throws Exception {
		// TODO MOVE THIS INTO ANNOTATION WITH PROPER AUTH
		// Mock the principal
		KeycloakAuthenticationToken authentication = mock(KeycloakAuthenticationToken.class, RETURNS_DEEP_STUBS);
		AccessToken accessToken = mock(AccessToken.class);
		when(authentication.getAccount().getKeycloakSecurityContext().getToken()).thenReturn(accessToken);
		when(accessToken.getSubject()).thenReturn("TEST_USERNAME");

		final Suite suiteToCreate = new Suite(0, 100, "CREATE_SUITE", PLanguage.JAVA, 12, null);

		// Perform POST request
		MvcResult result = mockMvc
			.perform(post(this.baseUri, suiteToCreate).principal(authentication))
			.andExpect(status().isCreated())
			.andReturn();
		System.out.println(result.getResponse().getContentAsString());
	}
}
