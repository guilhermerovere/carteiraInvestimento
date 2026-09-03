## Context

Ver `proposal.md` para a motivacao e a delta spec de `security-event-auditing` para o contrato. O Graphify atualizado localiza a lacuna entre `SecurityProblemDetailHandler`, configurado como `AccessDeniedHandler` em `FoundationSecurityConfiguration`, e o port `AuditoriaIsoladaPort`. O handler ja produz o `ProblemDetail` sanitizado; `AuditoriaPersistenceAdapter.recordIsoladamente` ja oferece `REQUIRES_NEW`; `CorrelationIdFilter` ja disponibiliza o UUID validado ou gerado no atributo da requisicao. O teste de integracao atual demonstra `ACESSO_NEGADO` apenas chamando o port diretamente.

## Goals / Non-Goals

**Goals:**

- Fazer a negacao de autorizacao real de Spring Security produzir a auditoria tipada, correlacionada e isolada ja prevista.
- Preservar integralmente a semantica e o corpo sanitizado do `403`, inclusive quando a auditoria falhar.
- Demonstrar o comportamento por uma requisicao HTTP autenticada sem permissao e consulta ao registro persistido.

**Non-Goals:**

- Alterar o fluxo de autenticacao, JWT, regras de role, endpoints funcionais, modelo/tabela de auditoria ou migrations.
- Auditar `401`, criar endpoint de probe em producao, criar consulta de auditoria, ou modificar os demais tipos de evento.

## Decisions

### 1. Auditar na fronteira de negacao de acesso ja configurada

Estender o componente que ja atende `AccessDeniedHandler` para construir `AuditoriaCommand` somente no metodo de negacao de autorizacao, antes de escrever a resposta. A identidade vira do `SecurityContext` autenticado e o endpoint da URI da requisicao; o correlation ID vem de `CorrelationIdFilter.correlationId(request)`. O comando usa `TipoEvento.ACESSO_NEGADO`, `ResultadoAuditoria.NEGADO` e `SeveridadeAuditoria.ALERTA`.

Essa e a unica fronteira comum para respostas 403 produzidas pelo fluxo de autorizacao atual e evita instrumentar controllers ou criar rotas. Auditar em cada controlador foi rejeitado porque perderia negativas que ocorrem antes deles e duplicaria a regra.

### 2. Reutilizar somente o port transacional isolado e conter falhas

O handler chamara `AuditoriaIsoladaPort.recordIsoladamente`, preservando a transacao `REQUIRES_NEW` do adapter existente. Qualquer excecao dessa chamada sera capturada, registrada apenas como diagnostico sanitizado e nao propagada; em seguida, o handler escrevera exatamente o mesmo `403` que ja escreve hoje.

Permitir que a excecao suba foi rejeitado porque poderia substituir o `403` por erro de infraestrutura. Criar uma nova transacao ou adapter foi rejeitado porque duplicaria uma garantia de isolamento ja existente.

### 3. Provar a cadeia inteira com um probe exclusivamente de teste

Adicionar ou adaptar uma configuracao de teste que carregue um controlador/probe protegido por role apenas no contexto de teste. Uma requisicao com JWT valido de `ROLE_USER` para esse probe administrativo devera verificar `403`, `application/problem+json`, correlation ID devolvido e a linha PostgreSQL correspondente. O teste nao cria endpoint em fontes de producao e substitui ou reduz a insercao manual que hoje e a unica prova de `ACESSO_NEGADO`.

Um teste unitario que chama diretamente o port foi rejeitado como prova principal porque nao exercita o `AccessDeniedHandler`, o principal autenticado, o filtro de correlation ID nem o contrato HTTP. A cobertura unitária do handler pode complementar o teste de integracao para forcar falha do port e garantir a preservacao do 403.

## Risks / Trade-offs

- [O principal presente no contexto nao ter UUID utilizavel] → tratar esse caso apenas conforme o contrato ja aplicado a um `403` de principal autenticado, sem registrar dados derivados de payload ou credencial; os testes devem usar o principal JWT persistido real.
- [A auditoria indisponivel durante a negacao] → capturar a falha, emitir somente log sanitizado e manter o mesmo `403`/`ProblemDetail`.
- [Probe de teste escapar para fontes de producao] → declara-lo dentro da arvore/configuracao de testes e verificar que nenhuma rota funcional nova e criada.
- [Regressao dos eventos existentes] → manter enums, port, adapter e seu isolamento; executar a suite de auditoria e seguranca existente alem do novo fluxo.

## Migration Plan

1. Nenhuma migration, mudanca de configuracao ou rollout de contrato e necessaria.
2. Publicar o backend com a integracao; novas negacoes autenticadas passam a gerar linhas adicionais de auditoria conforme o contrato ja existente.
3. Em rollback de aplicacao, nenhuma estrutura persistida precisa ser revertida; eventos ja gravados permanecem como historico valido.
