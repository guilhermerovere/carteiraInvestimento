# Graph Report - projetoJeff  (2026-09-01)

## Corpus Check
- Corpus is ~25,793 words - fits in a single context window. You may not need a graph.

## Summary
- 239 nodes · 294 edges · 26 communities (18 shown, 8 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 11 edges (avg confidence: 0.91)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Backend Integration Tests
- Project Governance Foundation
- OpenSpec Package Metadata
- Frontend Runtime Dependencies
- Frontend Tooling Dependencies
- TypeScript Compiler Configuration
- UI Component Configuration
- Security OpenAPI Configuration
- API Error Handling
- Maven Wrapper Script
- Frontend NPM Scripts
- TypeScript File Scope
- PostgreSQL Testcontainers Support
- OpenSpec Workflow Skills
- Spring Boot Entry Point
- Next App Providers
- Frontend Home Environment
- Database Migration Probe
- Database Migration Probe Copy
- Next Configuration
- Next Type Declarations
- PostCSS Configuration
- Maven Backend Module

## God Nodes (most connected - your core abstractions)
1. `compilerOptions` - 16 edges
2. `ApplicationFoundationIT` - 11 edges
3. `GlobalExceptionHandler` - 7 edges
4. `PostgreSqlContainerSupport` - 6 edges
5. `GlobalExceptionHandlerTest` - 6 edges
6. `aliases` - 6 edges
7. `scripts` - 6 edges
8. `include` - 6 edges
9. `Project Foundation Documentation` - 6 edges
10. `FailureProbeController` - 5 edges

## Surprising Connections (you probably didn't know these)
- `Project Foundation Documentation` --conceptually_related_to--> `Frontend Foundation Specification`  [INFERRED]
  README.md → openspec/specs/frontend-foundation/spec.md
- `Spec-Driven Development Policy` --conceptually_related_to--> `OpenSpec Spec-Driven Schema Configuration`  [INFERRED]
  AGENTS.md → openspec/config.yaml
- `Project Foundation Documentation` --conceptually_related_to--> `Backend Foundation Specification`  [INFERRED]
  README.md → openspec/specs/backend-foundation/spec.md
- `Project Foundation Documentation` --conceptually_related_to--> `Containerized Local Environment Specification`  [INFERRED]
  README.md → openspec/specs/containerized-local-environment/spec.md
- `Backend Application Configuration` --implements--> `Backend Foundation Specification`  [INFERRED]
  backend/src/main/resources/application.yml → openspec/specs/backend-foundation/spec.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **OpenSpec Change Lifecycle** — _agents_skills_openspec_explore_skill_openspec_explore, _agents_skills_openspec_propose_skill_openspec_propose, _agents_skills_openspec_apply_change_skill_openspec_apply_change, _agents_skills_openspec_archive_change_skill_openspec_archive_change [EXTRACTED 1.00]
- **Project Foundation Planning Artifacts** — openspec_changes_archive_2026_08_31_establish_project_foundation_proposal_project_foundation_proposal, openspec_changes_archive_2026_08_31_establish_project_foundation_design_project_foundation_design, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_backend_foundation_spec_backend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_containerized_local_environment_spec_containerized_environment_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_frontend_foundation_spec_frontend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_tasks_project_foundation_tasks [EXTRACTED 1.00]
- **Integrated Runtime Readiness** — docker_compose_local_service_orchestration, backend_src_main_resources_application_application_configuration, openspec_changes_archive_2026_08_31_establish_project_foundation_design_healthcheck_readiness_chain, openspec_specs_backend_foundation_spec_backend_foundation, openspec_specs_containerized_local_environment_spec_containerized_local_environment [INFERRED 0.95]

## Communities (26 total, 8 thin omitted)

### Community 0 - "Backend Integration Tests"
Cohesion: 0.12
Nodes (14): ApplicationFoundationIT, FlywayValidationIT, MissingDatabaseConfigurationTest, FailureProbeController, GlobalExceptionHandlerTest, javax.sql.DataSource, org.flywaydb.core.Flyway, org.junit.jupiter.api.BeforeEach (+6 more)

### Community 1 - "Project Governance Foundation"
Cohesion: 0.14
Nodes (21): Codex Project Instructions, Spec-Driven Development Policy, Backend Application Configuration, Local Service Orchestration, Archived Foundation Change Metadata, Healthcheck Readiness Chain, Project Foundation Design, Project Foundation Proposal (+13 more)

### Community 2 - "OpenSpec Package Metadata"
Cohesion: 0.10
Nodes (19): @fission-ai/openspec, author, bugs, url, description, devDependencies, @fission-ai/openspec, homepage (+11 more)

### Community 3 - "Frontend Runtime Dependencies"
Cohesion: 0.11
Nodes (19): class-variance-authority, clsx, dependencies, class-variance-authority, clsx, lucide-react, next, react (+11 more)

### Community 4 - "Frontend Tooling Dependencies"
Cohesion: 0.11
Nodes (19): eslint, eslint-config-next, devDependencies, eslint, eslint-config-next, shadcn, tailwindcss, @tailwindcss/postcss (+11 more)

### Community 5 - "TypeScript Compiler Configuration"
Cohesion: 0.11
Nodes (19): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+11 more)

### Community 6 - "UI Component Configuration"
Cohesion: 0.12
Nodes (16): aliases, components, hooks, lib, ui, utils, iconLibrary, rsc (+8 more)

### Community 7 - "Security OpenAPI Configuration"
Cohesion: 0.29
Nodes (8): FoundationSecurityConfiguration, OpenApiConfiguration, io.swagger.v3.oas.models.OpenAPI, OpenAPI, org.springframework.context.annotation.Bean, org.springframework.context.annotation.Configuration, org.springframework.security.config.annotation.web.builders.HttpSecurity, org.springframework.security.web.SecurityFilterChain

### Community 8 - "API Error Handling"
Cohesion: 0.42
Nodes (7): GlobalExceptionHandler, jakarta.servlet.http.HttpServletRequest, org.springframework.http.HttpStatus, org.springframework.http.ProblemDetail, org.springframework.web.bind.annotation.ExceptionHandler, org.springframework.web.bind.annotation.RestControllerAdvice, org.springframework.web.servlet.resource.NoResourceFoundException

### Community 9 - "Maven Wrapper Script"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 10 - "Frontend NPM Scripts"
Cohesion: 0.20
Nodes (9): name, private, scripts, build, dev, lint, start, typecheck (+1 more)

### Community 11 - "TypeScript File Scope"
Cohesion: 0.22
Nodes (8): exclude, include, .next/dev/types/**/*.ts, next-env.d.ts, .next/types/**/*.ts, node_modules, **/*.ts, **/*.tsx

### Community 12 - "PostgreSQL Testcontainers Support"
Cohesion: 0.48
Nodes (5): PostgreSqlContainerSupport, org.springframework.test.context.DynamicPropertyRegistry, org.springframework.test.context.DynamicPropertySource, org.testcontainers.containers.PostgreSQLContainer, org.testcontainers.junit.jupiter.Testcontainers

### Community 13 - "OpenSpec Workflow Skills"
Cohesion: 0.40
Nodes (6): OpenSpec Apply Change Workflow, OpenSpec Archive Change Workflow, OpenSpec Explore Mode, OpenSpec Propose Change Workflow, OpenSpec Sync Specs Workflow, OpenSpec Update Change Workflow

### Community 14 - "Spring Boot Entry Point"
Cohesion: 0.60
Nodes (3): CarteiraInvestimentoApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration

## Knowledge Gaps
- **88 isolated node(s):** `com.carteira:backend`, `foundation_probe`, `foundation_probe`, `$schema`, `style` (+83 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **8 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `dependencies` connect `Frontend Runtime Dependencies` to `Frontend NPM Scripts`?**
  _High betweenness centrality (0.024) - this node is a cross-community bridge._
- **Why does `devDependencies` connect `Frontend Tooling Dependencies` to `Frontend NPM Scripts`?**
  _High betweenness centrality (0.024) - this node is a cross-community bridge._
- **Why does `GlobalExceptionHandler` connect `API Error Handling` to `Backend Integration Tests`?**
  _High betweenness centrality (0.014) - this node is a cross-community bridge._
- **What connects `com.carteira:backend`, `foundation_probe`, `foundation_probe` to the rest of the system?**
  _88 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Backend Integration Tests` be split into smaller, more focused modules?**
  _Cohesion score 0.12315270935960591 - nodes in this community are weakly interconnected._
- **Should `Project Governance Foundation` be split into smaller, more focused modules?**
  _Cohesion score 0.14285714285714285 - nodes in this community are weakly interconnected._
- **Should `OpenSpec Package Metadata` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._