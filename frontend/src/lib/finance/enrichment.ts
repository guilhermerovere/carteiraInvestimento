import type { Position, PortfolioSummary, ValuedPosition } from "./contracts";

export type EnrichedPosition = Position & { valuation: ValuedPosition | null };

export function enrichPositionsByAssetId(positions: Position[], summary: PortfolioSummary | undefined): EnrichedPosition[] {
  const valuations = new Map((summary?.posicoes ?? []).map((position) => [position.ativoId, position]));
  return positions.map((position) => ({ ...position, valuation: valuations.get(position.ativoId) ?? null }));
}
