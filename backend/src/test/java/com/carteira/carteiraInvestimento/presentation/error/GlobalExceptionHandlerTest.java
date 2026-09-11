package com.carteira.carteiraInvestimento.presentation.error;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carteira.carteiraInvestimento.application.service.DuplicateEmailException;
import com.carteira.carteiraInvestimento.application.service.InvestmentConflictException;
import com.carteira.carteiraInvestimento.application.service.InvestmentNotFoundException;
import com.carteira.carteiraInvestimento.infrastructure.security.AccessDeniedAuditingService;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new FailureProbeController())
				.setControllerAdvice(new GlobalExceptionHandler(new ProblemDetailFactory(),
						new AccessDeniedAuditingService(event -> { throw new IllegalStateException("database PASSWORD_SENTINEL"); })))
				.build();
	}

	@Test
	void returnsSanitizedConflictForDuplicateEmail() throws Exception {
		mockMvc.perform(get("/test/duplicate-email"))
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.detail").value("An account with this email already exists."))
				.andExpect(content().string(not(containsString("existing-email@example.test"))));
	}

	@Test
	void returnsSanitizedProblemDetailForInvalidArguments() throws Exception {
		mockMvc.perform(get("/test/invalid"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value("The request is invalid."))
				.andExpect(content().string(not(containsString("super-secret"))))
				.andExpect(content().string(not(containsString("GlobalExceptionHandlerTest"))));
	}

	@Test
	void returnsSanitizedProblemDetailForUnexpectedFailures() throws Exception {
		mockMvc.perform(get("/test/unexpected"))
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(500))
				.andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
				.andExpect(content().string(not(containsString("database-password"))));
	}

	@Test
	void returnsSanitizedInvestmentConflictAndNotFound() throws Exception {
		mockMvc.perform(get("/test/investment-conflict"))
				.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(content().string(not(containsString("idempotency-secret"))));
		mockMvc.perform(get("/test/investment-not-found"))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void preservesForbiddenProblemDetailWhenAccessDeniedAuditFails() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(
				new TestingAuthenticationToken(UUID.randomUUID().toString(), null, "ROLE_USER"));
		mockMvc.perform(get("/test/access-denied")
				.requestAttr(CorrelationIdFilter.ATTRIBUTE, UUID.randomUUID()))
				.andExpect(status().isForbidden())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.detail").value("Access is denied."))
				.andExpect(content().string(not(containsString("PASSWORD_SENTINEL"))));
	}

	@RestController
	private static class FailureProbeController {

		@GetMapping("/test/invalid")
		void invalid() {
			throw new IllegalArgumentException("super-secret");
		}

		@GetMapping("/test/unexpected")
		void unexpected() {
			throw new IllegalStateException("database-password");
		}

		@GetMapping("/test/duplicate-email")
		void duplicateEmail() {
			throw new DuplicateEmailException();
		}

		@GetMapping("/test/access-denied")
		void accessDenied() {
			throw new AccessDeniedException("secret");
		}

		@GetMapping("/test/investment-conflict")
		void investmentConflict() { throw new InvestmentConflictException("idempotency-secret"); }

		@GetMapping("/test/investment-not-found")
		void investmentNotFound() { throw new InvestmentNotFoundException(); }
	}
}
