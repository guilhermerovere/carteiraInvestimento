package com.carteira.carteiraInvestimento.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-Correlation-ID";
	public static final String ATTRIBUTE = CorrelationIdFilter.class.getName() + ".correlationId";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		UUID correlationId = parseCanonicalUuid(request.getHeader(HEADER));
		if (correlationId == null) {
			correlationId = UUID.randomUUID();
		}

		request.setAttribute(ATTRIBUTE, correlationId);
		response.setHeader(HEADER, correlationId.toString());
		try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", correlationId.toString())) {
			filterChain.doFilter(request, response);
		}
	}

	public static UUID correlationId(HttpServletRequest request) {
		Object value = request.getAttribute(ATTRIBUTE);
		if (value instanceof UUID correlationId) {
			return correlationId;
		}
		throw new IllegalStateException("correlation id is unavailable");
	}

	private UUID parseCanonicalUuid(String value) {
		if (value == null || value.length() != 36) {
			return null;
		}
		try {
			UUID parsed = UUID.fromString(value);
			return value.equals(parsed.toString()) ? parsed : null;
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}
}
