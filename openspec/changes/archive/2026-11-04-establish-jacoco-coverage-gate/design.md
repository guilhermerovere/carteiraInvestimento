## Context

See [proposal.md](proposal.md) for motivation. O backend e um unico modulo Maven em Java 21. O `pom.xml` declara o Failsafe para `integration-test` e `verify`, mas nao declara JaCoCo nem Surefire explicitamente. A arvore atual possui 85 fontes em `src/test/java`: 45 arquivos `*Test`, sete arquivos `*IntegrationTest`, 32 arquivos `*IT` e `PostgreSqlContainerSupport`.

Pelas convencoes Maven atuais, os sete `*IntegrationTest` sao descobertos pelo Surefire apesar de serem integracoes por semantica, enquanto os 32 `*IT` sao descobertos pelo Failsafe. Os relatorios existentes registram 212 casos Surefire e 111 casos Failsafe; somente `BrokerLiveRegistrationQaIT` fica ignorado por exigir ativacao explicita de QA live. O gate precisa agregar os dois processos Maven, sem confundir fase de execucao com nivel de teste.

O PRD exige PostgreSQL real quando o comportamento depender dele. Portanto, uma linha de base obtida sem Docker, com integracoes omitidas ou com QA externo live habilitado nao e valida para calibrar o gate. A Aula 15 do `suporteos2025` e a unica referencia academica desta change: ela orienta medir antes de fixar o piso, avaliar linhas e ramos e tratar cobertura como diagnostico, nao como prova de qualidade.

## Goals / Non-Goals

**Goals:**

- Coletar dados independentes das fases Surefire e Failsafe, consolida-los depois das integracoes e gerar um unico relatorio navegavel e auditavel.
- Medir `LINE` e `BRANCH` no bundle completo de classes de producao, sem exclusoes, e reprovar `verify` quando qualquer piso versionado nao for atendido.
- Calibrar os pisos somente a partir de execucoes completas e estaveis e manter os valores visiveis como propriedades Maven.
- Usar lacunas de cobertura para selecionar testes de comportamento pelo risco do PRD e preservar a distincao historica entre caracterizacao e TDD.

**Non-Goals:**

- Estabelecer antecipadamente 80%, 100% ou qualquer outro numero sem medicao deste projeto.
- Medir frontend, testes manuais, Postman, Insomnia, QA live ou cobertura por chamadas artificiais.
- Adicionar exclusoes de classes, pacotes, DTOs, configuracoes ou codigo considerado trivial para elevar o percentual.
- Renomear em massa os sete `*IntegrationTest`, reorganizar toda a suite ou introduzir CI externo nesta change.
- Corrigir silenciosamente comportamento de produto descoberto durante a auditoria; uma correcao que altere contrato exige revisao explicita do escopo antes da mudanca de producao.

## Decisions

### 1. Separar a coleta Surefire/Failsafe e consolidar antes do relatorio

O JaCoCo `0.8.15`, versao empregada pela Aula 15, sera declarado por propriedade Maven. Uma execucao `prepare-agent` gravara os testes do Surefire em um arquivo de execucao unitario e uma execucao `prepare-agent-integration` gravara o Failsafe em outro arquivo. Cada agente usara uma `propertyName` propria, consumida por `maven-surefire-plugin` e `maven-failsafe-plugin` com avaliacao tardia de `argLine`.

No `post-integration-test`, o goal `merge` combinara explicitamente apenas esses dois arquivos no `jacoco.exec`. No `verify`, depois do Failsafe, `report` e posteriormente `check` consumirao o arquivo consolidado. O plugin JaCoCo ficara declarado depois do Failsafe para tornar explicita a ordem das execucoes ligadas a `verify`.

Alternativas rejeitadas:

- Um unico `prepare-agent` e um unico arquivo sem merge: e menor, mas torna menos auditavel se o segundo processo de testes sobrescreveu ou deixou de acrescentar dados do primeiro.
- Relatorios separados sem consolidacao: dificultariam definir um piso unico para o build e poderiam duplicar a interpretacao de classes exercitadas nos dois niveis.
- Somente Surefire: omitiria os 32 arquivos `*IT` e nao representaria a suite real.

### 2. Introduzir JaCoCo em duas etapas: medicao e gate

Na primeira etapa, o POM tera instrumentacao, merge e report, mas ainda nao tera limites ativos. Sera executado `mvnw.cmd clean verify` em ambiente com Docker, e os relatorios Surefire/Failsafe serao conferidos para provar que a suite esperada participou. Os contadores `missed` e `covered` de `LINE` e `BRANCH` no bundle do `jacoco.xml` serao usados para calcular `covered / (missed + covered)`.

Depois da analise e de eventuais testes relevantes, uma segunda execucao limpa confirmara a medicao final. Cada minimo versionado sera a razao final arredondada para baixo ao ponto percentual inteiro: por exemplo, `0,8734` gera piso `0.87`. Isso cria pequena margem contra ruido sem escolher um numero arbitrario abaixo da realidade. Os valores medidos e os pisos escolhidos serao registrados na documentacao do build.

Alternativas rejeitadas:

- Copiar `0.80` para linhas e `0.55` para branches da Aula 15: esses numeros pertencem a linha de base daquele repositorio, nao a este projeto.
- Fixar 80% antes da primeira execucao: poderia falhar por uma divida desconhecida ou aceitar regressao relevante caso a suite atual ja esteja acima do numero.
- Usar o percentual exibido visualmente no HTML: o XML fornece contadores inteiros reproduziveis e evita erro de leitura ou arredondamento.

### 3. Aplicar duas regras de bundle, uma para linhas e outra para branches

O goal `check` usara `element=BUNDLE`, `value=COVEREDRATIO` e limites separados para `LINE` e `BRANCH`, parametrizados por propriedades Maven. O relatorio padrao manterá HTML para investigacao humana e XML/CSV para auditoria automatizada. Nenhum `<exclude>` sera adicionado ao agente, ao relatorio ou ao check.

O gate sera demonstrado de duas formas: `clean verify` deve passar com os pisos versionados; uma execucao com override temporario de branch estritamente superior a medicao deve falhar no `check`, seguida de nova execucao com a configuracao versionada. Se a cobertura de branches for 100%, a demonstracao negativa devera usar uma regressao controlada somente de configuracao e nunca alterar ou desativar testes.

### 4. Priorizar lacunas por comportamento e risco, nao por cor do relatorio

O relatorio sera ordenado primeiro por branches perdidos e depois cruzado com os riscos do PRD: autenticacao/autorizacao, operacoes que alteram estado financeiro, calculos, validacoes de fronteira, idempotencia, concorrencia, rollback e tratamento de falhas. Uma lacuna so gera teste quando existir uma observacao publica ou uma regra relevante; getters, construtores ou chamadas sem assercao nao serao exercitados apenas para aumentar cobertura.

Testes que documentam comportamento ja implementado serao registrados como caracterizacao, mesmo se escritos antes de qualquer refatoracao de teste. Um ciclo so recebera o rotulo Red -> Green -> Refactor quando houver: (1) teste falhando pelo motivo esperado antes da mudanca de producao, (2) menor mudanca de producao que o torna verde e (3) refatoracao posterior com a suite verde. Uma falha que apenas revela ambiente, fixture ou expectativa incorreta nao conta como Red de TDD.

### 5. Preservar a taxonomia atual durante esta change

Os sete `*IntegrationTest` continuarao sendo executados pelo Surefire nesta change; a cobertura consolidada inclui seus dados, e os relatorios Maven documentam onde cada suite rodou. Renomea-los para `*IT` mudaria tempos e ordem do ciclo Maven sem ser necessario ao gate e merece uma change separada se houver interesse.

O QA live permanece opt-in e fora da linha de base porque depende de servico externo e propriedade de sistema. As integracoes deterministicas com PostgreSQL/Testcontainers sao obrigatorias para a medicao; uma execucao parcial nao pode definir nem atualizar os pisos.

## Risks / Trade-offs

- [O `argLine` do agente pode ser perdido ou sobrescrito por configuracao Maven] -> Usar propriedades distintas, avaliacao tardia e confirmar que os dois arquivos `.exec` existem e sao nao vazios antes do merge.
- [O relatorio pode ser gerado antes do Failsafe terminar] -> Ligar merge a `post-integration-test`, manter report/check em `verify` depois do plugin Failsafe e conferir os contadores dos dois diretorios de relatorio.
- [A nomenclatura `*IntegrationTest` sugere Failsafe, mas roda no Surefire] -> Registrar a diferenca e validar a agregacao; nao inferir nivel de teste apenas pelo arquivo `.exec`.
- [Docker indisponivel produz uma linha de base parcial] -> Nao calibrar o gate sem a suite PostgreSQL completa e relatorios Maven sem falhas inesperadas.
- [Cobertura pode incentivar testes artificiais] -> Exigir assercoes comportamentais, priorizacao por risco e revisao de cada teste novo; manter zero exclusoes JaCoCo.
- [Um piso exato demais pode oscilar com pequenas mudancas legitimas] -> Arredondar para baixo ao ponto percentual inteiro e repetir a medicao limpa antes de versionar o gate.
- [Uma lacuna pode revelar defeito real fora do escopo de tooling] -> Parar, registrar a evidencia e revisar a change antes de alterar producao; nao chamar caracterizacao de TDD retroativamente.

## Migration Plan

1. Adicionar somente coleta, merge e relatorio JaCoCo, mantendo o check ausente.
2. Executar a suite completa com `clean verify`, validar descoberta Surefire/Failsafe e registrar a linha de base de linhas e branches.
3. Investigar lacunas de maior risco e adicionar testes de caracterizacao pertinentes; somente desenvolver correcao real apos revisao de escopo e com ciclo TDD observavel.
4. Repetir a medicao limpa, calcular os pisos e adicionar o `check` com propriedades versionadas.
5. Demonstrar falha controlada acima da linha de base, restaurar os valores versionados e obter `BUILD SUCCESS` na suite completa.
6. Documentar comando, relatorios, valores medidos e limites; revisar o diff, preservar Graphify e validar a change estritamente.

Rollback remove as execucoes/propriedades JaCoCo e os testes/documentacao exclusivos desta change. Nao ha migracao de dados, mudanca de runtime ou passo de deploy.
