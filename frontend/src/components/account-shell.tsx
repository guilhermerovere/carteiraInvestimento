"use client";

import Link from "next/link";
import { CircleUserRound, LogOut } from "lucide-react";
import { useRouter } from "next/navigation";
import type { ReactNode } from "react";
import { useLogout } from "@/client/auth";
import type { CurrentUser } from "@/lib/auth-contracts";
import { Button } from "@/components/ui/button";
import { ThemeToggle } from "@/components/theme-toggle";

export function AccountShell({ user, children }: { user: CurrentUser; children: ReactNode }) {
  const router = useRouter(); const logout = useLogout();
  const home = user.role === "ROLE_ADMIN" ? "/admin" : "/carteira";
  async function exit() { await logout.mutateAsync(); router.replace("/login"); }
  return <div className="account-shell">
    <header className="account-shell__header">
      <Link href={home} className="brand" aria-label="Voltar ao início Valore"><span className="brand__mark" aria-hidden="true">V</span><span className="brand__name">Valore</span></Link>
      <div className="account-shell__actions"><ThemeToggle /><div className="account-shell__identity"><CircleUserRound aria-hidden="true" /><span><strong>{user.nome}</strong><small>{user.email}</small></span></div><Button variant="ghost" onClick={exit} disabled={logout.isPending}><LogOut aria-hidden="true" /> Sair</Button></div>
    </header>
    <main id="conteudo-principal" className="settings-main">{children}</main>
  </div>;
}
