import Link from "next/link";
import { RegisterForm } from "./register-form";
export default function RegisterPage() { return <main><h1>Criar conta</h1><RegisterForm /><Link href="/login">Já tenho conta</Link></main>; }
