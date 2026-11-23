## 1. Pre-flight e inventario

- [x] 1.1 Reconfirmar a branch `feature/jacoco-coverage-gate`, registrar `git status --short --branch`, confirmar com `npx.cmd openspec list --json` que `establish-jacoco-coverage-gate` e a unica change ativa e interromper se houver troca de branch ou outra change principal.
- [x] 1.2 Recontar fontes e casos descobertos pelo Surefire/Failsafe, conferir os sete `*IntegrationTest` executados pelo Surefire, os 32 `*IT` executados pelo Failsafe e o unico QA live opt-in; verificar a classificacao pelos XMLs em `target/surefire-reports` e `target/failsafe-reports`, sem renomear testes nesta change.

## 2. Instrumentacao e linha de base sem gate

- [x] 2.1 Adicionar ao `backend/pom.xml` a versao JaCoCo `0.8.15`, propriedades separadas de `argLine`, configuracao explicita do Surefire/Failsafe, agentes para as duas fases, merge em `post-integration-test` e report em `verify`, ainda sem o goal `check` e sem qualquer exclusao; verificar no effective POM que cada runner recebe o agente correto e que o report ocorre depois das integracoes.
- [x] 2.2 Executar `.\mvnw.cmd clean verify` a partir de `backend/`, com Docker disponivel e sem QA live, confirmar sucesso de todos os testes deterministas, existencia e tamanho nao zero dos dois arquivos de execucao e do `jacoco.exec` consolidado, e confirmar a geracao de `target/site/jacoco/index.html`, `jacoco.xml` e `jacoco.csv`.
- [x] 2.3 Extrair do bundle em `jacoco.xml` os contadores `missed`, `covered`, total e razao para `LINE` e `BRANCH`, registrar essa linha de base antes de adicionar o gate e rejeitar a medicao se os relatorios Maven mostrarem integracoes omitidas, falhas inesperadas ou execucao parcial sem PostgreSQL/Testcontainers.

## 3. Lacunas relevantes e disciplina de testes

- [x] 3.1 Ordenar pacotes/classes por branches perdidos e revisar primeiro autenticacao/autorizacao, alteracoes de estado financeiro, calculos, validacoes de fronteira, idempotencia, concorrencia, rollback e tratamento de falhas; selecionar somente lacunas com comportamento observavel do PRD e verificar que nenhum candidato e apenas getter, construtor trivial ou chamada sem assercao.
- [x] 3.2 Para comportamento existente que mereca protecao, adicionar testes de caracterizacao com assercoes relevantes e executar cada teste direcionado contra a producao inalterada; registrar que esses testes caracterizam comportamento e nao representam Red -> Green -> Refactor, inclusive quando forem motivados por uma linha ou branch descoberta pelo JaCoCo.
- [x] 3.3 Classificar qualquer falha que exija mudanca de producao antes de agir: se for ambiente, fixture ou expectativa incorreta, corrigi-la sem rotulo TDD; se revelar comportamento novo ou defeito real, revisar o escopo e somente registrar Red -> Green -> Refactor quando houver evidencia cronologica do teste falhando pelo motivo esperado, da menor correcao verde e da refatoracao com toda a suite verde; se nao houver caso qualificavel, registrar explicitamente que nenhum ciclo TDD ocorreu.

## 4. Calibracao e ativacao do gate

- [x] 4.1 Reexecutar `.\mvnw.cmd clean verify` a partir de `backend/`, ainda sem gate e apos os testes pertinentes, confirmar novamente a suite completa e calcular a medicao final de linhas e branches pelo XML; definir cada minimo como a respectiva razao final arredondada para baixo ao ponto percentual inteiro, sem copiar limites da Aula 15 nem reduzir o piso para acomodar falha.
- [x] 4.2 Adicionar propriedades Maven para os minimos medidos e o goal JaCoCo `check` em `verify`, com regras `BUNDLE/COVEREDRATIO` separadas para `LINE` e `BRANCH` e zero exclusoes; executar `.\mvnw.cmd clean verify` a partir de `backend/` e verificar `BUILD SUCCESS` com relatorio e gate sobre o arquivo consolidado.
- [x] 4.3 Executar uma demonstracao negativa com override temporario de branch estritamente superior a medicao, verificar que os testes passam mas o `check` reprova por cobertura, e em seguida executar novamente com os valores versionados para confirmar `BUILD SUCCESS` sem modificar ou desativar testes.

## 5. Documentacao, revisao e validacao

- [x] 5.1 Documentar no README o comando Windows `mvnw.cmd clean verify`, a contagem final das suites, os contadores/razoes medidos, os dois pisos escolhidos, os caminhos HTML/XML/CSV e a diferenca entre teste aprovado e politica de cobertura aprovada; verificar que nao foi criada nem proposta colecao Postman ou Insomnia.
- [x] 5.2 Revisar o diff e o estado do Git, confirmar ausencia de mudancas em runtime, migrations, contratos, frontend e artefatos Graphify alem do estado preexistente registrado no pre-flight, e executar `git diff --check`; nao fazer staging, commit, push, merge ou troca de branch.
- [x] 5.3 Executar `npx.cmd openspec validate establish-jacoco-coverage-gate --strict` e `npx.cmd openspec status --change establish-jacoco-coverage-gate`, corrigir qualquer inconsistencia de proposal/design/tasks e verificar que a change permanece com `skip_specs: true` e todos os artefatos de planejamento prontos para arquivamento posterior a implementacao.
