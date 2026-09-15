# Frontend Foundation Specification

## Purpose

Estabelece uma aplicação web mínima e executável sobre a stack frontend obrigatória, pronta para receber capacidades futuras sem antecipar telas de negócio.

## Requirements

### Requirement: Base executável do frontend
O repositório SHALL disponibilizar o frontend em `frontend/` como projeto Node independente, usando Next.js 15 ou superior com App Router, TypeScript e Tailwind CSS. O frontend SHALL possuir `frontend/package.json` e `frontend/package-lock.json` próprios, enquanto os manifests Node da raiz permanecem dedicados ao tooling do repositório/OpenSpec e todos os diretórios `node_modules` permanecem ignorados pelo Git.

#### Scenario: Build do frontend
- **WHEN** as dependências de `frontend/package-lock.json` são instaladas e o comando de build é executado em `frontend/`
- **THEN** a aplicação App Router em TypeScript compila sem erros e produz um build Next.js executável sem alterar os manifests Node da raiz

#### Scenario: Separação dos projetos Node
- **WHEN** os manifests Node da raiz e de `frontend/` são inspecionados
- **THEN** o tooling do repositório/OpenSpec permanece na raiz e as dependências Next.js pertencem somente ao projeto frontend independente

#### Scenario: Página inicial de fundação
- **WHEN** um cliente acessa a raiz do frontend em execução
- **THEN** recebe uma página mínima de fundação sem telas ou fluxos funcionais de negócio

### Requirement: Habilitadores da interface futura
O frontend SHALL deixar disponíveis e compatíveis os habilitadores previstos no PRD para componentes Shadcn UI, gráficos Recharts e estado remoto TanStack Query, criando apenas a configuração ou o scaffolding técnico mínimo necessário para comprovar que a stack funciona. Esta change MUST NOT criar dashboards, autenticação, componentes funcionais de negócio ou consumo funcional de APIs de negócio.

#### Scenario: Verificação das dependências de base
- **WHEN** o manifesto e a configuração do frontend são inspecionados
- **THEN** os habilitadores de UI, gráficos e consultas remotas estão instalados ou inicializados conforme necessário para uso por changes futuras

### Requirement: Configuração externa do endpoint do backend
 O frontend SHALL obter a URL interna do backend pela variável server-only `BACKEND_API_URL` e a origem canônica pública pela variável server-only `APP_ORIGIN`. O frontend MUST NOT depender funcionalmente de `NEXT_PUBLIC_API_URL`, incorporar URL de produção no código, expor `BACKEND_API_URL` ao browser ou exibir a URL interna do backend na página de fundação.

#### Scenario: URL configurada por ambiente
- **WHEN** o frontend é executado com `BACKEND_API_URL` e `APP_ORIGIN` válidas
- **THEN** somente código executado no servidor usa esses valores para comunicação com o backend e validação de origem

#### Scenario: Inspeção da página pública
- **WHEN** um cliente acessa a página pública de fundação
- **THEN** a resposta não revela `BACKEND_API_URL` nem depende de `NEXT_PUBLIC_API_URL`

### Requirement: Imagem executável do frontend
O frontend SHALL possuir uma definição de imagem Docker capaz de instalar dependências reproduzíveis, gerar o build e executar a aplicação na porta configurada.

#### Scenario: Execução do container frontend
- **WHEN** a imagem do frontend é construída e iniciada com a configuração documentada
- **THEN** a página mínima responde na porta exposta sem depender de telas de negócio
