# Instrucoes para o Codex

## OpenSpec no Windows

Ao executar comandos do OpenSpec dentro do Codex no Windows, utilizar:

npx.cmd openspec

Nao utilizar diretamente:

openspec
npm
npx

Quando necessario, utilizar:

npm.cmd
npx.cmd

## SDD

Este projeto utiliza Spec-Driven Development com OpenSpec.

Regras:

1. O PRD.md e a fonte de verdade do produto.
2. Nao implementar funcionalidades fora da change ativa.
3. Manter apenas uma change principal ativa por vez.
4. Revisar proposal, design e tasks antes da implementacao.
5. Executar testes antes de finalizar uma change.
6. Nao realizar merge automaticamente.
7. Changes concluidas devem ser arquivadas antes da proxima etapa principal.