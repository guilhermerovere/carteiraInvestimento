"use client";

import { ArrowRight, BarChart3, Banknote, CircleDollarSign, Clock3, LineChart, RefreshCw, TrendingUp, WalletCards, type LucideIcon } from "lucide-react";
import Link from "next/link";
import { useRef, useState, type ReactNode } from "react";
import Decimal from "decimal.js-light";
import { useCashBalance, usePortfolioSummary, usePositions, useRefreshPortfolioSummary } from "@/client/portfolio-queries";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { EntityLogo } from "./entity-logo";
import { EmptyState, FinancialSkeleton, ProblemDetailAlert } from "./states";
import { AveragePrice, GainLoss, Money, Quantity } from "./values";
import { formatCurrency } from "@/lib/finance/format";
import { PortfolioCharts } from "./portfolio-charts";

function decimalValue(value: string | null | undefined) {
  if (value == null) return undefined;
  try { return new Decimal(value); } catch { return undefined; }
}

function nonNegative(value: Decimal) {
  return value.isNegative() ? new Decimal(0) : value;
}

function boundedPercent(value: Decimal) {
  if (value.isNegative()) return new Decimal(0);
  return value.greaterThan(100) ? new Decimal(100) : value;
}

function percentOf(value: string | null | undefined, total: string | null | undefined) {
  const numerator = decimalValue(value);
  const denominator = decimalValue(total);
  if (!numerator || !denominator || !denominator.greaterThan(0)) return undefined;
  return boundedPercent(numerator.dividedBy(denominator).times(100));
}

function percentLabel(percent: Decimal) {
  return `${percent.toFixed(1).replace(".", ",")}%`;
}

function MetricComposition({ cash, positions }: { cash: string | null; positions: string | null }) {
  const cashValue = decimalValue(cash);
  const positionsValue = decimalValue(positions);
  if (!cashValue || !positionsValue) return <div className="current-metric current-metric--neutral">Composição indisponível</div>;
  const total = cashValue.plus(positionsValue);
  if (!total.greaterThan(0)) return <div className="current-metric current-metric--neutral">Sem patrimônio distribuído</div>;
  const cashPercent = nonNegative(cashValue.dividedBy(total).times(100));
  const positionsPercent = nonNegative(positionsValue.dividedBy(total).times(100));
  return <div className="current-metric current-composition" role="img" aria-label={`Composição atual: caixa ${cashPercent.toFixed(1)} por cento e posições ${positionsPercent.toFixed(1)} por cento.`}>
    <div className="current-composition__bar" aria-hidden="true"><span className="current-composition__cash" style={{ width: `${cashPercent.toFixed(3)}%` }} /><span className="current-composition__positions" style={{ width: `${positionsPercent.toFixed(3)}%` }} /></div>
    <div className="current-composition__legend"><span>Caixa</span><span>Posições</span></div>
  </div>;
}

function MetricProportion({ value, total, noun }: { value: string | null; total: string | null; noun: string }) {
  const percent = percentOf(value, total);
  if (!percent) return <div className="current-metric current-metric--neutral">Sem patrimônio para calcular</div>;
  return <div className="current-metric current-proportion" role="img" aria-label={`${percent.toFixed(1)} por cento do patrimônio em ${noun}.`}>
    <div className="current-proportion__bar" aria-hidden="true"><span style={{ width: `${percent.toFixed(3)}%` }} /></div>
    <small>{percentLabel(percent)} do patrimônio em {noun}</small>
  </div>;
}

function MetricComparison({ invested, current }: { invested: string | null; current: string | null }) {
  const investedValue = decimalValue(invested);
  const currentValue = decimalValue(current);
  if (!investedValue || !currentValue) return <div className="current-metric current-metric--neutral">Comparação indisponível</div>;
  const maximum = nonNegative(investedValue.greaterThan(currentValue) ? investedValue : currentValue);
  const widthFor = (value: Decimal) => maximum.isZero() ? new Decimal(0) : boundedPercent(value.dividedBy(maximum).times(100));
  const rows = [{ label: "Investido", value: investedValue, className: "current-comparison__invested" }, { label: "Atual", value: currentValue, className: "current-comparison__current" }];
  return <div className="current-metric current-comparison" role="img" aria-label={`Comparação atual: Investido ${invested}, Atual ${current}.`}>
    {rows.map((row) => <div className="current-comparison__row" key={row.label}><div className="current-comparison__heading"><span>{row.label}</span><small><Money value={row.value.toFixed(2)} /></small></div><div className="current-comparison__track" aria-hidden="true"><i className={row.className} style={{ width: `${widthFor(row.value).toFixed(3)}%` }} /></div></div>)}
  </div>;
}

function UnrealizedStateVisual({ state }: { state: ReturnType<typeof resultState> }) {
  const stateName = state?.tone ?? "neutral";
  return <div className={`current-metric current-result current-result--${stateName}`} role="img" aria-label={`Resultado não realizado: ${state?.label ?? "Indisponível"}.`}>
    <div className="current-result__track" aria-hidden="true"><span>−</span><i /><span>0</span><b /><span>+</span></div>
    <small>{state?.label ?? "Resultado indisponível"}</small>
  </div>;
}

function MetricAccent({ icon: Icon }: { icon: LucideIcon }) {
  return <span className="metric-accent" aria-hidden="true"><Icon /></span>;
}

function resultState(value: string | null): { label: string; tone: "positive" | "negative" | "neutral" } | undefined {
  if (value == null) return undefined;
  const decimal = new Decimal(value);
  return decimal.isPositive() ? { label: "Resultado positivo", tone: "positive" } : decimal.isNegative() ? { label: "Resultado negativo", tone: "negative" } : { label: "Resultado neutro", tone: "neutral" };
}

function Metric({ label, value, icon, accent, tone, supporting, visual }: { label: string; value: string | null; icon: LucideIcon; accent?: "blue" | "green" | "purple"; tone?: "positive" | "negative"; supporting?: string; visual?: ReactNode }) {
  const classes = ["portfolio-metric", accent ? `portfolio-metric--${accent}` : "", tone ? `portfolio-metric--${tone}` : ""].filter(Boolean).join(" ");
  return <Card className={classes}><CardContent><div className="portfolio-metric__heading"><span>{label}</span><MetricAccent icon={icon} /></div>{value == null ? <strong>Indisponível</strong> : <Money value={value} />}{supporting && <small className="portfolio-metric__supporting">{supporting}</small>}{visual}</CardContent></Card>;
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
  const unrealizedState = resultState(data.lucroNaoRealizadoBrl);
  return <section className="summary-page reference-page" aria-labelledby="portfolio-summary-title">
    <p className="sr-only" aria-live="polite">{announcement}</p>
    <header className="reference-page__header"><div><h2 id="portfolio-summary-title">Resumo da Carteira</h2><p>Acompanhe o desempenho dos seus investimentos, de forma simples e objetiva.</p></div><div className="reference-page__updated"><Clock3 aria-hidden="true" /><span>Última atualização<strong>{new Date(data.valuationInstant).toLocaleString("pt-BR")}</strong></span><Button variant="secondary" onClick={updateMarket} disabled={refresh.isPending}><RefreshCw className={refresh.isPending ? "is-spinning" : undefined} aria-hidden="true" />Atualizar</Button></div></header>
    {refresh.isError && <ProblemDetailAlert compact error={refresh.error} onRetry={updateMarket} />}
    {!data.cotacoesDisponiveis && <p role="status" className="operation-message">Não foi possível atualizar as cotações agora.</p>}
    <div className="summary-metrics">
      <Card className="wealth-hero"><CardContent><div className="portfolio-metric__heading"><span>Patrimônio total</span><MetricAccent icon={WalletCards} /></div>{data.patrimonioTotalBrl == null ? <strong className="wealth-hero__value">Indisponível</strong> : <><Money value={data.patrimonioTotalBrl} className="wealth-hero__value" />{data.lucroNaoRealizadoBrl != null && <GainLoss value={data.lucroNaoRealizadoBrl} percentage={data.rentabilidadeNaoRealizadaPercentual} />}</>}<MetricComposition cash={data.saldoCaixaBrl} positions={data.valorPosicoesBrl} /></CardContent></Card>
      <Metric label="Saldo em caixa" value={data.saldoCaixaBrl} icon={Banknote} supporting="Disponível para investir" visual={<MetricProportion value={data.saldoCaixaBrl} total={data.patrimonioTotalBrl} noun="caixa" />} />
      <Metric label="Total investido" value={data.totalInvestidoBrl} icon={BarChart3} accent="blue" visual={<MetricComparison invested={data.totalInvestidoBrl} current={data.valorPosicoesBrl} />} />
      <Metric label="Valor das posições" value={data.valorPosicoesBrl} icon={LineChart} accent="green" visual={<MetricProportion value={data.valorPosicoesBrl} total={data.patrimonioTotalBrl} noun="ativos" />} />
      <Metric label="Resultado realizado" value={data.lucroRealizadoAcumuladoBrl} icon={CircleDollarSign} accent="purple" supporting="Resultado acumulado com vendas" />
      <Metric label="Resultado não realizado" value={data.lucroNaoRealizadoBrl} icon={TrendingUp} tone={unrealizedState?.tone === "negative" ? "negative" : unrealizedState?.tone === "positive" ? "positive" : undefined} visual={<UnrealizedStateVisual state={unrealizedState} />} />
    </div>
    <PortfolioCharts positions={data.posicoes} />
    <section className="reference-table-card" aria-labelledby="positions-preview-title"><div className="reference-table-card__heading"><div><h2 id="positions-preview-title">Minhas posições</h2><p>Visão geral dos ativos que compõem sua carteira.</p></div><Link href="/carteira/posicoes">Ver todos os ativos <ArrowRight aria-hidden="true" /></Link></div>{positions.isPending ? <FinancialSkeleton rows={2} label="Carregando prévia de posições" /> : positions.data?.items.length ? <div className="reference-table-wrap"><table className="reference-table"><thead><tr><th>Ativo</th><th>Quantidade</th><th>Preço médio</th><th>Preço atual</th><th>Valor da posição</th><th>Resultado / Rentabilidade</th></tr></thead><tbody>{positions.data.items.map((position) => { const quote = valuation.get(position.ativoId); return <tr key={position.id}><td><span className="table-asset"><EntityLogo provider={position.logoProvider} reference={position.logoReference} label={position.ticker} /><span><strong>{position.ticker}</strong><small>{position.nome}</small></span></span></td><td><Quantity value={position.quantidade} /></td><td><AveragePrice value={position.precoMedioBrl} /></td><td>{quote?.cotacaoAtual != null ? formatCurrency(quote.cotacaoAtual, position.moeda) : "—"}</td><td>{quote?.valorAtualBrl != null ? <Money value={quote.valorAtualBrl} /> : "—"}</td><td>{quote?.lucroNaoRealizadoBrl != null ? <GainLoss value={quote.lucroNaoRealizadoBrl} percentage={quote.rentabilidadePercentual} /> : "—"}</td></tr>; })}</tbody></table></div> : <EmptyState title="Nenhuma posição aberta" description="Quando houver ativos em custódia, eles aparecerão aqui." />}<footer className="reference-table-card__footer"><span>Total de posições <strong>{positions.data?.totalElements ?? 0}</strong></span><span>Valor total das posições <Money value={data.valorPosicoesBrl} /></span></footer></section><aside className="sr-only"><WalletCards aria-hidden="true" />Valores confirmados pelo servidor.</aside>
  </section>;
}

function PersistedPortfolioFallback({ positions, cash, onRetry }: { positions: ReturnType<typeof usePositions>; cash: ReturnType<typeof useCashBalance>; onRetry: () => void }) {
  const persisted = positions.data?.items ?? [];
  const invested = persisted.reduce((total, position) => total.plus(position.totalInvestidoBrl), new Decimal(0)).toFixed(2);
  const realized = persisted.reduce((total, position) => total.plus(position.lucroRealizadoAcumuladoBrl), new Decimal(0)).toFixed(2);
  return <section className="summary-page reference-page" aria-labelledby="portfolio-summary-title">
    <header className="reference-page__header"><div><h2 id="portfolio-summary-title">Resumo da Carteira</h2><p>Os dados persistidos continuam disponíveis.</p></div><Button variant="secondary" onClick={onRetry}>Atualizar</Button></header>
    <p role="status" className="operation-message">Não foi possível atualizar as cotações agora.</p>
    <div className="summary-metrics"><Metric label="Saldo em caixa" value={cash.data?.saldoCaixaBrl ?? "0.00"} icon={Banknote} supporting="Disponível para investir" /><Metric label="Total investido" value={invested} icon={BarChart3} /><Card className="portfolio-metric"><CardContent><div className="portfolio-metric__heading"><span>Valor das posições</span><MetricAccent icon={LineChart} /></div><strong>Indisponível</strong></CardContent></Card><Metric label="Resultado realizado" value={realized} icon={CircleDollarSign} /><Card className="portfolio-metric"><CardContent><div className="portfolio-metric__heading"><span>Resultado não realizado</span><MetricAccent icon={TrendingUp} /></div><strong>Indisponível</strong></CardContent></Card></div>
    <section className="reference-table-card"><div className="reference-table-card__heading"><div><h2>Minhas posições</h2><p>Custódia e custo médio confirmados.</p></div></div>{positions.isPending ? <FinancialSkeleton rows={2} label="Carregando posições" /> : <div className="reference-table-wrap"><table className="reference-table"><thead><tr><th>Ativo</th><th>Quantidade</th><th>Preço médio</th><th>Preço atual</th></tr></thead><tbody>{persisted.map(position => <tr key={position.id}><td><strong>{position.ticker}</strong></td><td><Quantity value={position.quantidade} /></td><td><AveragePrice value={position.precoMedioBrl} currency={position.moeda} /></td><td>Indisponível</td></tr>)}</tbody></table></div>}</section>
  </section>;
}
