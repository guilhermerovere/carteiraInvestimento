"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { useRegister } from "@/client/auth";
import { AuthFormError } from "@/client/auth-form-api";

function messageFor(error: unknown): string {
  return error instanceof AuthFormError && error.kind === "http"
    ? error.problem?.detail ?? error.problem?.title ?? "Não foi possível criar a conta."
    : "Não foi possível criar a conta.";
}

export function RegisterForm() {
  const router = useRouter();
  const register = useRegister();
  const [message, setMessage] = useState("");
  const [correlationId, setCorrelationId] = useState("");

  return <form onSubmit={async (event) => {
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
    <label>Nome<input required name="nome" /></label>
    <label>E-mail<input required name="email" type="email" /></label>
    <label>Senha<input required name="senha" type="password" /></label>
    <button disabled={register.isPending}>Cadastrar</button>
    {message && <p role="alert">{message}</p>}
    {correlationId && <p>Código de suporte: {correlationId}</p>}
  </form>;
}
