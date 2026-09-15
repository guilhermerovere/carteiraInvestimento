import { readFileSync } from "node:fs";
import { join } from "node:path";
import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";

const { pathname, replace, logout } = vi.hoisted(() => ({
  pathname: { value: "/carteira/posicoes" },
  replace: vi.fn(),
  logout: vi.fn().mockResolvedValue(undefined),
}));
vi.mock("next/navigation", () => ({ usePathname: () => pathname.value, useRouter: () => ({ replace }) }));
vi.mock("@/client/auth", () => ({ useLogout: () => ({ mutateAsync: logout, isPending: false }) }));
vi.mock("@/components/theme-toggle", () => ({ ThemeToggle: () => <button>Tema</button> }));

import { AppShell } from "./app-shell";

const user = { id: "user-1", nome: "Ada Lovelace", email: "ada@example.test", role: "ROLE_USER" as const, ativo: true };

describe("shell financeiro autenticado", () => {
  afterEach(cleanup);

  it("mantém landmarks, título contextual e navegação ativa no desktop e mobile", () => {
    render(<AppShell user={user}><section>conteúdo financeiro</section></AppShell>);
    expect(screen.getByRole("main")).toHaveTextContent("conteúdo financeiro");
    expect(screen.getByRole("complementary", { name: "Navegação financeira" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { level: 1, name: "Posições" })).toBeInTheDocument();
    const positionLinks = screen.getAllByRole("link", { name: "Posições" });
    expect(positionLinks.every((link) => link.getAttribute("aria-current") === "page")).toBe(true);
    expect(screen.getAllByRole("link", { name: "Carteira" }).every((link) => !link.hasAttribute("aria-current"))).toBe(true);
    expect(screen.getByRole("navigation", { name: "Navegação financeira móvel" })).toBeInTheDocument();
    expect(screen.getByText("Mais")).toBeInTheDocument();
    expect(screen.getByText("Valore")).toBeInTheDocument();
    expect(positionLinks[0]).toHaveClass("is-active");
  });

  it("recolhe, expande e recolhe por teclado sem remover o foco do controle", async () => {
    render(<AppShell user={user}><span>conteúdo</span></AppShell>);
    const interaction = userEvent.setup();
    const button = screen.getByRole("button", { name: "Recolher menu" });
    button.focus();
    await interaction.keyboard("{Enter}");
    const expand = screen.getByRole("button", { name: "Expandir menu" });
    expect(expand).toHaveFocus();
    expect(expand.closest(".app-shell")).toHaveClass("app-shell--collapsed");
    expect(screen.getAllByRole("tooltip").map((item) => item.textContent)).toEqual(["Carteira", "Posições", "Ativos", "Transações", "Movimentações", "Operar", "Expandir menu"]);
    await interaction.keyboard(" ");
    const collapseAgain = screen.getByRole("button", { name: "Recolher menu" });
    expect(collapseAgain).toHaveFocus();
    expect(screen.queryByRole("tooltip")).not.toBeInTheDocument();
    await interaction.keyboard("{Enter}");
    expect(screen.getByRole("button", { name: "Expandir menu" })).toHaveFocus();
  });

  it("usa o logout existente e oferece o launcher financeiro ROLE_USER", async () => {
    render(<AppShell user={user}><span>conteúdo</span></AppShell>);
    await userEvent.click(screen.getAllByRole("button", { name: "Sair" })[0]);
    expect(logout).toHaveBeenCalledOnce();
    expect(replace).toHaveBeenCalledWith("/login");
    expect(document.body.textContent).toMatch(/comprar|depositar|sacar/i);
    expect(document.body.textContent).not.toMatch(/vender/i);
  });

  it("posiciona Operar no rodapé da sidebar, acima de Recolher, e o integra ao menu Mais móvel", async () => {
    render(<AppShell user={user}><span>conteúdo</span></AppShell>);
    const sidebar = screen.getByRole("complementary", { name: "Navegação financeira" });
    await waitFor(() => expect(sidebar.querySelector('summary[aria-label="Operar"]')).not.toBeNull());
    const operate = sidebar.querySelector<HTMLElement>('summary[aria-label="Operar"]');
    const collapse = screen.getByRole("button", { name: "Recolher menu" });
    expect(operate).not.toBeNull();
    expect(operate!.compareDocumentPosition(collapse) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
    expect(document.querySelector(".shell-header")?.contains(operate)).toBe(false);
    const more = screen.getByText("Mais").closest("details");
    expect(more?.querySelector("#mobile-operation-slot summary[aria-label='Operar']")).not.toBeNull();
  });

  it("abre Operar recolhido como popover lateral compacto e mantém ações por teclado", async () => {
    render(<AppShell user={user}><span>conteúdo</span></AppShell>);
    const interaction = userEvent.setup();
    await interaction.click(screen.getByRole("button", { name: "Recolher menu" }));
    const operate = document.querySelector<HTMLElement>(".desktop-sidebar summary[aria-label='Operar']");
    expect(operate).not.toBeNull();
    await interaction.click(operate!);
    const menu = within(operate!.parentElement!).getByRole("menu", { name: "Operações financeiras" });
    expect(menu).toBeVisible();
    expect(menu).toHaveClass("operation-launcher__menu");
    expect(operate!.parentElement).toHaveAttribute("open");
    expect(within(menu).getByRole("menuitem", { name: "Comprar" })).toBeVisible();
    await interaction.keyboard("{Escape}");
    expect(operate).toHaveFocus();
    expect(operate!.parentElement).not.toHaveAttribute("open");
  });

  it("abre o perfil por teclado, oferece Configurações e fecha com Escape restaurando o foco", async () => {
    render(<AppShell user={user}><span>conteúdo</span></AppShell>);
    const interaction = userEvent.setup();
    const summary = screen.getByText("Ada Lovelace", { selector: "summary strong" }).closest("summary")!;
    summary.focus(); await interaction.keyboard("{Enter}");
    const settings = within(summary.parentElement!).getByRole("link", { name: /Configurações/ });
    expect(settings).toHaveAttribute("href", "/configuracoes");
    await interaction.keyboard("{Escape}");
    expect(summary.parentElement).not.toHaveAttribute("open");
    expect(summary).toHaveFocus();
  });

  it("preserva boundaries do layout, tokens, foco, reduced motion e safe areas", () => {
    const rootLayout = readFileSync(join(process.cwd(), "src/app/layout.tsx"), "utf8");
    const portfolioLayout = readFileSync(join(process.cwd(), "src/app/(protected)/(portfolio)/layout.tsx"), "utf8");
    const css = readFileSync(join(process.cwd(), "src/app/globals.css"), "utf8");
    expect(rootLayout.match(/<QueryProvider/g)).toHaveLength(1);
    expect(rootLayout.match(/<ThemeProvider/g)).toHaveLength(1);
    expect(rootLayout.match(/suppressHydrationWarning/g)).toHaveLength(1);
    expect(portfolioLayout).toContain('requireRole("ROLE_USER", "/carteira")');
    expect(portfolioLayout).not.toContain('"use client"');
    for (const token of ["background", "foreground", "card", "popover", "primary", "secondary", "muted", "accent", "border", "input", "ring", "destructive", "success", "warning", "info"]) {
      expect(css).toContain("--" + token + ":");
    }
    expect(css).toMatch(/:focus-visible/);
    expect(css).toMatch(/prefers-reduced-motion:\s*reduce/);
    expect(css).toContain("safe-area-inset-bottom");
    expect(css).toMatch(/\.operation-dialog\s*\{[^}]*max-width:\s*38rem[^}]*margin:\s*auto/);
    expect(css).toMatch(/@media\s*\(max-width:\s*720px\)[\s\S]*\.operation-dialog/);
    const financeComponents = readFileSync(join(process.cwd(), "src/components/finance/summary-panel.tsx"), "utf8") + readFileSync(join(process.cwd(), "src/components/finance/positions-panel.tsx"), "utf8");
    expect(financeComponents).not.toMatch(/#[0-9a-f]{3,8}|rgb\(/i);
  });
});
