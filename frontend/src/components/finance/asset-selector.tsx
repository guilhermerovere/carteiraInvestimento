"use client";

import { useRef, useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { financeApi, FinanceApiError } from "@/client/finance-api";
import { ptBrError } from "@/client/presentation-errors";
import type { Asset, AssetDiscovery, Market } from "@/lib/finance/contracts";
import { EntityLogo } from "./entity-logo";
import { Button } from "@/components/ui/button";
import { formatCurrency } from "@/lib/finance/format";

export const catalogKeys = { assets: ["catalog", "assets"] as const, brokers: ["catalog", "brokers"] as const };
type ExactState = "idle" | "missing" | "inactive" | "unavailable";

function hasPositiveQuote(preview: AssetDiscovery) {
  const quote = Number(preview.cotacaoAtual);
  return Number.isFinite(quote) && quote > 0;
}

export function AssetSelector({ value, onChange }: { value: Asset | null; onChange: (asset: Asset, message?: string) => void }) {
  const queryClient = useQueryClient();
  const [query, setQuery] = useState("");
  const [market, setMarket] = useState<Market>("B3");
  const [preview, setPreview] = useState<AssetDiscovery | null>(null);
  const [exact, setExact] = useState<ExactState>("idle");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const operationEpoch = useRef(0);
  const assets = useQuery({ queryKey: [...catalogKeys.assets, query], queryFn: () => financeApi.assets(query, 0, 20), retry: false });
  const discovery = useQuery({ queryKey: ["catalog", "asset-discovery", market, query], queryFn: () => financeApi.discoverAssets(query, market), enabled: query.trim().length >= 2, retry: false });
  const catalogAssets = assets.data?.items.filter((asset) => asset.mercado === market) ?? [];
  const discoverableAssets = discovery.data?.filter((asset) => asset.mercado === market && hasPositiveQuote(asset)) ?? [];

  function changeMarket(next: Market) {
    operationEpoch.current += 1;
    setMarket(next);
    setPreview(null);
    setExact("idle");
    setBusy(false);
    setError("");
  }

  async function precheck(candidate = query.trim(), candidatePreview?: AssetDiscovery) {
    const requestEpoch = ++operationEpoch.current;
    const requestMarket = market;
    const isCurrent = () => operationEpoch.current === requestEpoch;
    const resolvePreview = () => {
      const resolved = candidatePreview ?? discovery.data?.find((item) => item.ticker === candidate.trim().toUpperCase()) ?? null;
      return resolved?.mercado === requestMarket ? resolved : null;
    };
    setBusy(true);
    setError("");
    try {
      const found = await financeApi.exactAsset(candidate);
      if (!isCurrent()) return;
      if (found.mercado !== requestMarket) {
        const resolvedPreview = resolvePreview();
        setPreview(resolvedPreview);
        setExact(resolvedPreview ? "missing" : "idle");
      } else if (found.ativo) {
        onChange(found);
        setExact("idle");
      } else setExact("inactive");
    } catch (failure) {
      if (!isCurrent()) return;
      if (failure instanceof FinanceApiError && failure.status === 404) {
        const resolvedPreview = resolvePreview();
        setPreview(resolvedPreview);
        setExact(resolvedPreview ? "missing" : "idle");
      }
      else {
        setExact("unavailable");
        setError(ptBrError(failure, "asset-validation"));
      }
    } finally {
      if (isCurrent()) setBusy(false);
    }
  }

  function selectDiscovery(result: AssetDiscovery) {
    setQuery(result.ticker);
    setPreview(result);
    void precheck(result.ticker, result);
  }

  async function register() {
    if (!preview) return;
    const requestEpoch = ++operationEpoch.current;
    const requestMarket = market;
    const isCurrent = () => operationEpoch.current === requestEpoch;
    const candidate = preview;
    setBusy(true);
    setError("");
    try {
      const asset = await financeApi.registerAsset(candidate.ticker, candidate.mercado);
      if (!isCurrent() || asset.mercado !== requestMarket) return;
      await queryClient.invalidateQueries({ queryKey: catalogKeys.assets });
      if (!isCurrent()) return;
      onChange(asset, asset.ticker !== query.trim().toUpperCase() ? `${query.trim().toUpperCase()} agora é negociado como ${asset.ticker}.` : undefined);
    } catch (failure) {
      if (!isCurrent()) return;
      if (failure instanceof FinanceApiError && failure.status === 409) {
        try {
          const asset = await financeApi.exactAsset(query.trim());
          if (!isCurrent() || asset.mercado !== requestMarket) return;
          onChange(asset, "O ativo canônico já havia sido cadastrado.");
          return;
        } catch {
          if (!isCurrent()) return;
          /* present the safe original conflict below */
        }
      }
      if (failure instanceof FinanceApiError && failure.status === 502) setExact("unavailable");
      setError(ptBrError(failure, "asset-validation"));
    } finally {
      if (isCurrent()) setBusy(false);
    }
  }

  return <div className="selector" aria-label="Selecionar ativo">
    <label className="operation-field">
      <span>Mercado</span>
      <select value={market} onChange={(event) => changeMarket(event.target.value as Market)}>
        <option value="B3">B3</option>
        <option value="US">US</option>
      </select>
    </label>
    <label className="operation-field">
      <span>Buscar ativo</span>
      <div className="selector__search">
        <input value={query} onChange={(event) => { setQuery(event.target.value); setPreview(null); setExact("idle"); setError(""); }} placeholder="Ticker ou nome" />
        <Button variant="secondary" onClick={() => void precheck()} disabled={!query.trim() || busy}>{busy ? "Verificando..." : "Verificar ticker"}</Button>
      </div>
    </label>
    {error && <p role="alert" className="operation-error">{error}</p>}
    {assets.isPending ? <p role="status">Buscando ativos...</p> : assets.isError ? <p role="alert">Não foi possível carregar o catálogo.</p> :
      <div role="listbox" aria-label="Ativos encontrados" className="selector__list">
        {catalogAssets.map((asset) => <button type="button" role="option" aria-selected={value?.id === asset.id} key={asset.id} onClick={() => onChange(asset)}>
          <EntityLogo provider={asset.logoProvider} reference={asset.logoReference} label={asset.ticker} />
          <span><strong>{asset.ticker}</strong><small>{asset.nome}</small><small>{asset.mercado} · {asset.moeda}</small></span>
        </button>)}
      </div>}
    {exact === "inactive" && <p role="alert">Este ticker existe, mas está inativo e não pode ser selecionado para compra.</p>}
    {discoverableAssets.length ? <div role="listbox" aria-label={`Sugestões de ${market}`} className="selector__list">
      {discoverableAssets.map((result) => <button type="button" role="option" aria-selected={value?.ticker === result.ticker} key={result.ticker} onClick={() => selectDiscovery(result)}>
        <EntityLogo provider={result.logoProvider} reference={result.logoReference} label={result.ticker} />
        <span><strong>{result.ticker}</strong><small>{result.nome}</small><small>{result.tipo} · {result.mercado} · {result.moeda}</small></span>
      </button>)}
    </div> : null}
    {exact === "unavailable" && <div className="registration-card" aria-live="polite">
      <strong>Não foi possível validar este ativo agora.</strong>
      <p>O ticker {query.trim().toUpperCase()} foi preservado. Tente novamente em alguns instantes.</p>
      <Button variant="secondary" onClick={() => void precheck()} disabled={busy}>{busy ? "Tentando..." : "Tentar novamente"}</Button>
    </div>}
    {exact === "missing" && preview && <div className="registration-card" aria-label={`Prévia de ${preview.ticker}`}>
      <EntityLogo provider={preview.logoProvider} reference={preview.logoReference} label={preview.ticker} />
      <strong>Cadastrar ticker {preview.ticker}</strong>
      <p>{preview.nome}</p>
      <p>{preview.tipo} · {preview.mercado} · {preview.moeda}</p>
      <strong>{formatCurrency(preview.cotacaoAtual, preview.moeda)}</strong>
      <p>Ao confirmar, o servidor revalidará os dados financeiros antes de criar o ativo no catálogo global.</p>
      <Button onClick={register} disabled={busy}>{busy ? "Validando..." : "Cadastrar ativo"}</Button>
    </div>}
  </div>;
}
