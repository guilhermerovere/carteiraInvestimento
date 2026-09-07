## MODIFIED Requirements

### Requirement: Configuração externa do endpoint do backend
O frontend SHALL obter a URL interna do backend pela variável server-only `BACKEND_API_URL` e a origem canônica pública pela variável server-only `APP_ORIGIN`. O frontend MUST NOT depender funcionalmente de `NEXT_PUBLIC_API_URL`, incorporar URL de produção no código, expor `BACKEND_API_URL` ao browser ou exibir a URL interna do backend na página de fundação.

#### Scenario: URL configurada por ambiente
- **WHEN** o frontend é executado com `BACKEND_API_URL` e `APP_ORIGIN` válidas
- **THEN** somente código executado no servidor usa esses valores para comunicação com o backend e validação de origem

#### Scenario: Inspeção da página pública
- **WHEN** um cliente acessa a página pública de fundação
- **THEN** a resposta não revela `BACKEND_API_URL` nem depende de `NEXT_PUBLIC_API_URL`
