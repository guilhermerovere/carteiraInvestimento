"use client";

import { ArrowRight, RefreshCw, ShieldCheck, Wallet } from "lucide-react";
import Link from "next/link";
import { useRef, useState } from "react";
import { usePortfolioSummary, useRefreshPortfolioSummary } from "@/client/portfolio-queries";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { FinancialSkeleton, EmptyState, ProblemDetailAlert } from "./states";
import { GainLoss, Money } from "./values";
import { MarketFreshness } from "./market-freshness";
import { FinancialPageIntro } from "./page-intro";

function Metric({ label, value, hint }: { label: string; value: string; hint: string }) {
  return <div className="secondary-metric"><span>{label}</span><Money value={value} /><small>{hint}</small></div>;
}

export function SummaryPanel() {
  const summary = usePortfolioSummary();
  const refresh = useRefreshPortfolioSummary();
  const [announcement, setAnnouncement] = useState("");
  const refreshInFlight = useRef(false);

  if (summary.isPending) return <FinancialSkeleton rows={5} label="Carregando resumo da carteira" />;
  if (summary.isError) return <ProblemDetailAlert error={summary.error} onRetry={() => summary.refetch()} />;
  const data = summary.data;

  async function updateMarket() {
    if (refresh.isPending || refreshInFlight.current) return;
    refreshInFlight.current = true;
    setAnnouncement("Atualizando dados de mercado.");
    try {
      await refresh.mutateAsync();
      setAnnouncement("Dados de mercado atualizados.");
    } catch {
      setAnnouncement("Não foi possível atualizar os dados de mercado.");
    } finally {
      refreshInFlight.current = false;
    }
  }

  return (
    <section className="summary-page" aria-labelledby="portfolio-summary-title">
      <FinancialPageIntro
        title="Seu patrimônio, com contexto"
        description="Uma leitura precisa da sua carteira em reais, com origem e horário de cada dado de mercado."
        action={<Button onClick={updateMarket} disabled={refresh.isPending}><RefreshCw className={refresh.isPending ? "is-spinning" : undefined} aria-hidden="true" />{refresh.isPending ? "Atualizando…" : "Atualizar mercado"}</Button>}
      />
      <p className="sr-only" aria-live="polite">{announcement}</p>
      {refresh.isError && <ProblemDetailAlert compact error={refresh.error} onRetry={updateMarket} />}

      <Card className="wealth-hero">
        <CardContent>
          <div className="wealth-hero__copy">
            <span id="portfolio-summary-title">Patrimônio total</span>
            <Money value={data.patrimonioTotalBrl} className="wealth-hero__value" />
            <div className="wealth-hero__performance">
              <GainLoss value={data.lucroNaoRealizadoBrl} percentage={data.rentabilidadeNaoRealizadaPercentual} />
              <span>Resultado não realizado</span>
            </div>
          </div>
          <div className="wealth-hero__trust">
            <ShieldCheck aria-hidden="true" />
            <div><strong>Visão consolidada em BRL</strong><span>Dados servidos por uma sessão protegida.</span></div>
          </div>
        </CardContent>
      </Card>

      <MarketFreshness summary={data} />

      <div className="metric-band" aria-label="Composição patrimonial">
        <Metric label="Saldo em caixa" value={data.saldoCaixaBrl} hint="Disponível em BRL" />
        <Metric label="Total investido" value={data.totalInvestidoBrl} hint="Custo materializado" />
        <Metric label="Valor das posições" value={data.valorPosicoesBrl} hint="Valuation atual" />
        <Metric label="Resultado realizado" value={data.lucroRealizadoAcumuladoBrl} hint="Acumulado histórico" />
      </div>

      <section className="preview-section" aria-labelledby="positions-preview-title">
        <div className="section-heading">
          <div><span>Posições abertas</span><h2 id="positions-preview-title">Onde seu patrimônio está</h2></div>
          <Link href="/carteira/posicoes">Ver todas <ArrowRight aria-hidden="true" /></Link>
        </div>
        {data.posicoes.length === 0 ? (
          <EmptyState title="Nenhuma posição aberta" description="Quando houver ativos em custódia, eles aparecerão aqui." />
        ) : (
          <div className="position-preview-list">
            {data.posicoes.slice(0, 4).map((position) => (
              <Card key={position.ativoId} className="position-preview">
                <CardContent>
                  <div className="asset-symbol"><span>{position.ticker.slice(0, 2)}</span><div><strong>{position.ticker}</strong><small>{position.mercado} · {position.moeda}</small></div></div>
                  <div className="position-preview__value"><Money value={position.valorAtualBrl} /><GainLoss value={position.lucroNaoRealizadoBrl} percentage={position.rentabilidadePercentual} /></div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </section>
      <aside className="cash-note"><Wallet aria-hidden="true" /><p><strong>Caixa e custódia têm autoridades distintas.</strong><span>Esta visão não estima nem recalcula valores no navegador.</span></p></aside>
    </section>
  );
}
