## Context

O backend já usa Java 21/Spring Boot, PostgreSQL/Flyway, UUIDs e as camadas domain/application/infrastructure/presentation. As migrations atuais terminam em V3; `ddl-auto` valida o mapeamento. Persistência, `ProblemDetail`, JWT, `SecurityContext`, `X-Correlation-ID`, Testcontainers e auditoria de 403 já existem. Ver `proposal.md` para a motivação e `specs/asset-catalog/spec.md` para o contrato.

## Goals / Non-Goals

**Goals:**

- Estabelecer `Ativo` como catálogo global, mantendo `acoes` e `/api/v1/acoes` por aderência ao PRD.
- Fazer PostgreSQL a autoridade final para forma canônica, formato, unicidade e coerência dos dados.
- Reutilizar segurança, correlação e tratamento de erros existentes sem acoplar o domínio puro ao Spring Security.

**Non-Goals:**

- Não introduzir frontend, BFF, cotação, preço, provider, cache, FX, corretora, transação, posição, saldo, caixa, snapshot, patrimônio, dashboard ou auditoria operacional.
- Não criar DELETE, endpoint administrativo paralelo, toggle de lifecycle, filtros genéricos ou índice especulativo de busca textual.
- Não modificar capabilities existentes; a capability nova descreve todo o comportamento adicional.

## Decisions

### Domínio global e identidade estrutural

O domínio terá `Ativo`, `TipoAtivo`, `Mercado`, `Moeda` e uma única regra de ticker. O ativo não terá vínculo com usuário ou carteira. Ticker, tipo, mercado e moeda serão criados uma única vez; só o nome será editável. O nome será trimado, terá de 1 a 160 caracteres após normalização e não poderá ser blank. Lifecycle será comando explícito que recebe o estado desejado e é idempotente.

Alternativa considerada: alterar identidade por PATCH ou usar toggle. Rejeitada porque a identidade será referenciada por módulos futuros e toggle não representa intenção idempotente.

### Defesa em profundidade no schema

`V4__create_acoes.sql` criará `acoes` com `VARCHAR(160)` para nome, `TIMESTAMPTZ`, `CHECK (ticker = upper(btrim(ticker)))`, `CHECK (btrim(nome) <> '')`, valores controlados, coerência B3/BRL e US/USD, e CHECK do formato por mercado: B3 `^[A-Z]{4}[0-9]{1,2}$`, US `^[A-Z]{1,5}$`. Esses formatos são restrição deliberada desta capability, não uma regra universal de mercado. A unicidade será a constraint nomeada estável `uk_acoes_ticker` sobre `ticker` canônico.

A aplicação continuará a executar trim, uppercase, validação e persistência; os CHECKs protegem escrita direta e regressões no adapter. Moeda não será recebida na criação: `Mercado` a deriva para BRL ou USD.

### Ports, concorrência e escopo de visibilidade

A application definirá `AtivoPort` e serviços de criar, listar, consultar ticker, atualizar nome e definir lifecycle. O adapter JPA fará `saveAndFlush` na criação e converterá somente violação da constraint `uk_acoes_ticker` em conflito de ticker; qualquer outra constraint seguirá como falha distinta, jamais 409 de duplicidade. Pre-check pode existir apenas como otimização; `uk_acoes_ticker` é a autoridade final sob concorrência.

Presentation/application resolverá a role em um escopo controlado antes do adapter (por exemplo, `ACTIVE_ONLY`, `ALL` ou `FILTERED`). O domínio permanece livre de `SecurityContextHolder` e APIs Spring Security. User recebe escopo ativo mesmo se enviar `ativo=false`; admin sem filtro recebe todos e com filtro recebe o estado solicitado.

### Contratos HTTP estritos e estáveis

O controller manterá `/api/v1/acoes`. POST aceita exatamente `ticker`, `nome`, `tipo`, `mercado`; PATCH de metadado aceita exatamente `nome`; PATCH `/ativo` aceita exatamente `ativo`. Campos desconhecidos ou estruturais indevidos serão rejeitados localmente para o catálogo com 400/ProblemDetail, sem alterar a configuração Jackson global.

As mutações retornam `AtivoResponse` com somente `id`, `ticker`, `nome`, `tipo`, `mercado`, `moeda`, `ativo`, `criadoEm`, `atualizadoEm`: POST 201, PATCHs 200. A lista retorna envelope próprio com `items`, `page`, `size`, `totalElements`, `totalPages`, nunca `Page` de Spring Data. `page` default 0 e `size` default 20; `page >= 0` e `1 <= size <= 100`. A ordenação é allowlist ticker/nome e asc/desc, padrão ticker asc. `q` é trimado, case-insensitive, contains em ticker ou nome; vazio equivale à ausência de filtro.

### Erros, auditoria e índices

Erros de entrada e payload estrito retornam 400; não encontrado ou inativo para user retorna 404; `uk_acoes_ticker` retorna 409; 401/403, ProblemDetail e correlação reutilizam a infraestrutura existente. CRUD autorizado não cria `logs_auditoria`; 403 continua auditado pela capability vigente. `UNIQUE(ticker)` fornece índice de busca exata. Busca `q` por contains não terá índice nesta change, evitando otimização prematura.

## Risks / Trade-offs

- [Formato de ticker inicialmente restrito] → documentar e testar como escopo atual; expansão requer change futura.
- [Concorrência entre pre-check e insert] → UNIQUE + `saveAndFlush` + tradução seletiva e teste com PostgreSQL real.
- [Campos extras silenciosamente ignorados] → desserialização estrita localizada e testes MVC.
- [Busca contains sem índice] → paginação e filtros mínimos; medir antes de adicionar índice.
- [Migration forward-only] → não editar V1–V3; validar Flyway/Hibernate e usar migration compensatória futura se necessário.

## Migration Plan

1. Criar V4 com `uk_acoes_ticker`, canonicalização de ticker, formato condicional por mercado, nome não blank/160 e demais CHECKs.
2. Mapear o schema com `ddl-auto=validate`, sem geração de schema pelo ORM.
3. Validar contra PostgreSQL/Testcontainers, inclusive inserts diretos inválidos, tradução seletiva e concorrência.
4. Não há rollback automático: uma reversão funcional não apaga registros e qualquer ajuste estrutural é forward-only.

## Open Questions

Nenhuma.
