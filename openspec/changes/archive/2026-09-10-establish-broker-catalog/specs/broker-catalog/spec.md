## Purpose

Estabelece catalogo global de corretoras para futuras transacoes, com identidade oficial, compliance, lifecycle local e administracao segura.

## ADDED Requirements

### Requirement: Corretora global canonica e preservavel
O sistema SHALL manter Corretora global, sem `usuario_id`, carteira, mercado, moeda, saldo, ativo financeiro, taxa ou campos de transacao. Ela SHALL conter UUID, CNPJ, razao social oficial, nome fantasia opcional, CEP/endereco postal, numero/complemento locais, lifecycle e timestamps. POST aceita CNPJ formatado ou somente digitos, canonicaliza para 14 digitos e valida checksum; CNPJ e imutavel e unico. PostgreSQL SHALL impor UNIQUE e CHECK de CNPJ/CEP/UF canonicos. Oficiais sao trimados, nao blank e limitados: razao social/logradouro 255, nome fantasia 255, bairro/cidade 160, UF 2, CEP 8; numero/complemento aplicam trim, blank para nulo e maximos 20/160.

#### Scenario: Criacao com CNPJ formatado
- **WHEN** ADMIN envia CNPJ formalmente valido formatado e dados manuais validos
- **THEN** persiste somente CNPJ de 14 digitos, manuais normalizados e dados oficiais admitidos

#### Scenario: Entrada local invalida
- **WHEN** CNPJ nao possui 14 digitos/checksum valido ou numero/complemento viola contrato
- **THEN** responde `400 Bad Request` sem chamada externa ou persistencia

#### Scenario: Duplicidade concorrente
- **WHEN** duas criacoes concorrentes usam o mesmo CNPJ canonico
- **THEN** exatamente uma corretora persiste, a perdedora recebe `409 Conflict` e nao ha segundo evento de criacao

### Requirement: Pipeline oficial deterministico e classificacao
Uma criacao aprovada SHALL executar: local/precheck; BrasilAPI CNPJ; validacao Receita; BrasilAPI CVM; validacao CVM; CEP oficial BrasilAPI; ViaCEP obrigatorio; validacao/canonicalizacao; construcao; persistencia. BrasilAPI CNPJ e autoridade para CNPJ, razao social, nome fantasia, situacao e CEP; ViaCEP e autoridade exclusiva para logradouro, bairro, cidade e UF; numero/complemento nunca vem de provider. ViaCEP e consultado em toda criacao; CEP divergente ou endereco incompleto falha. O PRD exige somente registro ativo na CVM, sem valores adicionais de campo/status; a integracao SHALL avaliar apenas esse contrato, sem inventar valores regulatorios.

BrasilAPI CNPJ semanticamente nao encontrada, Receita nao aceita, CVM sem registro ou sem registro ativo retornam `422 Unprocessable Content`. ViaCEP sem endereco utilizavel, timeout, 5xx, conexao, decoding, payload incompativel, campo oficial ausente/blank/excedendo limite ou outro contrato upstream inutilizavel retornam `502 Bad Gateway`. Nenhum caso persiste corretora.

#### Scenario: Compliance aprovado
- **WHEN** Receita encontra empresa ATIVA, CVM confirma registro ativo e ViaCEP devolve endereco completo consistente para o CEP oficial
- **THEN** cria corretora ativa e responde `201 Created`

#### Scenario: Reprovacao regulatoria conhecida
- **WHEN** Receita nao encontra empresa/recusa situacao ou CVM nao confirma registro ativo
- **THEN** nao cria corretora e responde `422 Unprocessable Content`

#### Scenario: Falha upstream
- **WHEN** provider falha tecnicamente, ViaCEP nao encontra CEP utilizavel, CEP diverge ou dado oficial e inutilizavel
- **THEN** nao cria corretora e responde `502 Bad Gateway` sanitizado

### Requirement: API estrita, PATCH parcial e lifecycle sem escrita desnecessaria
O sistema SHALL expor exatamente POST/GET em `/api/v1/corretoras`, GET UUID, GET `/cnpj/{cnpj}`, PATCH dados e PATCH `/ativo`; DELETE MUST NOT existir. GET CNPJ aceita exclusivamente path `^[0-9]{14}$` e checksum valido; CNPJ formatado/invalido no path retorna `400`, sem query alternativo. POST aceita exatamente `cnpj`, `numero`, `complemento`; PATCH manual aceita somente `numero`, `complemento`: ausente preserva, `null`/blank remove, texto normaliza/substitui, `{}` retorna `400`; proibido/desconhecido retorna `400`.

POST responde 201; PATCH responde 200. PATCH manual igual apos normalizacao ou lifecycle com estado atual responde 200 sem UPDATE, sem mudar `atualizadoEm` e sem auditoria. Mudanca real atualiza `atualizadoEm`; criacao usa o mesmo instante para criado/atualizado.

#### Scenario: PATCH parcial e no-op
- **WHEN** ADMIN envia campo ausente, nulo, blank, valido, `{}` ou valor normalizado igual
- **THEN** respectivamente mantem, remove, remove, substitui, retorna 400, ou retorna 200 sem escrita/timestamp/evento

#### Scenario: CNPJ invalido no path
- **WHEN** GET por CNPJ recebe formatado, nao numerico, tamanho invalido ou checksum invalido
- **THEN** responde `400 Bad Request`

### Requirement: Responses deterministicas, RBAC e visibilidade
Todas as rotas SHALL exigir autenticacao. USER somente lista/consulta ativas; inativa/inexistente retorna 404. ADMIN cria, altera, ativa/desativa e consulta ambas; USER em mutacao recebe 403 e anonimo 401. GET/lista de USER retorna exatamente SelectionResponse: `id`, `cnpj`, `razaoSocial`, `nomeFantasia`, `ativo`. ADMIN recebe exatamente AdminResponse: esses cinco e `cep`, `logradouro`, `bairro`, `cidade`, `uf`, `numero`, `complemento`, `criadoEm`, `atualizadoEm`. POST/PATCH retornam AdminResponse. Lista usa `{items,page,size,totalElements,totalPages}`, defaults 0/20, limites e ordem `razaoSocial ASC,id ASC`, sem filtros/sort livres.

#### Scenario: Responses por role
- **WHEN** USER ou ADMIN consulta/lista/muta corretora conforme autorizado
- **THEN** USER recebe somente SelectionResponse, ADMIN recebe AdminResponse e nunca entity, DTO externo, payload provider, auditoria ou detalhe tecnico

### Requirement: Fronteira transacional, auditoria e futuro
Providers SHALL ser chamados na fase externa sem transacao PostgreSQL ativa. Somente apos dados admitidos, fase local curta SHALL salvar, flushar UNIQUE, auditar sucesso e commitar. Mudanca real usa eventos sanitizados de criacao/edicao/ativacao/desativacao na mesma transacao; falha de auditoria reverte mutacao. Reprovacao Receita/CVM tenta auditoria isolada; se falha irrecuperavelmente, nao cria corretora e responde 500 sanitizado. Falha 502 nao e compliance rejeitado. Erros usam ProblemDetail/correlation ID sem SQL, stack trace, Feign, headers, tokens ou payload. Futura `transacoes.corretora_id` sera obrigatoria, requer ativa em BUY/SELL e tera `ON DELETE RESTRICT`; BUY/SELL nao chama providers.

#### Scenario: Auditoria atomica e rejeicao
- **WHEN** mutacao real conclui ou reprovacao regulatoria tem auditoria falha
- **THEN** sucesso commita corretora/evento juntos; falha de auditoria de rejeicao retorna 500 e nao cria corretora

#### Scenario: Superficie sem DELETE
- **WHEN** rotas sao enumeradas
- **THEN** existem somente os seis endpoints previstos e nenhuma rota DELETE
