"use client";

import { ArrowDownToLine, ArrowUpFromLine, Wallet } from "lucide-react";
import { useState } from "react";
import { useCashBalance, useCashMovements } from "@/client/portfolio-queries";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { EmptyState, FinancialSkeleton, ProblemDetailAlert } from "./states";
import { Pagination } from "./pagination";
import { FinancialPageIntro } from "./page-intro";
import { DateTime, Money } from "./values";

export function CashPanel() {
  const [page, setPage] = useState(0);
  const balance = useCashBalance();
  const movements = useCashMovements(page, 20);

  return (
    <section aria-labelledby="cash-title">
      <FinancialPageIntro title="Caixa e movimentações" description="Saldo atual em BRL e histórico de depósitos e saques já confirmados." />
      {balance.isPending ? <FinancialSkeleton rows={1} label="Carregando saldo em caixa" /> : balance.isError ? (
        <ProblemDetailAlert error={balance.error} onRetry={() => balance.refetch()} />
      ) : (
        <Card className="cash-balance">
          <CardContent><div><Wallet aria-hidden="true" /><span>Saldo atual</span></div><Money value={balance.data.saldoCaixaBrl} /></CardContent>
        </Card>
      )}

      <div className="cash-history-heading"><span>Histórico</span><h2 id="cash-title">Movimentações de caixa</h2></div>
      {movements.isPending ? <FinancialSkeleton rows={5} label="Carregando movimentações" /> : movements.isError ? (
        <ProblemDetailAlert error={movements.error} onRetry={() => movements.refetch()} />
      ) : movements.data.items.length === 0 ? (
        <EmptyState title="Sem movimentações" description="Nenhum depósito ou saque confirmado aparece no histórico." />
      ) : (
        <>
          <div className="movement-list">
            {movements.data.items.map((movement) => {
              const isDeposit = movement.tipo === "DEPOSITO";
              const Icon = isDeposit ? ArrowDownToLine : ArrowUpFromLine;
              return (
                <Card key={movement.id} className="movement-row">
                  <CardContent>
                    <span className="movement-row__icon"><Icon aria-hidden="true" /></span>
                    <div><Badge>{isDeposit ? "Depósito" : "Saque"}</Badge><strong>{movement.descricao ?? (isDeposit ? "Depósito em conta" : "Saque da conta")}</strong><DateTime value={movement.dataHora} /></div>
                    <Money value={(isDeposit ? "" : "-") + movement.valorBrl.replace(/^-/, "")} />
                  </CardContent>
                </Card>
              );
            })}
          </div>
          <Pagination page={movements.data.page} totalPages={movements.data.totalPages} onPageChange={setPage} disabled={movements.isFetching} />
        </>
      )}
    </section>
  );
}
