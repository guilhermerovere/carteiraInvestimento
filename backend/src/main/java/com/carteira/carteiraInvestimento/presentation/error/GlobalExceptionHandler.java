package com.carteira.carteiraInvestimento.presentation.error;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	ProblemDetail handleInvalidArgument(IllegalArgumentException exception, HttpServletRequest request) {
		return problem(HttpStatus.BAD_REQUEST, "Invalid request", "The request is invalid.", request);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	ProblemDetail handleNotFound(NoResourceFoundException exception, HttpServletRequest request) {
		return problem(HttpStatus.NOT_FOUND, "Resource not found", "The requested resource was not found.", request);
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpected(Exception exception, HttpServletRequest request) {
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
				"An unexpected error occurred.", request);
	}

	private ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}
}

