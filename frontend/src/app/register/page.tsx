import Link from "next/link";
import { AuthShell } from "@/components/auth-shell";
import { RegisterForm } from "./register-form";

export default function RegisterPage() {
  return (
    <AuthShell variant="register">
      <header className="auth-form-header">
        <span className="auth-eyebrow">Sua jornada na Valore</span>
        <h1>Criar conta</h1>
        <p>Comece a acompanhar seu patrimônio com mais clareza.</p>
      </header>
      <RegisterForm />
      <p className="auth-switch">Já faz parte da Valore? <Link href="/login">Entrar</Link></p>
    </AuthShell>
  );
}
