---
type: "query"
date: "2026-09-04T02:20:45.009765+00:00"
question: "Qual componente produz o 403 em AuditSecurityEventsIntegrationTest.persistsAccessDeniedFromTheRealHttpAuthorizationFlow?"
contributor: "graphify"
outcome: "useful"
source_nodes: ["SecurityProblemDetailHandler", "GlobalExceptionHandler", "AuditSecurityEventsIntegrationTest", "FoundationSecurityConfiguration"]
---

# Q: Qual componente produz o 403 em AuditSecurityEventsIntegrationTest.persistsAccessDeniedFromTheRealHttpAuthorizationFlow?

## Answer

Expanded from original query via vocab: [security, authorization, denied, exception, handler, audit, problem, filter, access, controller, probe, integration]. The request AuthorizationFilter grants anyRequest().authenticated(). The controller test method @PreAuthorize denies via AuthorizationManagerBeforeMethodInterceptor and ThrowingMethodAuthorizationDeniedHandler, throwing AuthorizationDeniedException. GlobalExceptionHandler.handleAccessDenied resolves it inside DispatcherServlet and creates the 403 ProblemDetail. ExceptionTranslationFilter receives a normal return, so SecurityProblemDetailHandler.handle is not invoked and no audit event is recorded.

## Outcome

- Signal: useful

## Source Nodes

- SecurityProblemDetailHandler
- GlobalExceptionHandler
- AuditSecurityEventsIntegrationTest
- FoundationSecurityConfiguration