"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { useLogin } from "@/client/auth";
import { AuthFormError } from "@/client/auth-form-api";
import { safeReturnTo } from "@/lib/return-to";

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

  return <form onSubmit={async (event) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    setMessage("");
    setCorrelationId("");
    try {
      await login.mutateAsync({ email: String(data.get("email")), senha: String(data.get("senha")) });
      router.replace(safeReturnTo(params.get("returnTo")));
    } catch (error) {
      setMessage(messageFor(error));
      if (error instanceof AuthFormError && error.correlationId) setCorrelationId(error.correlationId);
    }
  }}>
    <label>E-mail<input required name="email" type="email" /></label>
    <label>Senha<input required name="senha" type="password" /></label>
    <button disabled={login.isPending}>Entrar</button>
    {message && <p role="alert">{message}</p>}
    {correlationId && <p>Código de suporte: {correlationId}</p>}
  </form>;
}
