## 1. Schema e integridade histórica

- [ ] 1.1 Criar migration Flyway forward-only para `historico_cotacoes` com UUID, FK `ativo_id` para `acoes(id)` com delete restrito, `NUMERIC(15,4)`, TIMESTAMPTZ, checks de preço/moeda/provider e sem `criado_em`; verificar a migration em PostgreSQL via Testcontainers.
- [ ] 1.3 Garantir CHECK de moeda BRL/USD, precisão `NUMERIC(15,4)`, ambos TIMESTAMPTZ, índice de ordenação e ausência de UNIQUE de observação; verificar escrita direta EUR/BTC rejeitada e empate de timestamps ordenado por id em PostgreSQL.
- [ ] 1.2 Criar somente o índice de histórico recente por ativo, instante de cotação, recebimento e id; verificar por inspeção do schema que não há UNIQUE de observação, tabela de cotação atual ou alteração de preço em `acoes`.

## 2. Domínio de cotações

- [ ] 2.1 Implementar modelo de domínio separado de Ativo para Cotacao/HistoricoCotacao, provider controlado e validação de preço positivo, moeda e Instants; verificar com testes unitários de valor inválido e construção válida.
- [ ] 2.2 Implementar política que compara moeda da cotação com a moeda estrutural do Ativo; verificar em teste que BRL/USD incompatíveis não são aceitos.
- [ ] 2.3 Implementar normalização BigDecimal para escala 4 HALF_UP e rejeição de zero, negativo e overflow; verificar escalas menor/igual/maior que 4, HALF_UP e limites de NUMERIC(15,4).

## 3. Ports e contratos de aplicação

- [ ] 3.1 Definir port orientada ao caso de uso para obtenção de cotação e port de persistência/consulta paginada de histórico, sem tipos Feign, URLs, JSON ou API keys; verificar por compilação e testes de application com fakes.
- [ ] 3.2 Definir comandos, resultados e exceções de aplicação para cotação indisponível, falha de integração e refresh de ativo inativo; verificar os mapeamentos esperados em testes unitários.

## 4. Providers e configuração externa

- [ ] 4.1 Implementar adapter OpenFeign Brapi para B3, com DTO externo privado, parsing de preço/moeda/timestamp e classificação segura de vazio, malformado, timeout, 4xx relevante, 429 e 5xx; verificar por HTTP stub sem rede real.
- [ ] 4.2 Implementar adapters OpenFeign AlphaVantage e TwelveData para US com o mesmo mapeamento interno; verificar parsing válido, decimal, currency e timestamp por HTTP stubs sem rede real.
- [ ] 4.5 Interpretar timestamp de cada provider com timezone/offset explícito e sem timezone da JVM; verificar testes independentes de timezone para timestamp inválido, ausente e válido.
- [ ] 4.6 Permitir credenciais ausentes no startup, classificar ausência/401/403 externos como integração e emitir logs técnicos sanitizados; verificar binding, fallback e ausência de API key, Authorization, payload completo ou `logs_auditoria` nos logs.
- [ ] 4.7 Implementar e testar matriz B3 e US: Brapi sem fallback; todas as combinações AV/TD NOT_FOUND, integração e sucesso produzem 404, 502 ou 200 determinísticos.
- [ ] 4.3 Implementar seleção B3→Brapi e US→AlphaVantage→TwelveData, incluindo fallback para falhas elegíveis e símbolo não encontrado no primário; verificar em testes de application que não existe fallback B3 e que credencial inválida não vira 401.
- [ ] 4.4 Adicionar propriedades externas, timeout único de 4 segundos e variáveis sem segredos reais em `application.yml`, `.env.example` e `docker-compose.yml`; verificar binding de configuração e ausência de chaves hardcoded.

## 5. Persistência de histórico

- [ ] 5.1 Implementar adapter JPA de histórico com gravação de cada obtenção externa aceita e consulta paginada na ordem `instanteCotacao DESC, recebidoEm DESC, id DESC`; verificar com teste de integração PostgreSQL/Testcontainers.
- [ ] 5.2 Verificar por testes de integração Flyway, Hibernate validate, FK, `NUMERIC(15,4)`, ambos os timestamps, retenção lógica e coexistência de observações com valores/timestamps iguais.

## 6. Cache e orquestração de aplicação

- [ ] 6.1 Configurar cache Caffeine de 10 minutos indexado pelo UUID do Ativo e carga atômica/single-flight por chave quando suportada; verificar TTL, chave e ausência de chamadas duplicadas concorrentes em teste controlado.
- [ ] 6.1.1 Medir TTL desde recebidoEm/inserção no cache, não instanteCotacao; verificar cotação de mercado antiga recém-obtida fresca por dez minutos.
- [ ] 6.2 Implementar GET atual: localizar/validar Ativo, retornar cache hit sem provider/persistência e, em miss, obter, validar, persistir e popular cache; verificar todos os caminhos com fakes e teste de cache.
- [ ] 6.3 Implementar refresh manual que ignora cache, força obtenção externa, persiste nova observação e substitui cache após sucesso; verificar que o valor em cache não impede a chamada ao provider.
- [ ] 6.3.1 Garantir provider→validação→persistência/commit→cache e que falha de persistência não retorna nem cacheia a cotação; verificar com fake de repositório que falha antes de cache.put.
- [ ] 6.3.2 Preservar cache existente em refresh com falha de provider ou persistência e substituir somente após sucesso; verificar A preservada e B ausente de cache/histórico parcial.
- [ ] 6.3.3 Manter HTTP externo fora de transação longa PostgreSQL e usar transação curta para persistência; verificar por teste/instrumentação que conexão/transação não permanece aberta durante OpenFeign.
- [ ] 6.3.4 Coordenar GET miss e refresh simultâneos por UUID; verificar ausência de chamadas desnecessárias e que observação mais antiga não sobrescreve cache mais novo.
- [ ] 6.4 Aplicar lifecycle antes de cache/provider: USER não vê inativo, ADMIN apenas lê seu histórico, GET atual inativo não busca provider e refresh inativo é conflito; verificar esses fluxos em testes de application.
- [ ] 6.5 Garantir que cache expirado ou ausente nunca usa histórico como stale fallback; verificar que falha da cadeia externa permanece erro, sem retorno de observação antiga.

## 7. API backend-only

- [ ] 7.1 Adicionar DTO de cotação limitado a id, ativoId, preco, moeda, instanteCotacao, provider e recebidoEm e envelope próprio de histórico; verificar serialização sem entity JPA ou payload externo.
- [ ] 7.2 Expor GET `/api/v1/acoes/{id}/cotacao` e GET `/api/v1/acoes/{id}/historico` com paginação page=0/size=20, limites e ordem definidos; verificar API para sucesso, página e 400.
- [ ] 7.2.1 Verificar histórico vazio como 200 com envelope vazio e UUID inválido como 400 com ProblemDetail e X-Correlation-ID.
- [ ] 7.3 Expor PUT `/api/v1/acoes/{id}/atualizar-cotacao` somente para admin e ligar os três endpoints aos casos de uso; verificar que não há endpoint duplicado por ticker, scheduler ou refresh assíncrono.

## 8. Segurança e erros

- [ ] 8.1 Aplicar autorização USER/ADMIN nos GETs e ADMIN no refresh preservando JWT, SecurityContext e auditoria existente de 403; verificar API para 401 e 403, inclusive correlation ID e auditoria de acesso negado existente.
- [ ] 8.2 Traduzir ativo inexistente e indisponibilidade por ticker para 404, refresh de inativo para 409 e falhas externas conhecidas para 502 com ProblemDetail sanitizado; verificar que credenciais externas nunca resultam em 401 ou 500 genérico.
- [ ] 8.3 Verificar que consultas, cache hits e refreshes autorizados não gravam `logs_auditoria` nem nova auditoria operacional de provider.

## 9. Cobertura de testes sem internet

- [ ] 9.1 Adicionar testes de adapters Feign com HTTP stub/fake para respostas válidas e falhas timeout, 4xx relevante, 429, 5xx, vazio e malformado; verificar que nenhum teste depende de Brapi, AlphaVantage ou TwelveData reais.
- [ ] 9.2 Adicionar testes de API para GET atual, histórico, refresh, paginação, 400, 401, 403, 404, 409, 502, ProblemDetail e `X-Correlation-ID`; verificar a suíte no backend.

## 10. Validação final

- [ ] 10.1 Executar `backend\mvnw.cmd verify` com Docker/Testcontainers disponível e corrigir falhas relacionadas à change; verificar suíte unitária e de integração aprovada.
- [ ] 10.2 Executar `npx.cmd openspec validate establish-market-quotes --strict`, `git diff --check` e revisar `git status` antes de concluir as tasks; verificar todos os comandos aprovados.
- [ ] 10.3 Revisar o diff para confirmar backend-only, ausência de USD/BRL/FX, stale fallback, scheduler, Redis, frontend/BFF, WebClient, RestTemplate, SDK externo e Resilience4j; verificar que `acoes` continua sem preço.
