## Context

O backend separa domain/application/infrastructure/presentation; `asset-catalog` e o padrao para catalogo, lifecycle e UNIQUE. Auditoria oferece escrita normal e isolada `REQUIRES_NEW`; OpenFeign usa 4 segundos; Flyway termina em V6 e Hibernate valida schema.

## Goals / Non-Goals

**Goals:** catalogo global com CNPJ imutavel, pipeline deterministico, lifecycle/auditoria idempotentes e contracts por role; I/O externo comprovadamente fora de transacao DB longa.

**Non-Goals:** Transacao/BUY/SELL/Posicao/caixa/FK, DELETE, revalidacao/scheduler/cache/retry, frontend/BFF e edicao oficial.

## Decisions

### Modelo, schema e timestamps

V7 criara `corretoras`: UUID, CNPJ CHAR(14), razao/nome fantasia/logradouro 255, bairro/cidade 160, CEP CHAR(8), UF CHAR(2), numero 20, complemento 160, ativo/timestamps. CHECKs protegem formato/limites e `uk_corretoras_cnpj` e final. Dominio puro canonicaliza CNPJ/checksum, CEP, UF/manuais. Criacao usa instante unico; mudanca real usa novo instante (Clock injetavel quando aderente); no-op nao grava nem muda timestamp.

### Pipeline, fontes e falhas

Orquestrador nao transacional executa local/precheck; BrasilAPI CNPJ (autoridade para identidade, situacao/CEP); Receita ATIVA; BrasilAPI CVM; registro ativo; ViaCEP obrigatorio com CEP oficial (autoridade exclusiva para logradouro/bairro/cidade/UF); valida trim/limites; constroi agregado. Numero/complemento sao locais. CNPJ semantico ausente, Receita nao ATIVA/CVM sem registro ativo sao 422. Falha tecnica/inutilizavel, ViaCEP ausente, CEP divergente ou oficial invalido/excedido sao 502. PRD nao oferece valor CVM alem de registro ativo; nao inventar enumeracoes.

### Fronteira transacional e concorrencia

Nenhum provider e chamado sob `@Transactional`. Componente local transacional separado recebe agregado admitido, faz save+flush/auditoria/commit. Teste verifica que providers sao chamados sem transacao ativa. Precheck otimiza; concorrencia pode repetir I/O, mas UNIQUE vence e perdedora 409 nao audita criacao.

### API, PATCH, responses e auditoria

GET CNPJ aceita somente 14 digitos/checksum; POST aceita formatacao. PATCH diferencia ausencia/null: ausencia mantem, null/blank remove, vazio 400 e igualdade normalizada no-op. Lifecycle igual e no-op. USER recebe SelectionResponse exata de cinco campos; ADMIN AdminResponse exata de 14 campos em GET/lista/mutacoes. Sucessos reais e eventos commitam juntos; falha reverte. Reprovacao Receita/CVM usa auditoria isolada; falha irrecuperavel retorna 500, sem Corretora. 502 nao vira compliance. Nunca gravar provider/headers/tokens/corpo.

### Testes e OpenAPI

Adapters usam fake/stub, MockWebServer/WireMock ou HTTP local; `mvn verify` sem internet. Cobrir sucesso, semantico, CVM, ViaCEP, 5xx, timeout, malformed, ausente/excedido. Testcontainers somente PostgreSQL/Flyway/Hibernate/constraints/concorrencia. OpenAPI documenta seis endpoints, CNPJ path canonico, PATCH/no-op, roles/responses e 400/401/403/404/409/422/502/500.

## Risks / Trade-offs

- [I/O concorrente duplicado] → aceitar; UNIQUE garante resultado.
- [Auditoria regulatoria falha] → 500, pois 422 auditado nao foi satisfeito.
- [Responses distintos] → pequena duplicacao pelo contrato minimo USER.

## Migration Plan

Aplicar V7 apos V6, validar Flyway/Hibernate e configurar providers. Sem backfill; rollback de aplicacao nao reverte migration.

## Open Questions

Nenhuma. O contrato CVM normativo disponivel e somente registro ativo.
