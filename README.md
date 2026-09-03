# Carteira Investimento

Projeto academico desenvolvido no 6o semestre da UNIFEF, sob orientacao do professor Jefferson Antonio Ribeiro Passerine.

Esta change estabelece identidade persistida, carteira principal minima, autenticacao JWT Bearer HS256, autorizacao por role e auditoria de seguranca. O backend usa PostgreSQL, Flyway e Hibernate com `ddl-auto: validate`.

## Configuracao local

Copie o contrato de configuracao e preencha valores locais seguros:

```powershell
Copy-Item .env.example .env
```

O arquivo `.env` nao deve ser versionado. Para executar o backend diretamente no host, configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e, opcionalmente, `BACKEND_PORT` (padrao `8080`).

### JWT e administrador inicial

As variaveis abaixo sao consumidas pelo backend:

| Variavel | Regra |
| --- | --- |
| `JWT_SECRET_KEY` | Obrigatoria; chave UTF-8 com no minimo 32 bytes (256 bits). Nunca versione uma chave real. |
| `JWT_EXPIRATION_HOURS` | Opcional; inteiro positivo, com padrao `24`. |
| `JWT_ISSUER` | Opcional; padrao `carteira-investimento-backend`. |
| `JWT_AUDIENCE` | Opcional; padrao `carteira-investimento-api`. |
| `ADMIN_NAME` | Obrigatoria para o provisionamento inicial. |
| `ADMIN_EMAIL` | Obrigatoria para o provisionamento inicial; e canonicalizada. |
| `ADMIN_PASSWORD` | Obrigatoria para o provisionamento inicial; segue a politica de senha. |

`ADMIN_NAME`, `ADMIN_EMAIL` e `ADMIN_PASSWORD` formam um conjunto: ausencia, preenchimento parcial ou valor invalido interrompe a inicializacao antes do provisionamento. Com configuracao valida, o startup cria somente uma identidade ativa `ROLE_ADMIN`, sem carteira, e preserva senha e role se esse administrador ja existir. Um e-mail que ja pertenca a `ROLE_USER` tambem faz o startup falhar sem promover nem alterar o usuario.

## API de identidade

### Cadastro

`POST /api/v1/auth/register` recebe somente:

```json
{
  "nome": "Nome da Pessoa",
  "email": "pessoa@example.test",
  "senha": "Senha@2026"
}
```

A senha deve ter ao menos oito caracteres, com letra maiuscula, minuscula, numero e caractere especial. Em sucesso, a resposta `201 Created` contem exatamente `id`, `nome`, `email`, `role` e `ativo`. O cadastro fixa `ROLE_USER`, cria a `Carteira Principal` com saldo zero e nao emite token. Entrada invalida retorna `400`; e-mail canonicalizado ja utilizado retorna `409`.

### Login e Bearer

`POST /api/v1/auth/login` recebe somente `email` e `senha`:

```json
{
  "email": "pessoa@example.test",
  "senha": "Senha@2026"
}
```

Em sucesso, a resposta `200 OK` contem exatamente `accessToken`, `tokenType` (sempre `Bearer`) e `expiresIn` em segundos. Credenciais invalidas, usuario inativo e falhas de autenticacao retornam `401` com detalhe generico; a resposta nunca inclui senha, hash ou credencial adicional.

Use o token em rotas protegidas:

```http
Authorization: Bearer <accessToken>
```

O token usa HS256 e contem `sub`, `role`, `iat`, `exp`, `jti`, `iss` e `aud`. Em cada requisicao protegida, a assinatura, issuer, audience e o usuario persistido sao validados. Usuario inexistente, inativo ou com role divergente da claim recebe `401`. Um principal autenticado, mas sem permissao para uma rota, recebe `403`. Ambos usam `application/problem+json` sanitizado.

`GET /api/v1/auth/me` exige Bearer valido e retorna somente `id`, `nome`, `email`, `role` e `ativo` do principal do `SecurityContext`.

## Auditoria e endpoints tecnicos

Os eventos de cadastro, login, tentativas rejeitadas, acesso negado e provisionamento inicial sao persistidos com metadados tipados. O `X-Correlation-ID` e aceito apenas se for UUID canonico valido; caso contrario, o backend gera um UUID. Senhas, hashes, tokens, cabecalhos de autorizacao, corpos completos e dados privados nao sao registrados.

Permanecem publicos:

- Healthcheck: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

O OpenAPI anuncia o esquema Bearer para as rotas protegidas. Todas as outras rotas exigem autenticacao, salvo allowlist explicita.

## Limites desta change

O escopo termina na identidade, na carteira principal de saldo zero, na seguranca e na auditoria. Nenhuma capacidade adicional e inferida a partir desses contratos.

## Testes

No Windows, a partir de `backend/`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

`test` cobre testes unitarios e de integracao. `verify` tambem executa os testes `*IT`. Docker deve estar disponivel para o PostgreSQL `16.15-alpine3.24` iniciado por Testcontainers; nao ha fallback H2.

## Execucao com Docker

Na raiz do repositorio:

```powershell
docker compose config
docker compose up --build
```

A sequencia de prontidao e PostgreSQL, Flyway e Hibernate validate, seguida de `/actuator/health` com status `UP`. Para encerrar sem remover o volume:

```powershell
docker compose down
```
