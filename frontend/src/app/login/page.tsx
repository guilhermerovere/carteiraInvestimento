import Link from "next/link";
import { Suspense } from "react";
import { AuthShell } from "@/components/auth-shell";
import { LoginForm } from "./login-form";

export default function LoginPage() {
  return (
    <AuthShell variant="login">
      <header className="auth-form-header">
        <span className="auth-eyebrow">Acesso seguro à Valore</span>
        <h1>Bem-vindo de volta</h1>
        <p>Acesse sua carteira e acompanhe seu patrimônio.</p>
      </header>
      <Suspense fallback={<div className="auth-form-skeleton" aria-label="Carregando formulário" />}>
        <LoginForm />
      </Suspense>
      <p className="auth-switch">Ainda não tem uma conta? <Link href="/register">Criar conta</Link></p>
    </AuthShell>
  );
}
