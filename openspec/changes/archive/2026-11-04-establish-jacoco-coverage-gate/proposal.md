## Why

O backend possui uma suite ampla de testes unitarios e de integracao, mas o build Maven ainda nao mede cobertura nem impede regressoes de linhas e ramos. A change estabelece uma linha de base observada e um gate JaCoCo reproduzivel, usando a Aula 15 do `suporteos2025` como unica referencia academica e sem confundir quantidade ou cobertura de testes com qualidade comportamental.

## What Changes

- Configura o JaCoCo no Maven para coletar e consolidar a execucao real do Surefire e do Failsafe e gerar relatorios HTML, XML e CSV com cobertura de linhas e branches.
- Executa primeiro um build completo em modo de medicao, sem gate, confere a descoberta das duas suites e registra a linha de base real antes de escolher qualquer minimo.
- Analisa lacunas por risco do PRD e acrescenta somente testes com assercoes comportamentais relevantes; nao usa exclusoes cosmeticas, chamadas artificiais, remocao de testes ou reducao de limite para melhorar percentuais.
- Fixa os minimos iniciais de linhas e branches somente depois da medicao e das correcoes de cobertura justificadas, arredondando cada razao alcancada para baixo ao ponto percentual inteiro para criar um piso estavel contra regressao.
- Associa relatorio e verificacao ao ciclo `verify`, demonstra tanto o sucesso com os limites versionados quanto a falha controlada com um limite temporariamente superior a linha de base e documenta os caminhos dos artefatos gerados.
- Classifica testes adicionados sobre comportamento ja existente como caracterizacao. Registra Red -> Green -> Refactor apenas quando um comportamento novo ou uma correcao real for desenvolvido nessa ordem e houver evidencia das tres etapas.
- Nao cria colecao Postman ou Insomnia e nao altera APIs, contratos HTTP, comportamento de runtime, schema de banco, frontend ou pipelines externos.

## Capabilities

### New Capabilities

Nenhuma. A change introduz tooling de medicao e politica interna de qualidade do build, nao uma capacidade do produto.

### Modified Capabilities

Nenhuma. Os requisitos existentes do PRD e das specs continuam inalterados; a change apenas mede e protege a evidencia automatizada desses comportamentos.

A change declara `skip_specs: true` porque nao existe comportamento normativo novo ou modificado que justifique uma delta spec. Criar um requisito de produto apenas para representar configuracao JaCoCo confundiria tooling com capacidade funcional.

## Impact

- Maven: `backend/pom.xml`, com JaCoCo e coordenacao explicita entre Surefire e Failsafe.
- Testes: suite atual de 85 fontes Java, semanticamente composta por testes unitarios, slices e integracoes; a descoberta atual distribui 212 casos no Surefire e 111 no Failsafe, com um QA live ignorado por configuracao.
- Evidencias: `backend/target/site/jacoco/`, arquivos de execucao JaCoCo e relatorios Maven de testes, todos gerados pelo build e nao versionados.
- Possiveis testes adicionais: somente para lacunas relevantes identificadas no relatorio, priorizando seguranca, alteracoes de estado financeiro, calculos, fronteiras e tratamento de falhas do PRD.
- Sem impacto de deploy, migration, API ou compatibilidade de runtime.
