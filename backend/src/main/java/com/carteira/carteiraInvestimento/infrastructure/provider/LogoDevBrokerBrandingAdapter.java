package com.carteira.carteiraInvestimento.infrastructure.provider;

import com.carteira.carteiraInvestimento.application.port.BrokerBrandingPort;
import com.carteira.carteiraInvestimento.infrastructure.config.LogoDevProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import feign.FeignException;
import java.net.IDN;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class LogoDevBrokerBrandingAdapter implements BrokerBrandingPort {
	private static final Logger log = LoggerFactory.getLogger(LogoDevBrokerBrandingAdapter.class);
	private final LogoDevSearchClient client;
	private final LogoDevProperties properties;
	private final Cache<String, Optional<String>> cache = Caffeine.newBuilder()
			.maximumSize(1_000).expireAfterWrite(Duration.ofDays(7)).build();

	public LogoDevBrokerBrandingAdapter(LogoDevSearchClient client, LogoDevProperties properties) {
		this.client = client;
		this.properties = properties;
	}

	@Override
	public Optional<String> resolve(String legalName, String tradeName) {
		if (!properties.configured()) return Optional.empty();
		List<String> names = new ArrayList<>();
		if (legalName != null && !legalName.isBlank()) names.add(legalName.trim());
		if (tradeName != null && !tradeName.isBlank() && names.stream().noneMatch(tradeName::equalsIgnoreCase)) {
			names.add(tradeName.trim());
		}
		String key = names.stream().map(this::normalizeName).sorted().reduce((a, b) -> a + "|" + b).orElse("");
		return cache.get(key, ignored -> lookup(names));
	}

	private Optional<String> lookup(List<String> names) {
		try {
			Set<String> exactDomains = new LinkedHashSet<>();
			for (String name : names) {
				List<LogoDevSearchResult> results = client.search(name, "match", "Bearer " + properties.secretKey());
				if (results == null) continue;
				String normalizedInput = normalizeName(name);
				for (LogoDevSearchResult result : results) {
					if (result != null && normalizedInput.equals(normalizeName(result.name()))) {
						canonicalDomain(result.domain()).ifPresent(exactDomains::add);
					}
				}
			}
			return exactDomains.size() == 1 ? Optional.of(exactDomains.iterator().next()) : Optional.empty();
		} catch (FeignException exception) {
			log.warn("broker branding provider failure provider=LOGO_DEV failureCategory=http");
			return Optional.empty();
		} catch (RuntimeException exception) {
			log.warn("broker branding provider failure provider=LOGO_DEV failureCategory=payload");
			return Optional.empty();
		}
	}

	private String normalizeName(String value) {
		String ascii = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
				.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
		return ascii.replaceAll("\\b(s\\.?a\\.?|sa|ltda|limitada|sociedade|corretora|de|titulos|valores|mobiliarios)\\b", " ")
				.replaceAll("[^a-z0-9]", "");
	}

	private Optional<String> canonicalDomain(String raw) {
		if (raw == null || raw.isBlank()) return Optional.empty();
		try {
			String value = raw.trim().toLowerCase(Locale.ROOT);
			if (value.startsWith("http://") || value.startsWith("https://")) {
				value = java.net.URI.create(value).getHost();
			}
			if (value == null) return Optional.empty();
			value = IDN.toASCII(value.replaceFirst("^www\\.", ""));
			return value.matches("(?=.{1,253}$)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}")
					? Optional.of(value) : Optional.empty();
		} catch (IllegalArgumentException ignored) { return Optional.empty(); }
	}
}

@FeignClient(name = "logoDevSearchClient", url = "${application.logo-dev.url}")
interface LogoDevSearchClient {
	@GetMapping("/search")
	List<LogoDevSearchResult> search(@RequestParam("q") String query, @RequestParam("strategy") String strategy,
			@RequestHeader("Authorization") String authorization);
}

record LogoDevSearchResult(String name, String domain) { }
