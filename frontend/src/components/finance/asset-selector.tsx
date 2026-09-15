"use client";

import { useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { financeApi, FinanceApiError } from "@/client/finance-api";
import { ptBrError } from "@/client/presentation-errors";
import type { Asset } from "@/lib/finance/contracts";
import { EntityLogo } from "./entity-logo";
import { Button } from "@/components/ui/button";

export const catalogKeys = { assets: ["catalog", "assets"] as const, brokers: ["catalog", "brokers"] as const };
type ExactState = "idle" | "missing" | "inactive" | "unavailable";

export function AssetSelector({ value, onChange }: { value: Asset | null; onChange: (asset: Asset, message?: string) => void }) {
  const queryClient = useQueryClient();
  const [query, setQuery] = useState("");
  const [market, setMarket] = useState<"B3" | "US">("B3");
  const [exact, setExact] = useState<ExactState>("idle");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const assets = useQuery({ queryKey: [...catalogKeys.assets, query], queryFn: () => financeApi.assets(query, 0, 20), retry: false });
  const discovery = useQuery({ queryKey: ["catalog", "asset-discovery", query], queryFn: () => financeApi.discoverB3(query), enabled: query.trim().length >= 2, retry: false });

  async function precheck(candidate = query.trim()) {
    setBusy(true);
    setError("");
    try {
      const found = await financeApi.exactAsset(candidate);
      if (found.ativo) {
        onChange(found);
        setExact("idle");
      } else setExact("inactive");
    } catch (failure) {
      if (failure instanceof FinanceApiError && failure.status === 404) setExact("missing");
      else {
        setExact("unavailable");
        setError(ptBrError(failure, "asset-validation"));
      }
    } finally {
      setBusy(false);
    }
  }

  function selectDiscovery(ticker: string) {
    setQuery(ticker);
    void precheck(ticker);
  }

  async function register() {
    setBusy(true);
    setError("");
    try {
      const asset = await financeApi.registerAsset(query.trim(), market);
      await queryClient.invalidateQueries({ queryKey: catalogKeys.assets });
      onChange(asset, asset.ticker !== query.trim().toUpperCase() ? `${query.trim().toUpperCase()} agora é negociado como ${asset.ticker}.` : undefined);
    } catch (failure) {
      if (failure instanceof FinanceApiError && failure.status === 409) {
        try {
          const asset = await financeApi.exactAsset(query.trim());
          onChange(asset, "O ativo canônico já havia sido cadastrado.");
          return;
        } catch { /* present the safe original conflict below */ }
      }
      if (failure instanceof FinanceApiError && failure.status === 502) setExact("unavailable");
      setError(ptBrError(failure, "asset-validation"));
    } finally {
      setBusy(false);
    }
  }

  return <div className="selector" aria-label="Selecionar ativo">
    <label className="operation-field">
      <span>Buscar ativo</span>
      <div className="selector__search">
        <input value={query} onChange={(event) => { setQuery(event.target.value); setExact("idle"); setError(""); }} placeholder="Ticker ou nome" />
        <Button variant="secondary" onClick={() => void precheck()} disabled={!query.trim() || busy}>{busy ? "Verificando..." : "Verificar ticker"}</Button>
      </div>
    </label>
    {error && <p role="alert" className="operation-error">{error}</p>}
    {assets.isPending ? <p role="status">Buscando ativos...</p> : assets.isError ? <p role="alert">Não foi possível carregar o catálogo.</p> :
      <div role="listbox" aria-label="Ativos encontrados" className="selector__list">
        {assets.data.items.map((asset) => <button type="button" role="option" aria-selected={value?.id === asset.id} key={asset.id} onClick={() => onChange(asset)}>
          <EntityLogo provider={asset.logoProvider} reference={asset.logoReference} label={asset.ticker} />
          <span><strong>{asset.ticker}</strong><small>{asset.nome}</small><small>{asset.mercado} · {asset.moeda}</small></span>
        </button>)}
      </div>}
    {exact === "inactive" && <p role="alert">Este ticker existe, mas está inativo e não pode ser selecionado para compra.</p>}
    {discovery.data && discovery.data.length > 0 && <div role="listbox" aria-label="Sugestões da B3" className="selector__list">
      {discovery.data.map((result) => <button type="button" role="option" aria-selected={value?.ticker === result.ticker} key={result.ticker} onClick={() => selectDiscovery(result.ticker)}>
        <span><strong>{result.ticker}</strong><small>Resultado da B3</small></span>
      </button>)}
    </div>}
    {exact === "unavailable" && <div className="registration-card" aria-live="polite">
      <strong>Não foi possível validar este ativo agora.</strong>
      <p>O ticker {query.trim().toUpperCase()} foi preservado. Tente novamente em alguns instantes.</p>
      <Button variant="secondary" onClick={() => void precheck()} disabled={busy}>{busy ? "Tentando..." : "Tentar novamente"}</Button>
    </div>}
    {exact === "missing" && <div className="registration-card">
      <strong>Cadastrar ticker {query.trim().toUpperCase()}</strong>
      <p>O servidor validará os dados financeiros e criará o ativo no catálogo global.</p>
      <label>Mercado<select value={market} onChange={(event) => setMarket(event.target.value as "B3" | "US")}><option>B3</option><option>US</option></select></label>
      <Button onClick={register} disabled={busy}>{busy ? "Validando..." : "Cadastrar ativo"}</Button>
    </div>}
  </div>;
}
