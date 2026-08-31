package com.carteira.carteiraInvestimento.presentation.error;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new FailureProbeController())
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
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
	}
}
