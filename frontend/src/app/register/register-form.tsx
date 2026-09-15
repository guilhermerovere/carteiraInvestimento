"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { useRegister } from "@/client/auth";
import { AuthFormError, authErrorMessage } from "@/client/auth-form-api";

function messageFor(error: unknown): string {
  return authErrorMessage(error, "register");
}

export function RegisterForm() {
  const router = useRouter();
  const register = useRegister();
  const [message, setMessage] = useState("");
  const [correlationId, setCorrelationId] = useState("");

  const errorId = "register-form-error";

  return <form className="auth-form" aria-busy={register.isPending} onSubmit={async (event) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    setMessage("");
    setCorrelationId("");
    try {
      await register.mutateAsync({ nome: String(data.get("nome")), email: String(data.get("email")), senha: String(data.get("senha")) });
      router.replace("/login");
    } catch (error) {
      setMessage(messageFor(error));
      if (error instanceof AuthFormError && error.correlationId) setCorrelationId(error.correlationId);
    }
  }}>
    <div className="auth-field">
      <label htmlFor="register-name">Nome Completo</label>
      <input id="register-name" required name="nome" type="text" autoComplete="name" placeholder="Seu nome completo" disabled={register.isPending} aria-invalid={Boolean(message)} aria-describedby={message ? errorId : undefined} />
    </div>
    <div className="auth-field">
      <label htmlFor="register-email">E-mail</label>
      <input id="register-email" required name="email" type="email" inputMode="email" autoComplete="email" placeholder="voce@exemplo.com" disabled={register.isPending} aria-invalid={Boolean(message)} aria-describedby={message ? errorId : undefined} />
    </div>
    <div className="auth-field">
      <label htmlFor="register-password">Senha</label>
      <input id="register-password" required name="senha" type="password" autoComplete="new-password" placeholder="Crie uma senha segura" minLength={8} disabled={register.isPending} aria-invalid={Boolean(message)} aria-describedby={message ? `${errorId} register-password-help` : "register-password-help"} />
      <small id="register-password-help">Use no mínimo 8 caracteres, com maiúscula, minúscula, número e símbolo.</small>
    </div>
    <button className="auth-submit" disabled={register.isPending}>{register.isPending ? "Criando conta…" : "Cadastrar"}</button>
    <div className="auth-feedback" aria-live="polite">
      {message && <p id={errorId} role="alert">{message}</p>}
      {correlationId && <p className="auth-support-code">Código de suporte: {correlationId}</p>}
    </div>
  </form>;
}
