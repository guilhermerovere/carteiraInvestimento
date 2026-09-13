"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { useLogin } from "@/client/auth";
import { AuthFormError } from "@/client/auth-form-api";
import { safeReturnToForRole } from "@/lib/return-to";

function messageFor(error: unknown): string {
  return error instanceof AuthFormError && error.kind === "http"
    ? error.problem?.detail ?? error.problem?.title ?? "Não foi possível iniciar a sessão."
    : "Não foi possível iniciar a sessão.";
}

export function LoginForm() {
  const router = useRouter();
  const params = useSearchParams();
  const login = useLogin();
  const [message, setMessage] = useState("");
  const [correlationId, setCorrelationId] = useState("");

  const errorId = "login-form-error";

  return <form className="auth-form" aria-busy={login.isPending} onSubmit={async (event) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    setMessage("");
    setCorrelationId("");
    try {
      const user = await login.mutateAsync({ email: String(data.get("email")), senha: String(data.get("senha")) });
      router.replace(safeReturnToForRole(params.get("returnTo"), user.role));
    } catch (error) {
      setMessage(messageFor(error));
      if (error instanceof AuthFormError && error.correlationId) setCorrelationId(error.correlationId);
    }
  }}>
    <div className="auth-field">
      <label htmlFor="login-email">E-mail</label>
      <input id="login-email" required name="email" type="email" inputMode="email" autoComplete="username" placeholder="voce@exemplo.com" disabled={login.isPending} aria-invalid={Boolean(message)} aria-describedby={message ? errorId : undefined} />
    </div>
    <div className="auth-field">
      <label htmlFor="login-password">Senha</label>
      <input id="login-password" required name="senha" type="password" autoComplete="current-password" placeholder="Digite sua senha" disabled={login.isPending} aria-invalid={Boolean(message)} aria-describedby={message ? errorId : undefined} />
    </div>
    <button className="auth-submit" disabled={login.isPending}>{login.isPending ? "Entrando…" : "Entrar"}</button>
    <div className="auth-feedback" aria-live="polite">
      {message && <p id={errorId} role="alert">{message}</p>}
      {correlationId && <p className="auth-support-code">Código de suporte: {correlationId}</p>}
    </div>
  </form>;
}
