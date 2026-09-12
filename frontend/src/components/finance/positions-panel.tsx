"use client";

import { ChevronDown } from "lucide-react";
import { useState } from "react";
import { usePortfolioSummary, usePositions } from "@/client/portfolio-queries";
import { enrichPositionsByAssetId } from "@/lib/finance/enrichment";
import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { EmptyState, FinancialSkeleton, ProblemDetailAlert } from "./states";
import { Pagination } from "./pagination";
import { FinancialPageIntro } from "./page-intro";
import { DateTime, GainLoss, Money, Quantity } from "./values";

function Unavailable() { return <span className="unavailable">Indisponível</span>; }

function PositionDetails({ position }: { position: ReturnType<typeof enrichPositionsByAssetId>[number] }) {
  return (
    <details className="row-disclosure">
      <summary>Detalhes <ChevronDown aria-hidden="true" /></summary>
      <dl>
        <div><dt>Preço médio</dt><dd><Money value={position.precoMedioBrl} /></dd></div>
        <div><dt>Total investido</dt><dd><Money value={position.totalInvestidoBrl} /></dd></div>
        <div><dt>Cotação atual</dt><dd>{position.valuation ? <><span>{position.valuation.moeda}</span> <Quantity value={position.valuation.cotacaoAtual} /></> : <Unavailable />}</dd></div>
        <div><dt>Provider</dt><dd>{position.valuation?.providerCotacao.replaceAll("_", " ") ?? <Unavailable />}</dd></div>
        <div><dt>Custódia atualizada</dt><dd><DateTime value={position.ultimaAtualizacao} /></dd></div>
        <div><dt>Cotação observada</dt><dd>{position.valuation ? <DateTime value={position.valuation.instanteCotacao} /> : <Unavailable />}</dd></div>
      </dl>
    </details>
  );
}

export function PositionsPanel() {
  const [page, setPage] = useState(0);
  const positions = usePositions(page, 20);
  const summary = usePortfolioSummary();

  if (positions.isPending) return <FinancialSkeleton rows={7} label="Carregando posições" />;
  if (positions.isError) return <ProblemDetailAlert error={positions.error} onRetry={() => positions.refetch()} />;
  if (positions.data.items.length === 0) return <section aria-labelledby="positions-title"><FinancialPageIntro titleId="positions-title" title="Suas posições" description="Custódia aberta, enriquecida pela última avaliação disponível." /><EmptyState title="Sem posições abertas" description="Não há ativos em custódia para exibir." /></section>;

  const enriched = enrichPositionsByAssetId(positions.data.items, summary.data);
  return (
    <section aria-labelledby="positions-title">
      <FinancialPageIntro titleId="positions-title" title="Suas posições" description="Custódia aberta e valuation atual são exibidos como fontes distintas." />
      {summary.isError && <ProblemDetailAlert compact error={summary.error} onRetry={() => summary.refetch()} />}

      <Card className="desktop-position-table">
        <Table aria-label="Posições abertas">
          <TableHeader><TableRow><TableHead>Ativo</TableHead><TableHead>Mercado</TableHead><TableHead>Quantidade</TableHead><TableHead>Valor atual</TableHead><TableHead>Resultado</TableHead><TableHead>Detalhes</TableHead></TableRow></TableHeader>
          <TableBody>
            {enriched.map((position) => (
              <TableRow key={position.id}>
                <TableCell><strong className="ticker">{position.ticker}</strong></TableCell>
                <TableCell>{position.valuation?.mercado ?? <Unavailable />}</TableCell>
                <TableCell><Quantity value={position.quantidade} /></TableCell>
                <TableCell>{position.valuation ? <Money value={position.valuation.valorAtualBrl} /> : <Unavailable />}</TableCell>
                <TableCell>{position.valuation ? <GainLoss value={position.valuation.lucroNaoRealizadoBrl} percentage={position.valuation.rentabilidadePercentual} /> : <Unavailable />}</TableCell>
                <TableCell><PositionDetails position={position} /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Card>

      <div className="mobile-position-list">
        {enriched.map((position) => (
          <Card key={position.id} className="position-card">
            <CardContent>
              <div className="position-card__header"><div><strong>{position.ticker}</strong><span>{position.valuation?.mercado ?? "Mercado indisponível"}</span></div><span><Quantity value={position.quantidade} /> un.</span></div>
              <div className="position-card__value"><span>Valor atual</span>{position.valuation ? <Money value={position.valuation.valorAtualBrl} /> : <Unavailable />}</div>
              {position.valuation ? <GainLoss value={position.valuation.lucroNaoRealizadoBrl} percentage={position.valuation.rentabilidadePercentual} /> : <p className="position-card__notice">Valuation não atualizado para este ativo.</p>}
              <PositionDetails position={position} />
            </CardContent>
          </Card>
        ))}
      </div>
      <Pagination page={positions.data.page} totalPages={positions.data.totalPages} onPageChange={setPage} disabled={positions.isFetching} />
    </section>
  );
}
