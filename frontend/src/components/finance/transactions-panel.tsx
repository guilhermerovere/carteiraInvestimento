"use client";

import { useState } from "react";
import { useTransactions } from "@/client/portfolio-queries";
import { Badge } from "@/components/ui/badge";
import { EmptyState, FinancialSkeleton, ProblemDetailAlert } from "./states";
import { Pagination } from "./pagination";
import { GainLoss, Money, Quantity } from "./values";
import { EntityLogo } from "./entity-logo";
import { formatCurrency } from "@/lib/finance/format";

export function TransactionsPanel() {
  const [page, setPage] = useState(0);
  const transactions = useTransactions(page, 20);

  if (transactions.isPending && !transactions.data) {
    return <FinancialSkeleton rows={6} label="Carregando transações" />;
  }

  const history = transactions.data;
  if (!history) {
    return <ProblemDetailAlert error={transactions.error} message="Não foi possível carregar o histórico de transações agora." onRetry={() => transactions.refetch()} />;
  }

  return (
    <section className="reference-page transactions-page" aria-labelledby="transactions-title">
      <header className="reference-page__header">
        <div><h2 id="transactions-title">Transações</h2><p>Histórico confirmado das suas operações financeiras.</p></div>
      </header>
      {transactions.isError && <ProblemDetailAlert compact error={transactions.error} message="Não foi possível atualizar o histórico agora. Os dados exibidos foram preservados." onRetry={() => transactions.refetch()} />}
      {history.items.length === 0 ? <EmptyState title="Nenhuma transação ainda" description="Suas compras e vendas confirmadas aparecerão aqui." /> : (
        <section className="reference-table-card">
          <div className="reference-table-wrap">
            <table className="reference-table">
              <thead><tr><th>Data</th><th>Ativo</th><th>Tipo</th><th>Quantidade</th><th>Preço unitário</th><th>Taxas</th><th>Valor total</th><th>Corretora</th><th>Resultado realizado</th></tr></thead>
              <tbody>{history.items.map((transaction) => <tr key={transaction.id}>
                <td>{new Date(transaction.dataNegociacao).toLocaleDateString("pt-BR")}</td>
                <td><span className="table-asset"><EntityLogo provider={transaction.logoProvider ?? null} reference={transaction.logoReference ?? null} label={transaction.ticker} /><span><strong>{transaction.ticker}</strong><small>{transaction.nome ?? "Ativo negociado"}</small></span></span></td>
                <td><Badge className={"transaction-type transaction-type--" + transaction.tipo.toLowerCase()}>{transaction.tipo === "BUY" ? "Compra" : "Venda"}</Badge></td>
                <td><Quantity value={transaction.quantidade} /></td>
                <td>{formatCurrency(transaction.precoUnitario, transaction.moeda)}</td>
                <td><Money value={transaction.taxas} /></td>
                <td><Money value={transaction.valorTotalBrl} /></td>
                <td><span className="transaction-broker">{transaction.corretoraNome ?? "Corretora registrada"}</span></td>
                <td>{transaction.resultadoRealizadoBrl == null ? "—" : <GainLoss value={transaction.resultadoRealizadoBrl} realized />}</td>
              </tr>)}</tbody>
            </table>
          </div>
        </section>
      )}
      <Pagination page={history.page} totalPages={history.totalPages} onPageChange={setPage} disabled={transactions.isFetching} />
    </section>
  );
}
