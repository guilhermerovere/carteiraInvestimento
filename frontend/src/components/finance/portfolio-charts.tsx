"use client";

import Decimal from "decimal.js-light";
import { Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { usePortfolioEvolution } from "@/client/portfolio-queries";
import type { PortfolioEvolutionPoint, ValuedPosition } from "@/lib/finance/contracts";
import { formatMoney } from "@/lib/finance/format";
import { ProblemDetailAlert } from "./states";

const DONUT_COLORS = ["var(--primary)", "var(--info)", "var(--success)", "var(--warning)", "var(--ring)", "var(--negative)"];

function drawingNumber(value: string): number {
  return new Decimal(value).toNumber();
}

function dateLabel(value: string): string {
  const [year, month, day] = value.split("-");
  return day && month && year ? `${day}/${month}` : value;
}

type EvolutionDrawPoint = PortfolioEvolutionPoint & { investedDraw: number; resultDraw: number; label: string };
function EvolutionChart() {
  const evolution = usePortfolioEvolution();
  if (evolution.isPending) return <ChartState title="Evolução patrimonial" message="Carregando evolução patrimonial." />;
  if (evolution.isError) return <section className="portfolio-chart-card" aria-labelledby="evolution-title"><ChartHeading id="evolution-title" title="Evolução patrimonial" description="Histórico materializado da carteira." /><ProblemDetailAlert compact error={evolution.error} onRetry={() => evolution.refetch()} /></section>;
  if (!evolution.data?.length) return <ChartState title="Evolução patrimonial" message="Ainda não há snapshots com valuation materializado para exibir." />;
  const data: EvolutionDrawPoint[] = evolution.data.map((point) => ({ ...point, label: dateLabel(point.dataReferencia), investedDraw: drawingNumber(point.totalInvestidoBrl), resultDraw: drawingNumber(point.resultadoNaoRealizadoBrl) }));
  return <section className="portfolio-chart-card" aria-labelledby="evolution-title">
    <ChartHeading id="evolution-title" title="Evolução patrimonial" description="Snapshots históricos com valuation materializado." />
    <div className="portfolio-chart" role="img" aria-label="Gráfico de barras de valor investido e resultado não realizado por data">
      <ResponsiveContainer width="100%" height="100%"><BarChart data={data} margin={{ top: 12, right: 4, left: 4, bottom: 0 }}><CartesianGrid vertical={false} stroke="var(--border)" strokeOpacity={0.7} /><XAxis dataKey="label" tickLine={false} axisLine={false} tick={{ fill: "var(--muted-foreground)" }} /><YAxis tickLine={false} axisLine={false} tick={{ fill: "var(--muted-foreground)" }} tickFormatter={(value) => formatMoney(String(value))} width={72} /><Tooltip content={<EvolutionTooltip />} /><Legend /><Bar dataKey="investedDraw" name="Valor investido" fill="var(--primary)" radius={[4, 4, 0, 0]} /><Bar dataKey="resultDraw" name="Resultado não realizado" radius={[4, 4, 0, 0]}>{data.map((point) => <Cell key={point.dataReferencia} fill={point.resultDraw < 0 ? "var(--negative)" : "var(--info)"} />)}</Bar></BarChart></ResponsiveContainer>
    </div>
    <ul className="sr-only">{evolution.data.map((point) => <li key={point.dataReferencia}>{point.dataReferencia}: valor investido {formatMoney(point.totalInvestidoBrl)}; resultado não realizado {formatMoney(point.resultadoNaoRealizadoBrl)}.</li>)}</ul>
  </section>;
}

function EvolutionTooltip({ active, payload }: { active?: boolean; payload?: Array<{ payload: EvolutionDrawPoint }> }) {
  const point = active ? payload?.[0]?.payload : undefined;
  if (!point) return null;
  return <div className="portfolio-chart-tooltip"><strong>{point.dataReferencia}</strong><span>Valor investido: {formatMoney(point.totalInvestidoBrl)}</span><span>Resultado não realizado: {formatMoney(point.resultadoNaoRealizadoBrl)}</span></div>;
}

function ChartHeading({ id, title, description }: { id: string; title: string; description: string }) {
  return <div className="portfolio-chart-card__heading"><div><h2 id={id}>{title}</h2><p>{description}</p></div></div>;
}
function ChartState({ title, message }: { title: string; message: string }) {
  return <section className="portfolio-chart-card" aria-label={title}><ChartHeading id={title.toLowerCase().replaceAll(" ", "-")} title={title} description={message} /><p className="portfolio-chart-state" role="status">{message}</p></section>;
}

function CompositionChart({ positions }: { positions: ValuedPosition[] }) {
  const actionPositions = positions.filter((position) => position.tipo === "ACAO");
  const eligible = actionPositions.filter((position) => position.valorAtualBrl != null);
  if (!eligible.length) return <ChartState title="Composição por ação" message={actionPositions.length ? "Composição indisponível enquanto os valores atuais das ações não estiverem disponíveis." : "Não há ações elegíveis para compor o gráfico."} />;
  const total = eligible.reduce((sum, position) => sum.plus(position.valorAtualBrl!), new Decimal(0));
  const data = eligible.map((position) => {
    const valueBrl = position.valorAtualBrl!;
    return { ticker: position.ticker, value: drawingNumber(valueBrl), valueBrl, percentage: total.isZero() ? "0,0%" : new Decimal(valueBrl).div(total).times(100).toFixed(1).replace(".", ",") + "%" };
  });
  return <section className="portfolio-chart-card" aria-labelledby="composition-title">
    <ChartHeading id="composition-title" title="Composição por ação" description="Participação atual por ticker (apenas ações)." />
    {eligible.length !== actionPositions.length && <p className="portfolio-chart-notice" role="status">Composição parcial: ações sem valor atual não foram incluídas.</p>}
    <div className="portfolio-chart portfolio-chart--donut" role="img" aria-label="Gráfico de composição atual por ação">
      <ResponsiveContainer width="100%" height="100%"><PieChart><Pie data={data} dataKey="value" nameKey="ticker" innerRadius="62%" outerRadius="84%" paddingAngle={3} stroke="var(--card)" strokeWidth={2}>{data.map((entry, index) => <Cell key={entry.ticker} fill={DONUT_COLORS[index % DONUT_COLORS.length]} />)}</Pie><Tooltip content={<CompositionTooltip />} /></PieChart></ResponsiveContainer>
      <span className="portfolio-chart__donut-total" aria-hidden="true"><strong>{formatMoney(total.toFixed(2))}</strong><small>Total</small></span>
    </div>
    <ul className="portfolio-chart-legend">{data.map((point, index) => <li key={point.ticker}><i style={{ background: DONUT_COLORS[index % DONUT_COLORS.length] }} aria-hidden="true" /><span>{point.ticker}</span><strong>{formatMoney(point.valueBrl)}</strong><em>{point.percentage}</em></li>)}</ul>
  </section>;
}
function CompositionTooltip({ active, payload }: { active?: boolean; payload?: Array<{ payload: { ticker: string; valueBrl: string; percentage: string } }> }) {
  const point = active ? payload?.[0]?.payload : undefined;
  return point ? <div className="portfolio-chart-tooltip"><strong>{point.ticker}</strong><span>{formatMoney(point.valueBrl)} · {point.percentage}</span></div> : null;
}

export function PortfolioCharts({ positions }: { positions: ValuedPosition[] }) {
  return <section className="portfolio-charts" aria-label="Gráficos da carteira"><EvolutionChart /><CompositionChart positions={positions} /></section>;
}
