package com.carteira.carteiraInvestimento.infrastructure.security;

import com.carteira.carteiraInvestimento.presentation.error.ProblemDetailFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class SecurityProblemDetailHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
	private final ProblemDetailFactory problems;
	private final AccessDeniedAuditingService accessDeniedAuditing;

	public SecurityProblemDetailHandler(ProblemDetailFactory problems, AccessDeniedAuditingService accessDeniedAuditing) {
		this.problems = problems;
		this.accessDeniedAuditing = accessDeniedAuditing;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		write(response, problems.create(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required.", request));
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException exception) throws IOException {
		accessDeniedAuditing.record(request);
		write(response, problems.create(HttpStatus.FORBIDDEN, "Forbidden", "Access is denied.", request));
	}

	private void write(HttpServletResponse response, ProblemDetail problem) throws IOException {
		response.setStatus(problem.getStatus());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.getWriter().write("{\"title\":\"" + escape(problem.getTitle()) + "\",\"status\":"
				+ problem.getStatus() + ",\"detail\":\"" + escape(problem.getDetail()) + "\",\"instance\":\""
				+ escape(problem.getInstance().toString()) + "\"}");
	}

	private String escape(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
