"use client";

import { ArrowRight, Clock3, RefreshCw, WalletCards } from "lucide-react";
import Link from "next/link";
import { useRef, useState } from "react";
import Decimal from "decimal.js-light";
import { useCashBalance, usePortfolioSummary, usePositions, useRefreshPortfolioSummary } from "@/client/portfolio-queries";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { EntityLogo } from "./entity-logo";
import { EmptyState, FinancialSkeleton, ProblemDetailAlert } from "./states";
import { AveragePrice, GainLoss, Money, Quantity } from "./values";
import { formatCurrency } from "@/lib/finance/format";

function Metric({ label, value, tone }: { label: string; value: string | null; tone?: "positive" }) {
  return <Card className={"portfolio-metric" + (tone ? " portfolio-metric--" + tone : "")}><CardContent><span>{label}</span>{value == null ? <strong>Indisponível</strong> : <Money value={value} />}</CardContent></Card>;
}

export function SummaryPanel() {
  const summary = usePortfolioSummary();
  const positions = usePositions(0, 5);
  const cash = useCashBalance();
  const refresh = useRefreshPortfolioSummary();
  const [announcement, setAnnouncement] = useState("");
  const refreshInFlight = useRef(false);
  if (summary.isPending) return <FinancialSkeleton rows={5} label="Carregando resumo da carteira" />;
  if (summary.isError) return <><ProblemDetailAlert compact error={summary.error} onRetry={() => summary.refetch()} /><PersistedPortfolioFallback positions={positions} cash={cash} onRetry={() => summary.refetch()} /></>;
  const data = summary.data;
  async function updateMarket() {
    if (refresh.isPending || refreshInFlight.current) return;
    refreshInFlight.current = true; setAnnouncement("Atualizando dados de mercado.");
    try { await refresh.mutateAsync(); setAnnouncement("Dados de mercado atualizados."); }
    catch { setAnnouncement("Não foi possível atualizar os dados de mercado."); }
    finally { refreshInFlight.current = false; }
  }
  const valuation = new Map(data.posicoes.map((item) => [item.ativoId, item]));
  return <section className="summary-page reference-page" aria-labelledby="portfolio-summary-title">
    <p className="sr-only" aria-live="polite">{announcement}</p>
    <header className="reference-page__header"><div><h2 id="portfolio-summary-title">Resumo da Carteira</h2><p>Acompanhe o desempenho dos seus investimentos, de forma simples e objetiva.</p></div><div className="reference-page__updated"><Clock3 aria-hidden="true" /><span>Última atualização<strong>{new Date(data.valuationInstant).toLocaleString("pt-BR")}</strong></span><Button variant="secondary" onClick={updateMarket} disabled={refresh.isPending}><RefreshCw className={refresh.isPending ? "is-spinning" : undefined} aria-hidden="true" />Atualizar</Button></div></header>
    {refresh.isError && <ProblemDetailAlert compact error={refresh.error} onRetry={updateMarket} />}
    {!data.cotacoesDisponiveis && <p role="status" className="operation-message">Não foi possível atualizar as cotações agora.</p>}
    <div className="summary-metrics"><Card className="wealth-hero"><CardContent><span>Patrimônio total</span>{data.patrimonioTotalBrl == null ? <strong className="wealth-hero__value">Indisponível</strong> : <><Money value={data.patrimonioTotalBrl} className="wealth-hero__value" />{data.lucroNaoRealizadoBrl != null && <GainLoss value={data.lucroNaoRealizadoBrl} percentage={data.rentabilidadeNaoRealizadaPercentual} />}</>}</CardContent></Card><Metric label="Saldo em caixa" value={data.saldoCaixaBrl} /><Metric label="Total investido" value={data.totalInvestidoBrl} /><Metric label="Valor das posições" value={data.valorPosicoesBrl} /><Metric label="Resultado realizado" value={data.lucroRealizadoAcumuladoBrl} /><Metric label="Resultado não realizado" value={data.lucroNaoRealizadoBrl} tone="positive" /></div>
    <section className="reference-table-card" aria-labelledby="positions-preview-title"><div className="reference-table-card__heading"><div><h2 id="positions-preview-title">Minhas posições</h2><p>Visão geral dos ativos que compõem sua carteira.</p></div><Link href="/carteira/posicoes">Ver todos os ativos <ArrowRight aria-hidden="true" /></Link></div>{positions.isPending ? <FinancialSkeleton rows={2} label="Carregando prévia de posições" /> : positions.data?.items.length ? <div className="reference-table-wrap"><table className="reference-table"><thead><tr><th>Ativo</th><th>Quantidade</th><th>Preço médio</th><th>Preço atual</th><th>Valor da posição</th><th>Resultado / Rentabilidade</th></tr></thead><tbody>{positions.data.items.map((position) => { const quote = valuation.get(position.ativoId); return <tr key={position.id}><td><span className="table-asset"><EntityLogo provider={position.logoProvider} reference={position.logoReference} label={position.ticker} /><span><strong>{position.ticker}</strong><small>{position.nome}</small></span></span></td><td><Quantity value={position.quantidade} /></td><td><AveragePrice value={position.precoMedioBrl} /></td><td>{quote ? formatCurrency(quote.cotacaoAtual, position.moeda) : "—"}</td><td>{quote ? <Money value={quote.valorAtualBrl} /> : "—"}</td><td>{quote ? <GainLoss value={quote.lucroNaoRealizadoBrl} percentage={quote.rentabilidadePercentual} /> : "—"}</td></tr>; })}</tbody></table></div> : <EmptyState title="Nenhuma posição aberta" description="Quando houver ativos em custódia, eles aparecerão aqui." />}<footer className="reference-table-card__footer"><span>Total de posições <strong>{positions.data?.totalElements ?? 0}</strong></span><span>Valor total das posições <Money value={data.valorPosicoesBrl} /></span></footer></section><aside className="sr-only"><WalletCards aria-hidden="true" />Valores confirmados pelo servidor.</aside>
  </section>;
}

function PersistedPortfolioFallback({ positions, cash, onRetry }: { positions: ReturnType<typeof usePositions>; cash: ReturnType<typeof useCashBalance>; onRetry: () => void }) {
  const persisted = positions.data?.items ?? [];
  const invested = persisted.reduce((total, position) => total.plus(position.totalInvestidoBrl), new Decimal(0)).toFixed(2);
  const realized = persisted.reduce((total, position) => total.plus(position.lucroRealizadoAcumuladoBrl), new Decimal(0)).toFixed(2);
  return <section className="summary-page reference-page" aria-labelledby="portfolio-summary-title">
    <header className="reference-page__header"><div><h2 id="portfolio-summary-title">Resumo da Carteira</h2><p>Os dados persistidos continuam disponíveis.</p></div><Button variant="secondary" onClick={onRetry}>Atualizar</Button></header>
    <p role="status" className="operation-message">Não foi possível atualizar as cotações agora.</p>
    <div className="summary-metrics"><Metric label="Saldo em caixa" value={cash.data?.saldoCaixaBrl ?? "0.00"} /><Metric label="Total investido" value={invested} /><Card className="portfolio-metric"><CardContent><span>Valor das posições</span><strong>Indisponível</strong></CardContent></Card><Metric label="Resultado realizado" value={realized} /><Card className="portfolio-metric"><CardContent><span>Resultado não realizado</span><strong>Indisponível</strong></CardContent></Card></div>
    <section className="reference-table-card"><div className="reference-table-card__heading"><div><h2>Minhas posições</h2><p>Custódia e custo médio confirmados.</p></div></div>{positions.isPending ? <FinancialSkeleton rows={2} label="Carregando posições" /> : <div className="reference-table-wrap"><table className="reference-table"><thead><tr><th>Ativo</th><th>Quantidade</th><th>Preço médio</th><th>Preço atual</th></tr></thead><tbody>{persisted.map(position => <tr key={position.id}><td><strong>{position.ticker}</strong></td><td><Quantity value={position.quantidade} /></td><td><AveragePrice value={position.precoMedioBrl} currency={position.moeda} /></td><td>Indisponível</td></tr>)}</tbody></table></div>}</section>
  </section>;
}
