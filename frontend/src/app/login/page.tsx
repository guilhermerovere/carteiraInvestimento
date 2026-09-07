import Link from "next/link";
import { Suspense } from "react";
import { LoginForm } from "./login-form";
export default function LoginPage() { return <main><h1>Entrar</h1><Suspense fallback={null}><LoginForm /></Suspense><Link href="/register">Criar conta</Link></main>; }
