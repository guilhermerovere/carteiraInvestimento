"use client";

import { useLayoutEffect, useRef, useState, type FormEvent, type KeyboardEvent } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { adminApi, type AdminBroker } from "@/client/management-api";
import { ptBrError } from "@/client/presentation-errors";
import { Button } from "@/components/ui/button";
import { EntityLogo } from "@/components/finance/entity-logo";
import { cnpjCaretPosition, cnpjDigits, formatCnpj, isValidCnpj } from "@/lib/cnpj";

export function BrokerManagement() {
  const client = useQueryClient(); const list = useQuery({ queryKey: ["admin", "brokers"], queryFn: adminApi.brokers, retry: false });
  const [createOpen, setCreateOpen] = useState(false), [editing, setEditing] = useState<AdminBroker | null>(null), [message, setMessage] = useState(""), [error, setError] = useState(""), [cnpj, setCnpj] = useState(""), [cnpjError, setCnpjError] = useState("");
  const cnpjInput = useRef<HTMLInputElement>(null), pendingCaret = useRef<number | null>(null);
  useLayoutEffect(() => { if (pendingCaret.current !== null && cnpjInput.current) { cnpjInput.current.setSelectionRange(pendingCaret.current, pendingCaret.current); pendingCaret.current = null; } }, [cnpj]);
  const mutation = useMutation({ mutationFn: async (action: () => Promise<AdminBroker>) => action(), retry: false, onSuccess: () => client.invalidateQueries({ queryKey: ["admin", "brokers"] }) });
  async function run(action: () => Promise<AdminBroker>, success: string, context?: "broker-create") { setError(""); setMessage(""); try { await mutation.mutateAsync(action); setMessage(success); return true; } catch (failure) { setError(ptBrError(failure, context)); return false; } }
  function closeCreate() { setCreateOpen(false); setCnpj(""); setCnpjError(""); }
  function changeCnpj(value: string, caret: number, input: HTMLInputElement) {
    const next = cnpjDigits(value), formatted = formatCnpj(next), nextCaret = cnpjCaretPosition(formatted, cnpjDigits(value.slice(0, caret)).length);
    setCnpjError("");
    if (next === cnpj) { input.value = formatted; input.setSelectionRange(nextCaret, nextCaret); }
    else { pendingCaret.current = nextCaret; setCnpj(next); }
  }
  function handleCnpjKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    const input = event.currentTarget, caret = input.selectionStart ?? input.value.length, end = input.selectionEnd ?? caret;
    if (caret !== end) return;
    const previous = input.value[caret - 1], next = input.value[caret];
    if (event.key === "Backspace" && caret > 0 && previous && !/[0-9]/.test(previous)) {
      const digitIndex = cnpjDigits(input.value.slice(0, caret - 1)).length - 1;
      if (digitIndex >= 0) { event.preventDefault(); const digits = cnpj.slice(0, digitIndex) + cnpj.slice(digitIndex + 1); pendingCaret.current = digitIndex; setCnpj(digits); setCnpjError(""); }
    } else if (event.key === "Delete" && next && !/[0-9]/.test(next)) {
      const digitIndex = cnpjDigits(input.value.slice(0, caret)).length;
      if (digitIndex < cnpj.length) { event.preventDefault(); const digits = cnpj.slice(0, digitIndex) + cnpj.slice(digitIndex + 1); pendingCaret.current = digitIndex; setCnpj(digits); setCnpjError(""); }
    }
  }
  function pasteCnpj(event: React.ClipboardEvent<HTMLInputElement>) { event.preventDefault(); const input = event.currentTarget, digits = cnpjDigits(event.clipboardData.getData("text")), formatted = formatCnpj(digits), caret = cnpjCaretPosition(formatted, digits.length); setCnpjError(""); if (digits === cnpj) { input.value = formatted; input.setSelectionRange(caret, caret); } else { pendingCaret.current = caret; setCnpj(digits); } }
  async function create(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!isValidCnpj(cnpj)) { setCnpjError("Informe um CNPJ válido."); cnpjInput.current?.focus(); return; }
    const data = new FormData(event.currentTarget);
    if (await run(() => adminApi.createBroker({ cnpj, numero: String(data.get("numero") || "") || null, complemento: String(data.get("complemento") || "") || null }), "Corretora cadastrada com sucesso.", "broker-create")) closeCreate();
  }
  async function edit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); if (!editing) return; const data = new FormData(event.currentTarget); if (await run(() => adminApi.editBroker(editing.id, { numero: String(data.get("numero") || "") || null, complemento: String(data.get("complemento") || "") || null }), "Corretora atualizada com sucesso.")) setEditing(null); }
  return <section className="admin-page" aria-labelledby="brokers-heading">
    <header className="admin-page__header"><div className="page-intro"><div><span>Catálogo global</span><h2 id="brokers-heading">Corretoras</h2><p>Cadastro regulatório e disponibilidade operacional.</p></div></div><Button onClick={() => setCreateOpen(true)}>Nova corretora</Button></header>
    {(message || error) && <p role={error ? "alert" : "status"} className={error ? "settings-feedback settings-feedback--error" : "settings-feedback"}>{error || message}</p>}
    {list.isPending ? <p>Carregando corretoras...</p> : list.isError ? <p role="alert">Não foi possível carregar as corretoras.</p> : <div className="admin-list" aria-live="polite">{list.data.items.map((broker) => <article className="admin-row" key={broker.id}><EntityLogo provider={broker.logoProvider} reference={broker.logoReference} label={broker.nomeFantasia ?? broker.razaoSocial} /><div><strong>{broker.nomeFantasia ?? broker.razaoSocial}</strong><p>{broker.razaoSocial} · CNPJ {broker.cnpj} · {broker.cidade}/{broker.uf}</p></div><span className={broker.ativo ? "status-pill status-pill--active" : "status-pill"}>{broker.ativo ? "Ativa" : "Inativa"}</span><div className="admin-row__actions"><Button variant="secondary" onClick={() => setEditing(broker)}>Editar endereço</Button><Button variant={broker.ativo ? "danger" : "primary"} onClick={() => { if (!broker.ativo || window.confirm("Desativar esta corretora?")) void run(() => adminApi.setBrokerActive(broker.id, !broker.ativo), broker.ativo ? "Corretora desativada com sucesso." : "Corretora ativada com sucesso."); }}>{broker.ativo ? "Desativar" : "Ativar"}</Button></div></article>)}</div>}
    {createOpen && <Dialog title="Nova corretora" close={closeCreate}><p>Os dados oficiais e o branding são obtidos automaticamente.</p><form className="catalog-dialog__form" onSubmit={create}><label htmlFor="broker-cnpj"><span>CNPJ</span><input ref={cnpjInput} id="broker-cnpj" name="cnpj" type="text" inputMode="numeric" autoComplete="off" maxLength={18} required placeholder="00.000.000/0000-00" autoFocus value={formatCnpj(cnpj)} aria-invalid={cnpjError ? true : undefined} aria-describedby={cnpjError ? "broker-cnpj-error" : undefined} onChange={(event) => changeCnpj(event.currentTarget.value, event.currentTarget.selectionStart ?? event.currentTarget.value.length, event.currentTarget)} onPaste={pasteCnpj} onKeyDown={handleCnpjKeyDown} onBlur={() => { if (cnpj && !isValidCnpj(cnpj)) setCnpjError("Informe um CNPJ válido."); }} />{cnpjError && <span id="broker-cnpj-error" className="field-error">{cnpjError}</span>}</label><label><span>Número (opcional)</span><input name="numero" maxLength={20} /></label><label><span>Complemento (opcional)</span><input name="complemento" maxLength={160} /></label><Button type="submit" disabled={mutation.isPending}>Cadastrar corretora</Button></form></Dialog>}
    {editing && <Dialog title="Editar endereço" close={() => setEditing(null)}><p>{editing.nomeFantasia ?? editing.razaoSocial}</p><form className="catalog-dialog__form" onSubmit={edit}><label><span>Número</span><input name="numero" defaultValue={editing.numero ?? ""} maxLength={20} autoFocus /></label><label><span>Complemento</span><input name="complemento" defaultValue={editing.complemento ?? ""} maxLength={160} /></label><Button type="submit" disabled={mutation.isPending}>Salvar endereço</Button></form></Dialog>}
  </section>;
}

function Dialog({ title, close, children }: { title: string; close: () => void; children: React.ReactNode }) { return <div className="catalog-dialog-backdrop"><section className="catalog-dialog" role="dialog" aria-modal="true" aria-label={title}><header><h3>{title}</h3><Button variant="secondary" onClick={close}>Cancelar</Button></header>{children}</section></div>; }
