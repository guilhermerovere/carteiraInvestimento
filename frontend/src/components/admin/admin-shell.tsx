"use client";

import Link from "next/link";
import { Boxes, ChevronDown, CircleUserRound, LayoutDashboard, LogOut, Settings, ShieldCheck } from "lucide-react";
import { usePathname, useRouter } from "next/navigation";
import type { ReactNode } from "react";
import { useLogout } from "@/client/auth";
import type { CurrentUser } from "@/lib/auth-contracts";
import { Button } from "@/components/ui/button";
import { ThemeToggle } from "@/components/theme-toggle";
import { useDismissibleDetails } from "@/components/use-dismissible-details";

const links = [{ href: "/admin", label: "Visão geral", icon: LayoutDashboard }, { href: "/admin/corretoras", label: "Corretoras", icon: ShieldCheck }, { href: "/admin/ativos", label: "Ativos", icon: Boxes }];
export function AdminShell({ user, children }: { user: CurrentUser; children: ReactNode }) {
  const pathname = usePathname(); const router = useRouter(); const logout = useLogout();
  const profileMenu = useDismissibleDetails();
  async function exit() { await logout.mutateAsync(); router.replace("/login"); }
  return <div className="admin-shell"><aside className="admin-sidebar"><Link href="/admin" className="brand"><span className="brand__mark" aria-hidden="true">V</span><span className="brand__name">Valore Admin</span></Link><nav aria-label="Administração">{links.map(({ href, label, icon: Icon }) => <Link key={href} href={href} className={`shell-nav__link${pathname === href ? " is-active" : ""}`} aria-current={pathname === href ? "page" : undefined}><Icon aria-hidden="true" /><span>{label}</span></Link>)}</nav></aside>
    <div className="admin-body"><header className="shell-header"><div className="shell-heading"><span>Administração</span><h1>Catálogos globais</h1></div><div className="shell-actions"><ThemeToggle /><details className="profile-menu" ref={profileMenu}><summary aria-label={`Abrir perfil de ${user.nome}`}><CircleUserRound aria-hidden="true" /><span className="profile-menu__identity"><strong>{user.nome}</strong><small>{user.email}</small></span><ChevronDown aria-hidden="true" /></summary><div className="profile-menu__popover"><p><strong>{user.nome}</strong><span>{user.email}</span></p><Link className="profile-menu__action" href="/configuracoes"><Settings aria-hidden="true" /> Configurações</Link><Button variant="ghost" onClick={exit} disabled={logout.isPending}><LogOut aria-hidden="true" /> Sair</Button></div></details></div></header><main id="conteudo-principal" className="admin-main">{children}</main></div>
    <nav className="admin-mobile-nav" aria-label="Administração móvel">{links.map(({ href, label, icon: Icon }) => <Link key={href} href={href} className={pathname === href ? "is-active" : ""}><Icon aria-hidden="true" /><span>{label}</span></Link>)}<Link href="/configuracoes"><Settings aria-hidden="true" /><span>Conta</span></Link></nav>
  </div>;
}
