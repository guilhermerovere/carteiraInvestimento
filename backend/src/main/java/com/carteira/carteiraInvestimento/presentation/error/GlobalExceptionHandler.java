package com.carteira.carteiraInvestimento.presentation.error;

import jakarta.servlet.http.HttpServletRequest;
import com.carteira.carteiraInvestimento.application.service.AuthenticationFailedException;
import com.carteira.carteiraInvestimento.application.service.DuplicateEmailException;
import com.carteira.carteiraInvestimento.application.service.DuplicateTickerException;
import com.carteira.carteiraInvestimento.application.service.AtivoNotFoundException;
import com.carteira.carteiraInvestimento.application.service.InactiveAssetRefreshException;
import com.carteira.carteiraInvestimento.application.service.QuoteIntegrationException;
import com.carteira.carteiraInvestimento.application.service.QuoteNotFoundException;
import com.carteira.carteiraInvestimento.application.service.CashConflictException;
import com.carteira.carteiraInvestimento.application.service.PrimaryWalletMissingException;
import com.carteira.carteiraInvestimento.application.service.BrokerNotFoundException;
import com.carteira.carteiraInvestimento.application.service.DuplicateBrokerException;
import com.carteira.carteiraInvestimento.application.service.BrokerComplianceException;
import com.carteira.carteiraInvestimento.application.service.BrokerUpstreamException;
import com.carteira.carteiraInvestimento.application.service.BrokerAuditException;
import com.carteira.carteiraInvestimento.application.service.CambioUnavailableException;
import com.carteira.carteiraInvestimento.application.service.InvestmentConflictException;
import com.carteira.carteiraInvestimento.application.service.InvestmentNotFoundException;
import com.carteira.carteiraInvestimento.application.service.FxExpiredException;
import com.carteira.carteiraInvestimento.application.service.AssetValidationUnavailableException;
import com.carteira.carteiraInvestimento.application.service.AccountClosureConflictException;
import com.carteira.carteiraInvestimento.application.service.IncorrectCurrentPasswordException;
import com.carteira.carteiraInvestimento.application.service.PortfolioValuationConflictException;
import com.carteira.carteiraInvestimento.application.service.PortfolioValuationUpstreamException;
import com.carteira.carteiraInvestimento.domain.investment.FinancialStateException;
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
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Invalid request", "The request is invalid.", request);
		if ("invalid cnpj".equals(exception.getMessage()) || "cnpj is required".equals(exception.getMessage())) {
			problem.setProperty("code", "BROKER_CNPJ_INVALID");
		}
		return problem;
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
		ProblemDetail problem = problem(HttpStatus.CONFLICT, "Email already registered", "An account with this email already exists.", request);
		problem.setProperty("code", "EMAIL_IN_USE");
		return problem;
	}

	@ExceptionHandler(IncorrectCurrentPasswordException.class)
	ProblemDetail handleIncorrectCurrentPassword(IncorrectCurrentPasswordException exception, HttpServletRequest request) {
		ProblemDetail problem = problem(HttpStatus.CONFLICT, "Current password is incorrect",
				"The current password could not be verified.", request);
		problem.setProperty("code", "CURRENT_PASSWORD_INCORRECT");
		return problem;
	}

	@ExceptionHandler(AccountClosureConflictException.class)
	ProblemDetail handleAccountClosureConflict(AccountClosureConflictException exception, HttpServletRequest request) {
		ProblemDetail problem = problem(HttpStatus.CONFLICT, "Account cannot be closed",
				"The account does not satisfy the closure requirements.", request);
		problem.setProperty("code", exception.reason().name());
		return problem;
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

	@ExceptionHandler(AssetValidationUnavailableException.class)
	ProblemDetail handleAssetValidationUnavailable(AssetValidationUnavailableException exception, HttpServletRequest request) {
		return problem(HttpStatus.BAD_GATEWAY, "Asset validation unavailable",
				"The financial asset validation service is temporarily unavailable.", request);
	}

	@ExceptionHandler(CambioUnavailableException.class)
	ProblemDetail handleCambioUnavailable(CambioUnavailableException exception, HttpServletRequest request) {
		return problem(HttpStatus.BAD_GATEWAY, "Exchange-rate provider unavailable",
				"A valid USD/BRL exchange rate is temporarily unavailable.", request);
	}

	@ExceptionHandler(AuthenticationFailedException.class)
	ProblemDetail handleAuthenticationFailure(AuthenticationFailedException exception, HttpServletRequest request) {
		return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required.", request);
	}

	@ExceptionHandler(CashConflictException.class)
	ProblemDetail handleCashConflict(CashConflictException exception, HttpServletRequest request) {
		return problem(HttpStatus.CONFLICT, "Financial conflict", "The financial operation cannot be completed.", request);
	}

	@ExceptionHandler({InvestmentConflictException.class, FinancialStateException.class})
	ProblemDetail handleInvestmentConflict(RuntimeException exception, HttpServletRequest request) {
		return problem(HttpStatus.CONFLICT, "Financial conflict", "The financial operation cannot be completed.", request);
	}

	@ExceptionHandler(FxExpiredException.class)
	ProblemDetail handleFxExpired(FxExpiredException exception, HttpServletRequest request) {
		ProblemDetail problem = problem(HttpStatus.CONFLICT, "Exchange rate expired",
				"The selected USD/BRL observation expired before confirmation.", request);
		problem.setProperty("code", "FX_EXPIRED");
		return problem;
	}

	@ExceptionHandler(PortfolioValuationConflictException.class)
	ProblemDetail handleValuationConflict(PortfolioValuationConflictException exception, HttpServletRequest request) {
		return problem(HttpStatus.CONFLICT, "Valuation conflict",
				"The portfolio changed while valuation was being calculated.", request);
	}

	@ExceptionHandler(PortfolioValuationUpstreamException.class)
	ProblemDetail handleValuationUpstream(PortfolioValuationUpstreamException exception, HttpServletRequest request) {
		return problem(HttpStatus.BAD_GATEWAY, "Valuation provider unavailable",
				"A required market data service is temporarily unavailable.", request);
	}

	@ExceptionHandler(InvestmentNotFoundException.class)
	ProblemDetail handleInvestmentNotFound(InvestmentNotFoundException exception, HttpServletRequest request) {
		return problem(HttpStatus.NOT_FOUND, "Resource not found", "The requested resource was not found.", request);
	}

	@ExceptionHandler(PrimaryWalletMissingException.class)
	ProblemDetail handleMissingWallet(PrimaryWalletMissingException exception, HttpServletRequest request) {
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
				"An unexpected error occurred.", request);
	}

	@ExceptionHandler(BrokerNotFoundException.class)
	ProblemDetail handleBrokerNotFound(BrokerNotFoundException exception, HttpServletRequest request) {
		return problem(HttpStatus.NOT_FOUND, "Broker not found", "The requested broker was not found.", request);
	}

	@ExceptionHandler(DuplicateBrokerException.class)
	ProblemDetail handleDuplicateBroker(DuplicateBrokerException exception, HttpServletRequest request) {
		ProblemDetail problem = problem(HttpStatus.CONFLICT, "CNPJ already registered", "Esta corretora já está cadastrada.", request);
		problem.setProperty("code", "BROKER_ALREADY_REGISTERED");
		return problem;
	}

	@ExceptionHandler(BrokerComplianceException.class)
	ProblemDetail handleBrokerCompliance(BrokerComplianceException exception, HttpServletRequest request) {
		String detail = "cvm-not-found".equals(exception.getMessage())
				? "Este CNPJ não está cadastrado na CVM."
				: "Este CNPJ não corresponde a uma corretora válida.";
		ProblemDetail problem = problem(HttpStatus.UNPROCESSABLE_CONTENT, "Broker rejected", detail, request);
		if ("cvm-not-found".equals(exception.getMessage())) problem.setProperty("code", "BROKER_CVM_NOT_REGISTERED");
		else if ("cvm-inactive".equals(exception.getMessage())) problem.setProperty("code", "BROKER_CVM_NOT_APPROVED");
		return problem;
	}

	@ExceptionHandler(BrokerUpstreamException.class)
	ProblemDetail handleBrokerUpstream(BrokerUpstreamException exception, HttpServletRequest request) {
		ProblemDetail problem = problem(HttpStatus.BAD_GATEWAY, "Broker validation unavailable", "Broker validation is temporarily unavailable.", request);
		problem.setProperty("code", "BROKER_VALIDATION_UNAVAILABLE");
		return problem;
	}

	@ExceptionHandler(BrokerAuditException.class)
	ProblemDetail handleBrokerAudit(BrokerAuditException exception, HttpServletRequest request) {
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "An unexpected error occurred.", request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
		accessDeniedAuditing.record(request);
		return problem(HttpStatus.FORBIDDEN, "Forbidden", "Access is denied.", request);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	ProblemDetail handleNotFound(NoResourceFoundException exception, HttpServletRequest request) {
		if (request.getRequestURI().startsWith("/api/v1/corretoras/cnpj/")) {
			return problem(HttpStatus.BAD_REQUEST, "Invalid request", "The request is invalid.", request);
		}
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

