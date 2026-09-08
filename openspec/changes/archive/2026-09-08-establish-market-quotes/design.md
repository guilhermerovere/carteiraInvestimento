## Context

See `proposal.md` for motivation and `specs/market-quotes/spec.md` for the behavior contract. O catálogo existente persiste Ativo em `acoes`, com UUID, `mercado`, `moeda` estrutural e lifecycle, e o expõe por controller próprio. OpenFeign, Caffeine, Flyway, PostgreSQL, JWT, ProblemDetail e a auditoria de 403 já integram a stack; não há cliente externo nem dado de preço atual em Ativo. O grafo atualizado confirma os pontos de extensão Ativo, controller, erro, segurança, migrations e testes de integração. Não foi localizado artefato local do Explore concluído para market quotes; as decisões abaixo se limitam ao PRD e ao escopo explicitamente fornecido.

## Goals / Non-Goals

**Goals:**

- Criar uma fronteira de aplicação pequena, `MarketQuotePort` (ou equivalente), que entregue um modelo interno de cotação sem expor Feign, URL, JSON ou API keys a domínio/application.
- Separar observações históricas do agregado Ativo e garantir que toda obtenção externa aceita seja persistida.
- Implementar leitura fresca por cache Caffeine de 10 minutos, refresh administrativo síncrono e cadeia determinística Brapi/B3 ou AlphaVantage→TwelveData/US.
- Integrar aos contratos existentes de segurança, lifecycle, ProblemDetail e correlation ID sem criar auditoria operacional adicional.

**Non-Goals:**

- USD/BRL ou qualquer FX, tabela de cotação atual, stale fallback, scheduler, refresh assíncrono, Redis ou lógica de horário de bolsa.
- Transações, posições, patrimônio, dashboard, frontend, BFF, Next.js ou TanStack Query.
- WebClient, RestTemplate, SDK externo ou Resilience4j.

## Decisions

### Modelo e persistência histórica

Introduzir o modelo de domínio `Cotacao`/`HistoricoCotacao` independente de `Ativo`, com valor monetário positivo, `Moeda`, enum controlado de provider e `Instant` para `instanteCotacao` e `recebidoEm`. A migration forward-only criará `historico_cotacoes` com `id UUID`, `ativo_id UUID NOT NULL REFERENCES acoes(id) ON DELETE RESTRICT`, `preco NUMERIC(15,4) NOT NULL`, `moeda`, `instante_cotacao TIMESTAMPTZ`, `origem_provider` e `recebido_em TIMESTAMPTZ`; terá checks de preço, moeda e provider, sem `criado_em` redundante. Criar apenas o índice de leitura recente `(ativo_id, instante_cotacao DESC, recebido_em DESC, id DESC)` e nenhuma unicidade de observação.

Essa decisão mantém Ativo como catálogo imutável em sua identidade e deixa o histórico com retenção indefinida. Alternativas rejeitadas: `precoAtual` em `acoes` e tabela `cotacao_atual`, pois misturam catálogo e dado mutável; unicidade por valor/timestamp, pois elimina observações externas legítimas.

### Ports, adapters e provider chain

A application receberá uma port orientada ao uso, como `MarketQuotePort`, e uma port de repositório de histórico. Uma policy/factory de aplicação selecionará a cadeia por `Mercado`: B3 chama exclusivamente adapter Brapi; US chama AlphaVantage e tenta TwelveData nas falhas elegíveis. Cada adapter Feign possui contrato externo e DTOs privados de infraestrutura, converte a resposta para o modelo interno e classifica a falha em `NOT_FOUND` ou falha de integração.

Timeout, 429, 5xx, vazio, malformado, indisponibilidade e credencial/configuração inválida são falhas de integração; no primário US acionam fallback. Símbolo não encontrado no primário também permite tentativa do fallback antes de decidir 404; nenhum provider aplicável encontrar o símbolo produz 404 de cotação indisponível. Se a cadeia terminar em falha de integração, produz 502. Alternativas rejeitadas: vazar clientes Feign ao domínio, fallback B3 inventado e retornar 401 para credencial externa inválida.

Configurar um timeout único de 4 segundos para todos os adapters, dentro do intervalo do PRD. Propriedades externas conterão tokens/keys de Brapi, AlphaVantage e TwelveData sem defaults secretos; a implementação atualizará `application.yml`, `.env.example` e `docker-compose.yml` no padrão do repositório.

### Fluxo de cotação atual e cache

O serviço de aplicação localiza o Ativo por UUID e aplica a regra de lifecycle antes de acessar cache ou provider. Para ativo elegível, o cache Caffeine é indexado exclusivamente por UUID do Ativo, expira após 10 minutos e usa carregamento atômico por chave quando viável para single-flight. Cache hit retorna o DTO interno existente sem I/O externo nem persistência. Cache miss chama a cadeia aplicável, valida preço/moeda/timestamps, grava exatamente uma observação, popula o cache e responde. O refresh administrativo não lê o valor existente, sempre chama provider e sobrescreve a entrada somente após sucesso.

Histórico persistido é exclusivamente trilha de observações: nunca é usado para satisfazer GET atual após miss/expiração. Alternativas rejeitadas: fallback stale, que viola freshness; Redis, desnecessário para a primeira versão; scheduler, que introduz atualização fora de requisição.

### Lifecycle, API e erros

As APIs entram no controller sob `/api/v1/acoes/{id}`: GET atual e histórico para USER/ADMIN, PUT refresh somente ADMIN. O DTO próprio de cotação não expõe entity ou contratos externos; o histórico devolve envelope estável próprio e usa a ordenação suportada pelo índice. USER recebe 404 para ativo inativo em qualquer endpoint de mercado. ADMIN pode ler histórico inativo, porém GET atual também retorna 404 e PUT refresh retorna 409; ambos não acionam providers. Ativo ausente retorna 404.

Exceções de integração e indisponibilidade de cotação serão traduzidas no handler existente em ProblemDetail seguro: 404 para símbolo indisponível, 409 para refresh de inativo e 502 para cadeia externa. Os handlers preservam correlation ID e não interferem no mecanismo atual de 401/403, inclusive a auditoria já existente para 403. Não será criada auditoria de consulta ou provider.

## Risks / Trade-offs

- [Limites e contratos variáveis dos providers] → Centralizar parsing e classificação por adapter, usar HTTP stubs/fakes e tratar payload vazio/malformado como falha de integração.
- [Cache local não é compartilhado entre réplicas] → Aceitar a consistência por instância nesta versão; TTL curto, chave estável e sem Redis estão no escopo acordado.
- [Concorrência em cache miss pode duplicar observações] → Preferir loader atômico/single-flight por UUID e cobrir concorrência quando a implementação o suportar.
- [Timestamp ou moeda do provider inválidos] → Validar antes da persistência, derivar a moeda esperada do Ativo e responder 502 sanitizado para resposta externa inválida.
- [Admin precisa de dados antigos de ativo inativo] → Permitir apenas histórico; bloquear cotação atual e refresh para preservar lifecycle sem apresentar dado fresco inexistente.
- [Provider sem timestamp interpretável] → Rejeitar a resposta como inválida; nunca assumir timezone local ou fabricar Instant.
- [Refresh concorre com GET miss] → Coordenar localmente por UUID e impedir resultado mais antigo de sobrescrever mais novo no cache.

## Refinamentos de precisão, tempo e coordenação

Preço externo seguirá `parse BigDecimal → validar positivo → setScale(4, HALF_UP) → validar capacidade NUMERIC(15,4) → persistir`. Não numérico/não finito ou válido, zero, negativo e overflow são resposta externa inválida: 502 sanitizado, sem persistência ou cache. A migration terá também CHECK `moeda IN ('BRL','USD')` e preservará o índice `(ativo_id, instante_cotacao DESC, recebido_em DESC, id DESC)` sem UNIQUE.

Cada adapter interpreta timestamp conforme timezone/offset do contrato externo e converte para Instant; é proibido `ZoneId.systemDefault()` ou `LocalDateTime` sem timezone conhecido. Insuficiência temporal, valor impossível ou malformado é falha de integração. `recebidoEm` é criado pelo sistema ao aceitar resposta válida e freshness é dez minutos desde essa ingestão/inserção no cache, não desde `instanteCotacao`.

Em miss a ordem é provider, validação, transação curta de persistência com commit confirmado, cache, resposta. OpenFeign não fica dentro de transação longa. Refresh preserva entrada A até provider, validação e persistência de B terem sucesso; falha de integração ou persistência mantém A. Coordenação local por UUID cobre GET miss e refresh; serializa atualização de cache e não permite que observação mais antiga substitua uma mais nova. Não há lock distribuído ou Redis.

Credenciais ausentes não bloqueiam startup. Na operação são falha de integração, aplicam fallback US e nunca retornam 401. B3: NOT_FOUND→404, integração→502. US retorna 404 somente AV NOT_FOUND + TD NOT_FOUND; qualquer combinação inconclusiva com falha de integração→502; sucesso de TD→200. Logging técnico sanitizado pode registrar provider, ativoId, categoria, fallback e correlationId, nunca segredo, Authorization, payload completo ou auditoria operacional.

## Migration Plan

1. Aplicar migration Flyway aditiva para `historico_cotacoes` e índice de recente, mantendo `acoes` inalterada; Hibernate permanece em validate.
2. Publicar configuração externa sem versionar segredos; deployment só habilita obtenções externas quando as credenciais de cada provider aplicável existirem e forem válidas.
3. Implantar adapters, cache e APIs após a migration. Falhas de provider são observáveis apenas pelo contrato 502 sanitizado; não há migração de dados nem backfill.
4. Rollback de aplicação remove a superfície HTTP e desativa chamadas; a migration é forward-only, portanto a tabela histórica permanece sem afetar o catálogo existente.

## Open Questions

- Nenhuma decisão aberta que altere o contrato, a abordagem ou a divisão das tasks. Os detalhes sintáticos finais dos payloads dos providers serão isolados nos adapters e validados por stubs, sem alterar o comportamento especificado.
