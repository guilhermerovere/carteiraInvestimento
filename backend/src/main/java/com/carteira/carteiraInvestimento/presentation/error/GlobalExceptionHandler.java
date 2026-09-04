package com.carteira.carteiraInvestimento.presentation.error;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import com.carteira.carteiraInvestimento.application.service.AuthenticationFailedException;
import com.carteira.carteiraInvestimento.application.service.DuplicateEmailException;
import com.carteira.carteiraInvestimento.infrastructure.security.AccessDeniedAuditingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private final ProblemDetailFactory problems;
	private final AccessDeniedAuditingService accessDeniedAuditing;

	@Autowired
	public GlobalExceptionHandler(ProblemDetailFactory problems, AccessDeniedAuditingService accessDeniedAuditing) {
		this.problems = problems;
		this.accessDeniedAuditing = accessDeniedAuditing;
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ProblemDetail handleInvalidArgument(IllegalArgumentException exception, HttpServletRequest request) {
		return problem(HttpStatus.BAD_REQUEST, "Invalid request", "The request is invalid.", request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		return problem(HttpStatus.BAD_REQUEST, "Invalid request", "The request is invalid.", request);
	}

	@ExceptionHandler(DuplicateEmailException.class)
	ProblemDetail handleDuplicateEmail(DuplicateEmailException exception, HttpServletRequest request) {
		return problem(HttpStatus.CONFLICT, "Email already registered", "An account with this email already exists.", request);
	}

	@ExceptionHandler(AuthenticationFailedException.class)
	ProblemDetail handleAuthenticationFailure(AuthenticationFailedException exception, HttpServletRequest request) {
		return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required.", request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
		accessDeniedAuditing.record(request);
		return problem(HttpStatus.FORBIDDEN, "Forbidden", "Access is denied.", request);
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
		return problems.create(status, title, detail, request);
	}
}

