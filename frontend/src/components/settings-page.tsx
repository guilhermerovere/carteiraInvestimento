"use client";

import Link from "next/link";
import { useRef, useState, type FormEvent } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { AlertTriangle, KeyRound, Save, Trash2, X } from "lucide-react";
import { authMeKey, useCurrentUser } from "@/client/auth";
import { financeApi } from "@/client/finance-api";
import { accountApi } from "@/client/management-api";
import { ptBrError } from "@/client/presentation-errors";
import { RECOVERY_KEY } from "@/client/operation-intent";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Money } from "@/components/finance/values";

export function SettingsPage() {
  const current = useCurrentUser(); const queryClient = useQueryClient(); const router = useRouter();
  const user = current.data;
  const [editedName, setEditedName] = useState<string | null>(null); const [editedEmail, setEditedEmail] = useState<string | null>(null);
  const [currentPassword, setCurrentPassword] = useState(""); const [newPassword, setNewPassword] = useState(""); const [confirmation, setConfirmation] = useState("");
  const [message, setMessage] = useState(""); const [error, setError] = useState(""); const [pending, setPending] = useState(false);
  const [ambiguous] = useState(() => typeof window !== "undefined" && Boolean(sessionStorage.getItem(RECOVERY_KEY))); const closeDialog = useRef<HTMLDialogElement>(null);
  const cash = useQuery({ queryKey: ["settings", "cash"], queryFn: financeApi.cash, enabled: user?.role === "ROLE_USER", retry: false });
  const positions = useQuery({ queryKey: ["settings", "positions"], queryFn: () => financeApi.positions(0, 1), enabled: user?.role === "ROLE_USER", retry: false });
  const cashIsZero = cash.data ? /^0(?:\.0+)?$/.test(cash.data.saldoCaixaBrl) : false;
  const hasOpenPositions = (positions.data?.totalElements ?? 0) > 0;
  const financialStateReady = user?.role !== "ROLE_USER" || (cash.isSuccess && positions.isSuccess);
  const closureBlocked = ambiguous || !financialStateReady || (user?.role === "ROLE_USER" && (!cashIsZero || hasOpenPositions));
  const name = editedName ?? user?.nome ?? "";
  const email = editedEmail ?? user?.email ?? "";

  async function updateProfile(kind: "name" | "email") {
    setPending(true); setError(""); setMessage("");
    try {
      const updated = kind === "name" ? await accountApi.updateName(name) : await accountApi.updateEmail(email);
      queryClient.setQueryData(authMeKey, updated); setEditedName(null); setEditedEmail(null);
      setMessage(kind === "name" ? "Nome atualizado com sucesso." : "Email atualizado com sucesso.");
      router.refresh();
    } catch (failure) { setError(ptBrError(failure, kind === "email" ? "email" : "generic")); }
    finally { setPending(false); }
  }

  async function changePassword(event: FormEvent) {
    event.preventDefault(); setError(""); setMessage("");
    if (newPassword !== confirmation) { setError("A confirmação da nova senha não confere."); return; }
    setPending(true);
    try {
      await accountApi.changePassword(currentPassword, newPassword);
      setCurrentPassword(""); setNewPassword(""); setConfirmation(""); setMessage("Senha alterada com sucesso.");
      setTimeout(() => { queryClient.clear(); router.replace("/login"); router.refresh(); }, 600);
    } catch (failure) { setError(ptBrError(failure, "password")); setPending(false); }
  }

  function openClosure() { setError(""); setMessage(""); closeDialog.current?.showModal(); }
  function closeClosure() { if (!pending) closeDialog.current?.close(); }
  async function closeAccount(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError("");
    const data = new FormData(event.currentTarget); const password = String(data.get("senhaAtual") ?? ""); const text = String(data.get("confirmacao") ?? "");
    if (closureBlocked) { setError("Resolva as pendências indicadas antes de excluir a conta."); return; }
    setPending(true);
    try {
      await accountApi.close(password, text); sessionStorage.removeItem(RECOVERY_KEY); setMessage("Sua conta foi excluída com sucesso."); closeDialog.current?.close();
      setTimeout(() => { queryClient.clear(); router.replace("/login"); router.refresh(); }, 600);
    } catch (failure) { setError(ptBrError(failure, "closure")); setPending(false); }
  }

  if (current.isPending || !user) return <p role="status">Carregando configurações...</p>;
  return <section className="settings-page">
    <div className="settings-heading"><span>Conta Valore</span><h1>Configurações</h1><p>Atualize seus dados e proteja o acesso à sua conta.</p></div>
    {(message || error) && <div className={error ? "settings-feedback settings-feedback--error" : "settings-feedback"} role={error ? "alert" : "status"}>{error || message}</div>}
    <Card><CardHeader><div className="settings-section-title"><Save aria-hidden="true" /><div><h2>Dados pessoais</h2><p>Os dados exibidos no menu de perfil são atualizados imediatamente.</p></div></div></CardHeader><CardContent className="settings-form-grid">
      <label className="settings-field"><span>Nome</span><input value={name} onChange={(event) => setEditedName(event.target.value)} maxLength={255} required aria-describedby="name-help" /><small id="name-help">Entre 1 e 255 caracteres.</small></label>
      <Button onClick={() => updateProfile("name")} disabled={pending || !name.trim()}>Salvar nome</Button>
      <label className="settings-field"><span>Email</span><input type="email" value={email} onChange={(event) => setEditedEmail(event.target.value)} maxLength={320} required /></label>
      <Button onClick={() => updateProfile("email")} disabled={pending || !email.trim()}>Salvar email</Button>
    </CardContent></Card>
    <Card><CardHeader><div className="settings-section-title"><KeyRound aria-hidden="true" /><div><h2>Segurança</h2><p>A troca encerra a sessão atual. Outros tokens existentes expiram no prazo normal.</p></div></div></CardHeader><CardContent><form className="settings-form-grid" onSubmit={changePassword}>
      <label className="settings-field"><span>Senha atual</span><input type="password" autoComplete="current-password" value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} required /></label>
      <label className="settings-field"><span>Nova senha</span><input type="password" autoComplete="new-password" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} required aria-describedby="password-help" /><small id="password-help">Mínimo de 8 caracteres, com maiúscula, minúscula, número e caractere especial.</small></label>
      <label className="settings-field"><span>Confirmar nova senha</span><input type="password" autoComplete="new-password" value={confirmation} onChange={(event) => setConfirmation(event.target.value)} required /></label>
      <Button type="submit" disabled={pending}>Alterar senha</Button>
    </form></CardContent></Card>
    <Card className="danger-zone"><CardHeader><div className="settings-section-title"><AlertTriangle aria-hidden="true" /><div><h2>Zona de perigo</h2><p>O encerramento é permanente para esta identidade e preserva o histórico financeiro obrigatório.</p></div></div></CardHeader><CardContent className="danger-zone__content">
      {user.role === "ROLE_USER" && <div className="closure-status"><span>Saldo conhecido: {cash.data ? <Money value={cash.data.saldoCaixaBrl} /> : "carregando"}</span><span>Posições abertas: {positions.data?.totalElements ?? "carregando"}</span></div>}
      {ambiguous && <p role="alert">Existe uma operação financeira com resultado pendente. <Link href="/carteira">Revise a operação antes de continuar.</Link></p>}
      {user.role === "ROLE_USER" && cash.data && !cashIsZero && <p>Para excluir sua conta, primeiro deixe o saldo em caixa em R$ 0,00. <Link href="/carteira">Ir para a carteira</Link></p>}
      {user.role === "ROLE_USER" && hasOpenPositions && <p>Venda suas posições antes de excluir a conta. <Link href="/carteira/posicoes">Ver posições</Link></p>}
      <Button variant="danger" onClick={openClosure} disabled={closureBlocked}><Trash2 aria-hidden="true" /> Excluir conta</Button>
    </CardContent></Card>
    <dialog ref={closeDialog} className="closure-dialog" aria-labelledby="closure-title" onCancel={(event) => { if (pending) event.preventDefault(); }}>
      <div className="operation-dialog__header"><div><span>Confirmação obrigatória</span><h2 id="closure-title">Excluir minha conta</h2></div><Button variant="ghost" size="icon" aria-label="Fechar" onClick={closeClosure}><X aria-hidden="true" /></Button></div>
      <form className="closure-dialog__body" onSubmit={closeAccount}><p>Esta ação desativa o acesso e anonimiza seus dados pessoais. O histórico financeiro e de auditoria será preservado.</p><label className="settings-field"><span>Senha atual</span><input name="senhaAtual" type="password" autoComplete="current-password" required /></label><label className="settings-field"><span>Digite EXCLUIR MINHA CONTA</span><input name="confirmacao" required autoComplete="off" /></label>{error && <p role="alert" className="settings-feedback settings-feedback--error">{error}</p>}<div className="operation-actions"><Button variant="ghost" onClick={closeClosure}>Cancelar</Button><Button variant="danger" type="submit" disabled={pending}>{pending ? "Excluindo..." : "Excluir minha conta"}</Button></div></form>
    </dialog>
  </section>;
}
