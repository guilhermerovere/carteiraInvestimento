# Containerized Local Environment Specification

## Purpose

Fornece um ambiente local integrado e reproduzível para executar PostgreSQL, backend e frontend com uma única configuração baseada em Docker Compose.

## Requirements

### Requirement: Configuração de ambiente local
 A raiz do repositório SHALL conter `.env` e `.env.example` com todas as variáveis necessárias para PostgreSQL, backend e frontend. Para o BFF do frontend, os arquivos e a orquestração SHALL fornecer `BACKEND_API_URL` e `APP_ORIGIN` como configuração server-only; `NEXT_PUBLIC_API_URL` MUST NOT ser dependência funcional do frontend. O `.env.example` MUST usar valores de exemplo seguros, e nenhum dos arquivos MUST conter segredo real.

#### Scenario: Preparação a partir do exemplo
- **WHEN** um desenvolvedor usa `.env.example` como referência para configurar o ambiente local
- **THEN** encontra `BACKEND_API_URL` e `APP_ORIGIN`, além das demais variáveis necessárias para iniciar os três serviços sem consultar configuração oculta

#### Scenario: Container frontend configurado
- **WHEN** o Compose inicia o frontend com a configuração local válida
- **THEN** o processo Next.js recebe os valores server-only necessários para alcançar o backend na rede interna e validar a origem pública canônica

#### Scenario: Inspeção de segurança
- **WHEN** os arquivos de ambiente versionáveis são revisados
- **THEN** eles contêm somente valores acadêmicos, locais, vazios ou claramente exemplificativos, sem credenciais reais

### Requirement: Orquestração dos três serviços
A raiz do repositório SHALL conter um `docker-compose.yml` que constrói e executa os serviços PostgreSQL, backend e frontend. O comando `docker compose up --build` SHALL iniciar o conjunto usando as portas configuráveis, com padrões locais 5432, 8080 e 3000 respectivamente.

#### Scenario: Inicialização integrada
- **WHEN** um desenvolvedor executa `docker compose up --build` com Docker disponível e a configuração local válida
- **THEN** PostgreSQL, backend e frontend iniciam e o frontend e o Swagger ficam acessíveis nas portas configuradas

### Requirement: PostgreSQL 16 versionado e saudável
O ambiente SHALL usar uma imagem oficial PostgreSQL da linha 16 com tag de patch explícita e MUST NOT usar `latest` nem uma tag flutuante apenas de major. O serviço SHALL possuir healthcheck que verifica prontidão para aceitar conexões.

#### Scenario: Imagem do banco inspecionada
- **WHEN** o `docker-compose.yml` é inspecionado
- **THEN** a imagem PostgreSQL possui uma tag explícita `16.x` e não usa `latest` ou somente `16`

#### Scenario: Banco ainda indisponível
- **WHEN** o PostgreSQL ainda não aceita conexões
- **THEN** seu healthcheck permanece não saudável e o backend não é iniciado como serviço pronto

### Requirement: Ordem e conectividade dos serviços
O backend SHALL aguardar o healthcheck PostgreSQL baseado em `pg_isready` antes de iniciar, SHALL conectar ao banco pelo nome interno do serviço e SHALL executar Flyway e Hibernate validate antes de se tornar saudável. O Compose SHALL verificar a prontidão real do backend por `GET /actuator/health`, e o frontend MUST aguardar o backend saudável antes de iniciar. Processo ou container apenas iniciado MUST NOT ser tratado como prova de prontidão.

#### Scenario: Subida após banco saudável
- **WHEN** o PostgreSQL passa para o estado saudável
- **THEN** o backend pode iniciar e executar Flyway e Hibernate validate, mas o frontend ainda aguarda o healthcheck do backend

#### Scenario: Subida após backend saudável
- **WHEN** `GET /actuator/health` do backend responde com sucesso e estado `UP`
- **THEN** o Compose considera o backend saudável e permite iniciar o frontend com a URL pública configurada

#### Scenario: Banco indisponível
- **WHEN** o PostgreSQL não se torna saudável
- **THEN** o Compose não inicia prematuramente o backend nem o frontend dependentes

#### Scenario: Backend indisponível
- **WHEN** o processo do backend iniciou mas `/actuator/health` ainda não retorna estado saudável
- **THEN** o Compose mantém o frontend aguardando e não considera a cadeia integrada pronta

### Requirement: Imagem executável do backend
O backend SHALL possuir uma definição de imagem Docker que use Java 21, produza o artefato com Maven de forma reproduzível e execute a aplicação sem incluir ferramentas de desenvolvimento desnecessárias na imagem final.

#### Scenario: Execução do container backend
- **WHEN** a imagem do backend é construída e iniciada com PostgreSQL saudável e variáveis válidas
- **THEN** a aplicação inicia na porta configurada e disponibiliza sua documentação OpenAPI
