import { Clock3 } from "lucide-react";
import type { PortfolioSummary } from "@/lib/finance/contracts";
import { DateTime } from "./values";

export function MarketFreshness({ summary }: { summary: PortfolioSummary }) {
  return (
    <div className="freshness">
      <span className="freshness__primary"><Clock3 aria-hidden="true" /> Carteira calculada em <DateTime value={summary.valuationInstant} /></span>
      <details>
        <summary>Ver origem e horários</summary>
        <div className="freshness__details">
          {summary.posicoes.map((position) => (
            <p key={position.ativoId}>
              <strong>{position.ticker}</strong>: cotação de <DateTime value={position.instanteCotacao} /> · {position.providerCotacao.replaceAll("_", " ")}
            </p>
          ))}
          {summary.cambioAtual && (
            <p><strong>USD/BRL</strong>: câmbio de <DateTime value={summary.cambioAtual.instanteCambio} /> · {summary.cambioAtual.provider.replaceAll("_", " ")}</p>
          )}
          <p className="freshness__note">Os horários de cálculo, cotação e câmbio podem ser diferentes.</p>
        </div>
      </details>
    </div>
  );
}
