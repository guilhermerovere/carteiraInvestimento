# Identity And Primary Wallet Specification

## Purpose

Define a identidade persistida, as duas roles admitidas e o ciclo minimo de criacao de usuarios, incluindo a carteira principal obrigatoria de `ROLE_USER` e o administrador inicial sem carteira.

## Requirements

### Requirement: Usuario persistido e protegido
O sistema SHALL persistir cada usuario com `id` UUID, `nome`, `email`, `senha_hash`, `role`, `ativo`, `criado_em` e `atualizado_em`. O sistema MUST armazenar senhas somente como hash BCrypt com strength 12 e MUST NOT expor senha ou `senha_hash` em qualquer resposta HTTP.

#### Scenario: Persistencia de novo usuario
- **WHEN** um usuario valido e criado
- **THEN** todos os campos obrigatorios sao persistidos, o identificador e UUID, `ativo` e verdadeiro e a senha armazenada e um hash BCrypt strength 12

#### Scenario: Resposta HTTP segura
- **WHEN** qualquer endpoint retorna dados de usuario
- **THEN** a resposta nao contem senha nem `senha_hash`

### Requirement: E-mail canonico e unico
O sistema SHALL aplicar `trim` e lowercase antes de persistir ou procurar um e-mail. O PostgreSQL MUST garantir unicidade case-insensitive do e-mail, sem depender somente de verificacao previa da aplicacao.

#### Scenario: Canonicalizacao de e-mail
- **WHEN** o cadastro recebe um e-mail com espacos nas extremidades e letras maiusculas
- **THEN** o sistema persiste e utiliza somente a forma sem espacos e em lowercase

#### Scenario: Duplicidade por variacao de caixa
- **WHEN** um cadastro tenta usar um e-mail que difere de outro persistido apenas por caixa ou espacos nas extremidades
- **THEN** o sistema rejeita a duplicidade e nenhuma segunda identidade e criada

#### Scenario: Concorrencia de cadastros duplicados
- **WHEN** cadastros concorrentes tentam persistir a mesma forma canonica de e-mail
- **THEN** a restricao PostgreSQL permite no maximo um usuario com esse e-mail

### Requirement: Role unica e restrita
Cada usuario MUST possuir exatamente uma role, limitada a `ROLE_USER` ou `ROLE_ADMIN`, representada como enum de dominio e protegida por constraint PostgreSQL. O sistema MUST NOT criar ou depender de tabela de roles.

#### Scenario: Role admitida
- **WHEN** um usuario e persistido
- **THEN** sua role e exatamente `ROLE_USER` ou `ROLE_ADMIN`

#### Scenario: Role invalida no banco
- **WHEN** uma escrita tenta persistir qualquer outro valor de role
- **THEN** o PostgreSQL rejeita a escrita por constraint

### Requirement: Carteira principal minima por usuario comum
Cada `ROLE_USER` SHALL possuir exatamente uma carteira principal e cada `ROLE_ADMIN` MUST NOT possuir carteira. A relacao estrutural SHALL ser `Usuario 1 -> 0..1 Carteira`; a carteira SHALL possuir `id` UUID, `usuario_id` UUID unico, `nome`, `saldo_caixa_brl` e `data_criacao`, com saldo inicial zero. A carteira criada pelo cadastro SHALL se chamar `Carteira Principal`.

#### Scenario: Carteira de usuario comum
- **WHEN** um `ROLE_USER` e criado pelo fluxo suportado
- **THEN** existe exatamente uma carteira chamada `Carteira Principal` ligada ao usuario e seu `saldo_caixa_brl` e zero

#### Scenario: Unicidade estrutural da carteira
- **WHEN** uma escrita tenta vincular uma segunda carteira ao mesmo usuario
- **THEN** o PostgreSQL rejeita a escrita pela unicidade de `usuario_id`

#### Scenario: Administrador sem carteira
- **WHEN** um `ROLE_ADMIN` e provisionado
- **THEN** nenhuma carteira e criada para ele

### Requirement: Cadastro publico transacional
`POST /api/v1/auth/register` SHALL aceitar somente a criacao de `ROLE_USER`, validar e-mail e senha, criar usuario ativo, carteira principal e auditoria de cadastro na mesma transacao, responder `201 Created`, nao autenticar automaticamente e nao emitir token. A resposta publica SHALL conter somente `id`, `nome`, `email`, `role` e `ativo`. A senha MUST possuir no minimo oito caracteres, incluindo ao menos uma letra maiuscula, uma minuscula, um numero e um caractere especial.

#### Scenario: Cadastro valido
- **WHEN** nome, e-mail ainda nao utilizado e senha valida sao enviados ao cadastro
- **THEN** o sistema responde `201 Created` somente com `id`, `nome`, `email`, `role` e `ativo`, sem token, senha ou hash, e persiste usuario ativo, carteira `Carteira Principal` com saldo zero e auditoria de cadastro na mesma transacao

#### Scenario: Senha fora da politica
- **WHEN** a senha nao satisfaz qualquer requisito minimo
- **THEN** o cadastro e rejeitado com `400 Bad Request` e nenhum usuario ou carteira e persistido

#### Scenario: E-mail invalido
- **WHEN** o cadastro recebe um e-mail sintaticamente invalido
- **THEN** o cadastro e rejeitado com `400 Bad Request` e nenhum usuario ou carteira e persistido

#### Scenario: E-mail ja utilizado
- **WHEN** o cadastro recebe um e-mail cuja forma canonica ja existe
- **THEN** o sistema responde `409 Conflict` e nao cria usuario nem carteira adicionais

#### Scenario: Falha ao criar carteira ou auditoria
- **WHEN** a persistencia da carteira ou da auditoria falha depois do inicio do cadastro
- **THEN** a transacao e revertida e nem usuario, carteira nem auditoria de sucesso permanecem persistidos

#### Scenario: Role fornecida pelo cliente
- **WHEN** o cliente tenta solicitar `ROLE_ADMIN` ou outra role no cadastro publico
- **THEN** o sistema ignora ou rejeita esse campo e nunca cria um administrador por esse endpoint

### Requirement: Provisionamento do administrador inicial
Na inicializacao, um `CommandLineRunner` SHALL processar `ADMIN_NAME`, `ADMIN_EMAIL` e `ADMIN_PASSWORD` como conjunto obrigatorio e validado. `ADMIN_PASSWORD` MUST obedecer a mesma politica de senha do cadastro e ser armazenada com BCrypt strength 12. Com configuracao valida, SHALL criar um `ROLE_ADMIN` ativo, sem carteira e com auditoria de criacao na mesma transacao somente quando o e-mail canonico nao existir; o processo SHALL ser idempotente e MUST NOT sobrescrever senha ou role existentes.

#### Scenario: Primeiro provisionamento valido
- **WHEN** as tres configuracoes `ADMIN_*` sao validas e o e-mail canonico nao existe
- **THEN** um `ROLE_ADMIN` ativo e criado com senha BCrypt strength 12, sem carteira e com auditoria de criacao na mesma transacao

#### Scenario: Administrador ja existente
- **WHEN** o e-mail configurado ja pertence a um `ROLE_ADMIN`
- **THEN** nenhum usuario e duplicado e sua senha e role nao sao alteradas

#### Scenario: Conflito com usuario comum
- **WHEN** o e-mail configurado pertence a um `ROLE_USER`
- **THEN** a inicializacao falha explicitamente sem promover o usuario nem alterar sua senha

#### Scenario: Configuracao ausente, parcial ou invalida
- **WHEN** qualquer configuracao `ADMIN_*` esta ausente, parcial ou invalida
- **THEN** a inicializacao falha explicitamente e nenhum administrador parcial e criado
