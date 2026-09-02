---
type: "query"
date: "2026-09-01T23:41:30.041690+00:00"
question: "Localizar impactos existentes para estabelecer identidade e controle de acesso"
contributor: "graphify"
outcome: "useful"
source_nodes: ["FoundationSecurityConfiguration", "CarteiraInvestimentoApplication", "OpenApiConfiguration", "GlobalExceptionHandler", "ApplicationFoundationIT", "FlywayValidationIT", "PostgreSqlContainerSupport"]
---

# Q: Localizar impactos existentes para estabelecer identidade e controle de acesso

## Answer

Expanded from original query via graph vocab: [security, user, carteira, configuration, foundation, flyway, database, exception, swagger, healthcheck, application, autoconfigure]. O grafo confirmou FoundationSecurityConfiguration, exclusao de UserDetailsServiceAutoConfiguration, OpenApiConfiguration, GlobalExceptionHandler, ApplicationFoundationIT, FlywayValidationIT e PostgreSqlContainerSupport como seams principais; o planejamento preserva health, OpenAPI, Swagger, ProblemDetail e a cadeia PostgreSQL/Flyway/Testcontainers.

## Outcome

- Signal: useful

## Source Nodes

- FoundationSecurityConfiguration
- CarteiraInvestimentoApplication
- OpenApiConfiguration
- GlobalExceptionHandler
- ApplicationFoundationIT
- FlywayValidationIT
- PostgreSqlContainerSupport