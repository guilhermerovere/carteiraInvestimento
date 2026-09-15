"use client";

import { useState } from "react";
import type { Asset } from "@/lib/finance/contracts";
import { AssetSelector } from "./asset-selector";
import { EntityLogo } from "./entity-logo";

export function AssetDiscoveryPage() {
  const [selected, setSelected] = useState<Asset | null>(null);
  const [message, setMessage] = useState("");
  return <section className="asset-discovery" aria-labelledby="assets-title">
    <div className="page-intro"><div><span>Mercados</span><h2 id="assets-title">Ativos</h2><p>Busque por ticker ou empresa e adicione somente instrumentos validados ao catálogo global.</p></div></div>
    <AssetSelector value={selected} onChange={(asset, info) => { setSelected(asset); setMessage(info ?? (asset.ativo ? `${asset.ticker} está disponível no catálogo.` : "Este ativo está inativo.")); }} />
    {message && <p className="operation-message" role="status">{message}</p>}
    {selected && <article className="asset-discovery__selection" aria-label={`Ativo selecionado ${selected.ticker}`}>
      <EntityLogo provider={selected.logoProvider} reference={selected.logoReference} label={selected.ticker} />
      <div><strong>{selected.ticker}</strong><span>{selected.nome}</span><small>{selected.mercado} · {selected.tipo} · {selected.moeda}</small></div>
      <div><small>Pronto para compra</small>{selected.ativo ? <button type="button" className="asset-discovery__operate" onClick={() => window.dispatchEvent(new CustomEvent("valore:buy", { detail: selected }))}>Adicionar no Operar</button> : <strong>Indisponível</strong>}</div>
    </article>}
  </section>;
}
