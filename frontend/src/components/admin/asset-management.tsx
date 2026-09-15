"use client";

import { useState, type FormEvent } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { adminApi, type AdminAsset } from "@/client/management-api";
import { ptBrError } from "@/client/presentation-errors";
import { Button } from "@/components/ui/button";
import { EntityLogo } from "@/components/finance/entity-logo";

export function AssetManagement() {
  const client = useQueryClient();
  const list = useQuery({ queryKey: ["admin", "assets"], queryFn: adminApi.assets, retry: false });
  const [createOpen, setCreateOpen] = useState(false), [editing, setEditing] = useState<AdminAsset | null>(null), [message, setMessage] = useState(""), [error, setError] = useState("");
  const mutation = useMutation({ mutationFn: async (action: () => Promise<AdminAsset>) => action(), retry: false, onSuccess: () => client.invalidateQueries({ queryKey: ["admin", "assets"] }) });
  async function run(action: () => Promise<AdminAsset>, success: string) { setError(""); setMessage(""); try { await mutation.mutateAsync(action); setMessage(success); return true; } catch (failure) { setError(ptBrError(failure)); return false; } }
  async function create(event: FormEvent<HTMLFormElement>) { event.preventDefault(); const data = new FormData(event.currentTarget); if (await run(() => adminApi.createAsset({ ticker: String(data.get("ticker")), mercado: String(data.get("mercado")) as AdminAsset["mercado"] }), "Ativo cadastrado com sucesso.")) setCreateOpen(false); }
  async function edit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); if (!editing) return; const data = new FormData(event.currentTarget); if (await run(() => adminApi.editAsset(editing.id, String(data.get("nome"))), "Nome do ativo atualizado com sucesso.")) setEditing(null); }
  return <section className="admin-page" aria-labelledby="assets-heading">
    <header className="admin-page__header"><div className="page-intro"><div><span>Catálogo global</span><h2 id="assets-heading">Ativos</h2><p>Pesquisa, lifecycle e identidade de mercado em uma lista enxuta.</p></div></div><Button onClick={() => setCreateOpen(true)}>Adicionar ativo</Button></header>
    {(message || error) && <p role={error ? "alert" : "status"} className={error ? "settings-feedback settings-feedback--error" : "settings-feedback"}>{error || message}</p>}
    {list.isPending ? <p>Carregando ativos...</p> : list.isError ? <p role="alert">Não foi possível carregar os ativos.</p> : <div className="admin-list" aria-live="polite">{list.data.items.map((asset) => <article className="admin-row" key={asset.id}><EntityLogo provider={asset.logoProvider} reference={asset.logoReference} label={asset.ticker} /><div><strong>{asset.ticker}</strong><p>{asset.nome} · {asset.tipo} · {asset.mercado} · {asset.moeda}</p></div><span className={asset.ativo ? "status-pill status-pill--active" : "status-pill"}>{asset.ativo ? "Ativo" : "Inativo"}</span><div className="admin-row__actions"><Button variant="secondary" onClick={() => setEditing(asset)}>Editar nome</Button><Button variant={asset.ativo ? "danger" : "primary"} onClick={() => { if (!asset.ativo || window.confirm("Desativar este ativo?")) void run(() => adminApi.setAssetActive(asset.id, !asset.ativo), asset.ativo ? "Ativo desativado com sucesso." : "Ativo ativado com sucesso."); }}>{asset.ativo ? "Desativar" : "Ativar"}</Button></div></article>)}</div>}
    {createOpen && <Dialog title="Adicionar ativo" close={() => setCreateOpen(false)}><p>O servidor valida e completa os dados financeiros automaticamente.</p><form className="catalog-dialog__form" onSubmit={create}><label><span>Ticker</span><input name="ticker" required maxLength={6} placeholder="BBAS3 ou AAPL" autoFocus /></label><label><span>Mercado</span><select name="mercado"><option value="B3">B3</option><option value="US">Estados Unidos</option></select></label><Button type="submit" disabled={mutation.isPending}>Cadastrar ativo</Button></form></Dialog>}
    {editing && <Dialog title="Editar nome" close={() => setEditing(null)}><p>{editing.ticker} mantém ticker, tipo e mercado.</p><form className="catalog-dialog__form" onSubmit={edit}><label><span>Nome</span><input name="nome" defaultValue={editing.nome} required maxLength={160} autoFocus /></label><Button type="submit" disabled={mutation.isPending}>Salvar nome</Button></form></Dialog>}
  </section>;
}

function Dialog({ title, close, children }: { title: string; close: () => void; children: React.ReactNode }) { return <div className="catalog-dialog-backdrop"><section className="catalog-dialog" role="dialog" aria-modal="true" aria-label={title}><header><h3>{title}</h3><Button variant="secondary" onClick={close}>Cancelar</Button></header>{children}</section></div>; }
