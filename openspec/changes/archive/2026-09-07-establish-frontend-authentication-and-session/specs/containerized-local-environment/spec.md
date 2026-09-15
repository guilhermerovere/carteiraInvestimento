## MODIFIED Requirements

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
