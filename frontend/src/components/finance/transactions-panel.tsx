"use client";

import { ChevronDown } from "lucide-react";
import { useState } from "react";
import { useTransactions } from "@/client/portfolio-queries";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { EmptyState, FinancialSkeleton, ProblemDetailAlert } from "./states";
import { Pagination } from "./pagination";
import { FinancialPageIntro } from "./page-intro";
import { DateTime, GainLoss, Money, Quantity } from "./values";
import type { Transaction } from "@/lib/finance/contracts";

function TransactionTypeBadge({ type }: { type: Transaction["tipo"] }) {
  return <Badge className={"transaction-type transaction-type--" + type.toLowerCase()}>{type === "BUY" ? "BUY · Compra" : "SELL · Venda"}</Badge>;
}

function TransactionDetails({ transaction }: { transaction: Transaction }) {
  return (
    <details className="transaction-details">
      <summary>Ver detalhes <ChevronDown aria-hidden="true" /></summary>
      <dl>
        <div><dt>Quantidade</dt><dd><Quantity value={transaction.quantidade} /></dd></div>
        <div><dt>Preço unitário</dt><dd>{transaction.moeda} <Quantity value={transaction.precoUnitario} /></dd></div>
        <div><dt>Taxas em BRL</dt><dd><Money value={transaction.taxas} /></dd></div>
        <div><dt>FX histórico</dt><dd><Quantity value={transaction.taxaCambioBrl} /> BRL</dd></div>
        <div><dt>Identificador da corretora</dt><dd><code>{transaction.corretoraId}</code></dd></div>
        {transaction.exchangeRateId && <div><dt>Identificador do câmbio</dt><dd><code>{transaction.exchangeRateId}</code></dd></div>}
        <div><dt>Negociação</dt><dd><DateTime value={transaction.dataNegociacao} /></dd></div>
        <div><dt>Registro</dt><dd><DateTime value={transaction.dataRegistro} /></dd></div>
      </dl>
    </details>
  );
}

export function TransactionsPanel() {
  const [page, setPage] = useState(0);
  const transactions = useTransactions(page, 20);
  if (transactions.isPending) return <FinancialSkeleton rows={6} label="Carregando transações" />;
  if (transactions.isError) return <ProblemDetailAlert error={transactions.error} onRetry={() => transactions.refetch()} />;
  return (
    <section aria-labelledby="transactions-title">
      <FinancialPageIntro titleId="transactions-title" title="Transações confirmadas" description="Histórico imutável de compras e vendas, sem controles de edição." />
      {transactions.data.items.length === 0 ? (
        <EmptyState title="Sem transações" description="Nenhuma compra ou venda confirmada aparece no histórico." />
      ) : (
        <div className="transaction-list">
          {transactions.data.items.map((transaction) => (
            <Card key={transaction.id} className="transaction-row">
              <CardContent>
                <div className="transaction-row__main">
                  <TransactionTypeBadge type={transaction.tipo} />
                  <div><strong>{transaction.ticker}</strong><DateTime value={transaction.dataNegociacao} /></div>
                </div>
                <div className="transaction-row__amount">
                  <Money value={transaction.valorTotalBrl} />
                  {transaction.resultadoRealizadoBrl != null ? <GainLoss value={transaction.resultadoRealizadoBrl} realized /> : <span className="transaction-row__pending">Sem resultado realizado</span>}
                </div>
                <TransactionDetails transaction={transaction} />
              </CardContent>
            </Card>
          ))}
        </div>
      )}
      <Pagination page={transactions.data.page} totalPages={transactions.data.totalPages} onPageChange={setPage} disabled={transactions.isFetching} />
    </section>
  );
}
