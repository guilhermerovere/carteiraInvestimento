package com.carteira.carteiraInvestimento.presentation.error;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import com.carteira.carteiraInvestimento.application.service.AuthenticationFailedException;
import com.carteira.carteiraInvestimento.application.service.DuplicateEmailException;
import com.carteira.carteiraInvestimento.application.service.DuplicateTickerException;
import com.carteira.carteiraInvestimento.application.service.AtivoNotFoundException;
import com.carteira.carteiraInvestimento.application.service.InactiveAssetRefreshException;
import com.carteira.carteiraInvestimento.application.service.QuoteIntegrationException;
import com.carteira.carteiraInvestimento.application.service.QuoteNotFoundException;
import com.carteira.carteiraInvestimento.application.service.CashConflictException;
import com.carteira.carteiraInvestimento.application.service.PrimaryWalletMissingException;
import com.carteira.carteiraInvestimento.infrastructure.security.AccessDeniedAuditingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

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

	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
			HandlerMethodValidationException.class})
	ProblemDetail handleMalformedRequest(Exception exception, HttpServletRequest request) {
		return problem(HttpStatus.BAD_REQUEST, "Invalid request", "The request is invalid.", request);
	}

	@ExceptionHandler(DuplicateEmailException.class)
	ProblemDetail handleDuplicateEmail(DuplicateEmailException exception, HttpServletRequest request) {
		return problem(HttpStatus.CONFLICT, "Email already registered", "An account with this email already exists.", request);
	}

	@ExceptionHandler(DuplicateTickerException.class)
	ProblemDetail handleDuplicateTicker(DuplicateTickerException exception, HttpServletRequest request) {
		return problem(HttpStatus.CONFLICT, "Ticker already registered",
				"An asset with this ticker already exists.", request);
	}

	@ExceptionHandler(AtivoNotFoundException.class)
	ProblemDetail handleAtivoNotFound(AtivoNotFoundException exception, HttpServletRequest request) {
		return problem(HttpStatus.NOT_FOUND, "Asset not found", "The requested asset was not found.", request);
	}

	@ExceptionHandler(QuoteNotFoundException.class)
	ProblemDetail handleQuoteNotFound(QuoteNotFoundException exception, HttpServletRequest request) {
		return problem(HttpStatus.NOT_FOUND, "Quote not found", "A quote is unavailable for the requested asset.", request);
	}

	@ExceptionHandler(InactiveAssetRefreshException.class)
	ProblemDetail handleInactiveRefresh(InactiveAssetRefreshException exception, HttpServletRequest request) {
		return problem(HttpStatus.CONFLICT, "Inactive asset", "An inactive asset cannot be refreshed.", request);
	}

	@ExceptionHandler(QuoteIntegrationException.class)
	ProblemDetail handleQuoteIntegration(QuoteIntegrationException exception, HttpServletRequest request) {
		return problem(HttpStatus.BAD_GATEWAY, "Quote provider unavailable",
				"The market quote service is temporarily unavailable.", request);
	}

	@ExceptionHandler(AuthenticationFailedException.class)
	ProblemDetail handleAuthenticationFailure(AuthenticationFailedException exception, HttpServletRequest request) {
		return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required.", request);
	}

	@ExceptionHandler(CashConflictException.class)
	ProblemDetail handleCashConflict(CashConflictException exception, HttpServletRequest request) {
		return problem(HttpStatus.CONFLICT, "Financial conflict", "The financial operation cannot be completed.", request);
	}

	@ExceptionHandler(PrimaryWalletMissingException.class)
	ProblemDetail handleMissingWallet(PrimaryWalletMissingException exception, HttpServletRequest request) {
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
				"An unexpected error occurred.", request);
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

