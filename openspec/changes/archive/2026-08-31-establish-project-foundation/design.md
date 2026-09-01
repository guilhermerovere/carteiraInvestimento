## Context

O repositório contém hoje um único projeto Maven em `carteiraInvestimento/`, com Java 21, Spring Boot 4.1.1, H2 e PostgreSQL declarados, além de um `application.properties` mínimo. Na raiz já existem `package.json` e `package-lock.json` dedicados ao tooling do repositório/OpenSpec; não existem frontend, Dockerfiles ou Compose. O `PRD.md` agora fixa Spring Boot 4.1.1, PostgreSQL 16+, Flyway, Next.js 15+ e execução integrada, e reserva identidade e domínio para changes específicas. Consulte `proposal.md` para a motivação e as três specs desta change para os contratos verificáveis.

## Goals / Non-Goals

**Goals:**

- Produzir uma árvore de projeto estável para que changes posteriores possam evoluir backend, frontend e dados sem nova reorganização estrutural.
- Fixar e documentar uma matriz compatível com Spring Boot 4.1.1 nos manifests, BOMs, lockfiles e imagens, preservando Java 21 e PostgreSQL 16.
- Tornar build, testes PostgreSQL e execução integrada verificáveis a partir de comandos documentados.
- Deixar pontos de extensão técnicos preparados, mas vazios de comportamento de negócio.

**Non-Goals:**

- Escolher ou criar o modelo relacional do domínio; nenhuma migration desta change criará as tabelas listadas no PRD.
- Definir contratos de autenticação, autorização, JWT, usuários, carteira ou demais APIs de negócio.
- Definir RBAC ou uma política de segurança definitiva; a configuração permissiva desta fundação é explicitamente temporária.
- Implementar adapters reais, regras financeiras, dashboards ou componentes funcionais de produto.
- Fazer merge, publicar imagens ou configurar infraestrutura de produção.

## Decisions

### 1. Reorganizar o monólito atual em dois projetos irmãos

O diretório `carteiraInvestimento/` será movido para `backend/` preservando o histórico dos arquivos, e `frontend/` será criado como projeto separado. Configurações compartilhadas de execução permanecem na raiz.

**Racional:** corresponde à estrutura mandatória do PRD e evita acoplamento entre ciclos de build Java e Node. A alternativa de manter o nome atual ou criar um monorepo com build unificado conservaria a divergência estrutural ou adicionaria tooling sem necessidade nesta fundação.

### 2. Fixar Spring Boot 4.1.1 e gerenciar compatibilidade por BOMs

O parent Maven permanecerá em Spring Boot 4.1.1 com Java 21. A matriz inicial da fundação será:

| Componente | Versão/linha | Gerenciamento |
|---|---:|---|
| Spring Boot | 4.1.1 | parent `spring-boot-starter-parent` |
| Spring Framework | 7.0.9 | BOM do Spring Boot 4.1.1 |
| Spring Security | 7.1.1 | BOM do Spring Boot 4.1.1 |
| Spring Cloud / OpenFeign | 2025.1.2 | BOM `spring-cloud-dependencies` |
| Springdoc OpenAPI | 3.1.0 | versão explícita compatível com Spring Boot 4.1.x |
| Testcontainers | 2.0.5 | BOM do Spring Boot 4.1.1 |
| Flyway | 12.4.0 | BOM do Spring Boot 4.1.1 |
| PostgreSQL JDBC | 42.7.13 | BOM do Spring Boot 4.1.1 |

Spring Cloud 2025.1.2 é o primeiro service release da linha 2025.1.x declarado compatível com Spring Boot 4.1.x. Springdoc 3.1.0 acompanha Spring Boot 4.1.0 e é a linha selecionada para o Boot 4.1.1. A implementação deverá confirmar a matriz com o modelo efetivo Maven e testes; qualquer incompatibilidade comprovada deverá ser documentada antes de um override.

Dependências já cobertas pelo parent do Spring Boot ou pelo BOM do Spring Cloud não receberão versões avulsas. Springdoc permanece explícito porque não é gerenciado pelo BOM do Boot. O conjunto base incluirá Web MVC, Validation, Data JPA, Security, Actuator, OpenFeign, Caffeine, Flyway PostgreSQL, driver PostgreSQL, Springdoc, logging via SLF4J e suporte de testes/Testcontainers.

**Racional:** manter Boot 4.1.1 respeita a decisão definitiva e o esqueleto atual. BOMs evitam combinações arbitrárias e reduzem overrides. A alternativa de migrar para Boot 3.x foi descartada; fixar manualmente cada dependência transitiva aumenta o risco de conflito.

Como Spring Security estará no classpath antes da change de identidade, uma configuração de fundação desabilitará login gerado e mecanismos de autenticação e permitirá somente o comportamento técnico necessário sem criar usuários ou JWT. Ela será marcada no código e na documentação como temporária e deverá ser substituída integralmente pela futura change de identidade/autenticação.

### 3. Tornar PostgreSQL e Flyway obrigatórios em todos os caminhos persistentes

H2 será removido por completo. `application.yml` apontará exclusivamente para variáveis de ambiente PostgreSQL, habilitará Flyway com `validate-on-migrate` e configurará Hibernate com `ddl-auto: validate`. O diretório `db/migration` será criado, mas esta change não adicionará DDL de domínio. A validação automatizada usará um PostgreSQL Testcontainers; testes unitários que não carregam persistência continuam independentes de banco.

**Racional:** um único mecanismo de schema e o mesmo motor em execução/testes eliminam diferenças silenciosas. A alternativa de H2 para testes contraria explicitamente o PRD. Criar uma migration vazia apenas para obter um número de versão foi rejeitado porque poluiria o histórico sem mudança estrutural; a suíte validará que o Flyway consegue migrar/validar um banco limpo sem migrations de domínio.

### 4. Separar configuração de aplicação, Compose e exposição ao navegador

`application.yml` consumirá `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `BACKEND_PORT`. `.env.example` documentará todo o contrato do PRD com valores seguros ou vazios, e `.env` conterá apenas valores locais acadêmicos permitidos pelo PRD. O Compose mapeará variáveis do host para cada container e usará o hostname interno do serviço PostgreSQL no JDBC.

`NEXT_PUBLIC_API_URL` representará a URL vista pelo navegador, normalmente `http://localhost:8080`; ela não será confundida com DNS interno de container. Variáveis reservadas para JWT, administrador e provedores poderão ser documentadas, mas não serão consumidas por funcionalidades nesta change.

Os `package.json` e `package-lock.json` existentes na raiz serão preservados exclusivamente para o tooling do repositório/OpenSpec. O Next.js será inicializado dentro de `frontend/`, com `frontend/package.json` e `frontend/package-lock.json` independentes. Nenhuma dependência frontend será adicionada ao manifesto raiz, e `node_modules` continuará ignorado em qualquer nível.

**Racional:** URLs internas e públicas têm consumidores distintos. Usar uma única URL interna para tudo faria o navegador tentar resolver um hostname exclusivo da rede Docker.

### 5. Usar imagens multi-stage e uma tag PostgreSQL de patch explícita

Os Dockerfiles separarão build e runtime. O backend compilará com JDK 21 e executará em JRE 21; o frontend instalará pelo lockfile, gerará build de produção e executará com runtime Node compatível com a versão Next.js selecionada. O Compose usará uma tag oficial `postgres:16.x` concreta escolhida na implementação e registrada também na documentação; `postgres:latest` e `postgres:16` são proibidas.

**Racional:** imagens finais menores e tags explícitas tornam builds e execução mais previsíveis. Uma tag por digest seria ainda mais imutável, mas prejudica legibilidade e atualização acadêmica; poderá ser adotada futuramente sem mudar o contrato.

### 6. Expressar prontidão real por dois healthchecks e dependências condicionais

O PostgreSQL usará `pg_isready` com as credenciais configuradas. O backend dependerá do banco com condição `service_healthy`, executará Flyway e Hibernate validate durante seu startup e exporá `GET /actuator/health` no servidor principal. O Compose verificará esse endpoint e somente considerará o backend saudável quando ele responder com sucesso e estado `UP`. O frontend dependerá do backend com condição `service_healthy`.

A sequência obrigatória é:

```text
PostgreSQL healthy
-> backend inicia e valida Flyway/JPA
-> backend healthy em /actuator/health
-> frontend inicia
```

**Racional:** processo criado ou container em execução não comprova prontidão HTTP. Sleeps e `depends_on` sem healthcheck foram rejeitados por serem frágeis. Actuator é parte obrigatória da fundação e complementa, sem substituir, o healthcheck PostgreSQL.

### 7. Criar apenas scaffolding arquitetural e contratos técnicos

Os pacotes `domain`, `infrastructure` e `presentation` serão materializados com arquivos técnicos mínimos necessários para compilação e documentação, sem nenhuma entidade JPA de domínio. OpenAPI terá metadados da aplicação e esquema Bearer apenas como documentação preparatória, sem implementar JWT. Um `RestControllerAdvice` produzirá `ProblemDetail` para categorias genéricas já testáveis, mantendo mensagens sanitizadas. Actuator exporá apenas o healthcheck necessário, sem detalhes sensíveis.

No frontend, a rota raiz será somente uma página de confirmação da fundação. Tailwind será funcional; TanStack Query terá um provider técnico se necessário para uso correto no App Router; Shadcn UI e Recharts serão instalados/configurados sem dashboards ou componentes de negócio.

**Racional:** pontos de extensão reais detectam incompatibilidades cedo, enquanto entidades fictícias ou endpoints demonstrativos contaminariam o escopo. Classes placeholder sem responsabilidade serão evitadas quando um package puder ser preservado por documentação de arquitetura ou por um tipo técnico legítimo.

### 8. Verificação em camadas

A implementação será validada por build e testes Maven, inspeção do modelo efetivo/dependency tree, build/lint do frontend, validação de configuração do Compose e uma subida integrada com verificação de `pg_isready`, `/actuator/health`, Swagger e página inicial. Os testes Testcontainers deverão ser identificáveis como integração e executar em ambiente com Docker.

**Racional:** cada ferramenta detecta uma classe diferente de falha. Confiar apenas na compilação não comprova wiring de banco, containers ou configuração de runtime.

## Risks / Trade-offs

- [Uma dependência pode ainda divergir da matriz Boot 4.1.1/Cloud 2025.1.2/Springdoc 3.1.0] → Confirmar o modelo efetivo Maven e os endpoints em testes; documentar tecnicamente qualquer override antes de aplicá-lo.
- [A mudança de diretório pode perder arquivos ou quebrar referências] → Fazer a movimentação preservando conteúdo/histórico, buscar referências ao caminho antigo e validar Maven Wrapper a partir de `backend/`.
- [Spring Security pode bloquear Swagger/Actuator ou criar autenticação padrão] → Adicionar configuração temporária explicitamente permissiva, sem login, usuários, JWT ou RBAC, marcada para substituição pela change de identidade/autenticação.
- [Flyway sem schema de domínio pode dar falsa impressão de banco concluído] → Documentar o limite, testar apenas inicialização/validação e proibir DDL das entidades nesta change.
- [Testcontainers depende de Docker e pode falhar em ambientes sem daemon] → Documentar o pré-requisito e manter falha explícita; não introduzir fallback H2.
- [Variáveis `NEXT_PUBLIC_*` são incorporadas no build e são públicas] → Usá-las somente para dados não secretos e construir a imagem com a URL adequada ao ambiente.
- [O Compose pode indicar containers iniciados antes de frontend/backend estarem prontos] → Exigir `pg_isready` no banco, `/actuator/health` no backend e dependências `service_healthy` em toda a cadeia.
- [O scaffolding Next.js pode sobrescrever os manifests Node da raiz] → Executar a criação estritamente em `frontend/`, revisar o diff e verificar que a raiz conserva apenas o tooling do repositório/OpenSpec.
- [Instalar toda a stack frontend aumenta o baseline] → Limitar configuração a habilitadores mandatórios e não criar componentes ou telas de negócio.

## Migration Plan

1. Confirmar a branch `feature/fundacao-do-projeto` e o estado do worktree antes de aplicar mudanças.
2. Mover o projeto existente para `backend/`, atualizar identificadores e referências e estabilizar o build Maven em Spring Boot 4.1.1/Java 21 com a matriz definida.
3. Substituir persistência/configuração, adicionar Actuator e segurança temporária, criar o scaffolding arquitetural e validar o backend com PostgreSQL Testcontainers.
4. Criar e validar o frontend base independente, preservando os manifests Node da raiz e fixando dependências no lockfile de `frontend/`.
5. Adicionar arquivos de ambiente, Dockerfiles e Compose; validar sintaxe e a sequência PostgreSQL healthy, backend healthy e frontend.
6. Executar todos os testes e builds e então validar `docker compose up --build`, `/actuator/health`, Swagger e a página técnica do frontend.

Rollback durante a implementação consiste em reverter os commits da change na branch de feature. Não há migração de dados a desfazer, pois esta fundação não cria schema de domínio e não será aplicada automaticamente a ambientes compartilhados.
