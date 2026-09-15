"use client";

import {
  BriefcaseBusiness, ChartNoAxesCombined, ChevronDown, ChevronLeft, ChevronRight, CircleUserRound,
  Ellipsis, Landmark, LogOut, Menu, ReceiptText, Settings, WalletCards,
} from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState, type ReactNode } from "react";
import { useLogout } from "@/client/auth";
import type { CurrentUser } from "@/lib/auth-contracts";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { ThemeToggle } from "@/components/theme-toggle";
import { OperationLauncher } from "@/components/finance/operation-center";
import { useDismissibleDetails } from "@/components/use-dismissible-details";

const navigation = [
  { href: "/carteira", label: "Carteira", icon: Landmark, exact: true },
  { href: "/carteira/posicoes", label: "Posições", icon: BriefcaseBusiness },
  { href: "/carteira/ativos", label: "Ativos", icon: ChartNoAxesCombined },
  { href: "/carteira/transacoes", label: "Transações", icon: ReceiptText },
  { href: "/carteira/movimentacoes", label: "Movimentações", icon: WalletCards },
] as const;

const titles: Record<string, { eyebrow: string; title: string }> = {
  "/carteira": { eyebrow: "Visão patrimonial", title: "Minha carteira" },
  "/carteira/posicoes": { eyebrow: "Custódia", title: "Posições" },
  "/carteira/ativos": { eyebrow: "Catálogo de mercado", title: "Ativos" },
  "/carteira/transacoes": { eyebrow: "Histórico", title: "Transações" },
  "/carteira/movimentacoes": { eyebrow: "Caixa em BRL", title: "Movimentações" },
};

function isActive(pathname: string, href: string, exact?: boolean) {
  return exact ? pathname === href : pathname.startsWith(href);
}

function NavigationLink({ item, compact = false, tooltip = false }: { item: (typeof navigation)[number]; compact?: boolean; tooltip?: boolean }) {
  const pathname = usePathname();
  const Icon = item.icon;
  const active = isActive(pathname, item.href, "exact" in item ? item.exact : false);
  return (
    <Link className={cn("shell-nav__link", compact && "shell-nav__link--compact", active && "is-active")} href={item.href} aria-current={active ? "page" : undefined}>
      <Icon aria-hidden="true" />
      <span className="shell-nav__label">{item.label}</span>
      {tooltip && <span className="sidebar-tooltip" role="tooltip">{item.label}</span>}
    </Link>
  );
}

export function AppShell({ user, children }: { user: CurrentUser; children: ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const logout = useLogout();
  const [collapsed, setCollapsed] = useState(false);
  const profileMenu = useDismissibleDetails();
  const mobileMenu = useDismissibleDetails();
  const heading = titles[pathname] ?? titles["/carteira"];

  async function handleLogout() {
    await logout.mutateAsync();
    router.replace("/login");
  }

  return (
    <div className={cn("app-shell", collapsed && "app-shell--collapsed")}>
      <a className="skip-link" href="#conteudo-principal">Ir para o conteúdo</a>
      <aside className="desktop-sidebar" aria-label="Navegação financeira">
        <div className="brand">
          <span className="brand__mark" aria-hidden="true">V</span>
          <span className="brand__name">Valore</span>
        </div>
        <nav className="shell-nav" aria-label="Seções da carteira">
          {navigation.map((item) => <NavigationLink key={item.href} item={item} tooltip={collapsed} />)}
        </nav>
        <div className="sidebar-footer">
          <div id="desktop-operation-slot" className="desktop-operation-slot" />
          <Separator />
          <Button className="sidebar-collapse" variant="ghost" size="sm" aria-expanded={!collapsed} aria-label={collapsed ? "Expandir menu" : "Recolher menu"} onClick={() => setCollapsed((value) => !value)}>
            {collapsed ? <ChevronRight aria-hidden="true" /> : <ChevronLeft aria-hidden="true" />}
            {!collapsed && <span className="sidebar-collapse__label">Recolher</span>}
            {collapsed && <span className="sidebar-tooltip" role="tooltip">Expandir menu</span>}
          </Button>
        </div>
      </aside>

      <div className="shell-body">
        <header className="shell-header">
          <div className="shell-heading"><span>{heading.eyebrow}</span><h1>{heading.title}</h1></div>
          <div className="shell-actions">
            <ThemeToggle />
            <details className="profile-menu" ref={profileMenu}>
              <summary aria-label={"Abrir perfil de " + user.nome}>
                <CircleUserRound aria-hidden="true" />
                <span className="profile-menu__identity"><strong>{user.nome}</strong><small>{user.email}</small></span>
                <ChevronDown aria-hidden="true" />
              </summary>
              <div className="profile-menu__popover">
                <p><strong>{user.nome}</strong><span>{user.email}</span></p>
                <Link className="profile-menu__action" href="/configuracoes"><Settings aria-hidden="true" /> Configurações</Link>
                <Button variant="ghost" onClick={handleLogout} disabled={logout.isPending}><LogOut aria-hidden="true" /> {logout.isPending ? "Saindo…" : "Sair"}</Button>
              </div>
            </details>
          </div>
        </header>
        <header className="mobile-header">
          <Link href="/carteira" className="brand" aria-label="Valore — Carteira"><span className="brand__mark" aria-hidden="true">V</span></Link>
          <div><span>{heading.eyebrow}</span><strong>{heading.title}</strong></div>
          <details className="mobile-menu" ref={mobileMenu}>
            <summary aria-label="Abrir preferências"><Menu aria-hidden="true" /></summary>
            <div className="mobile-menu__popover">
              <ThemeToggle />
              <span>{user.nome}</span>
              <Link className="profile-menu__action" href="/configuracoes"><Settings aria-hidden="true" /> Configurações</Link>
              <Button variant="ghost" size="sm" onClick={handleLogout}><LogOut aria-hidden="true" /> Sair</Button>
            </div>
          </details>
        </header>
        <main id="conteudo-principal" className="portfolio-main">{children}</main>
      </div>

      <OperationLauncher collapsed={collapsed} />
      <nav className="mobile-bottom-nav" aria-label="Navegação financeira móvel">
        {navigation.slice(0, 3).map((item) => <NavigationLink key={item.href} item={item} compact />)}
        <details className="more-navigation">
          <summary className={cn("shell-nav__link shell-nav__link--compact", pathname === "/carteira/movimentacoes" && "is-active")} aria-label="Mais opções">
            <Ellipsis aria-hidden="true" /><span>Mais</span>
          </summary>
          <div className="more-navigation__popover">{navigation.slice(3).map((item) => <NavigationLink key={item.href} item={item} />)}<div id="mobile-operation-slot" /></div>
        </details>
      </nav>
    </div>
  );
}
