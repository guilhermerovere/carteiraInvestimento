import { readFileSync } from "node:fs";
import { join } from "node:path";
import { cleanup, render, screen } from "@testing-library/react";
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
    expect(screen.getAllByRole("tooltip").map((item) => item.textContent)).toEqual(["Carteira", "Posições", "Transações", "Movimentações", "Expandir menu"]);
    await interaction.keyboard(" ");
    const collapseAgain = screen.getByRole("button", { name: "Recolher menu" });
    expect(collapseAgain).toHaveFocus();
    expect(screen.queryByRole("tooltip")).not.toBeInTheDocument();
    await interaction.keyboard("{Enter}");
    expect(screen.getByRole("button", { name: "Expandir menu" })).toHaveFocus();
  });

  it("usa o logout existente e não oferece operações financeiras", async () => {
    render(<AppShell user={user}><span>conteúdo</span></AppShell>);
    await userEvent.click(screen.getAllByRole("button", { name: "Sair" })[0]);
    expect(logout).toHaveBeenCalledOnce();
    expect(replace).toHaveBeenCalledWith("/login");
    expect(document.body.textContent).not.toMatch(/comprar|vender|depositar|sacar/i);
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
    const financeComponents = readFileSync(join(process.cwd(), "src/components/finance/summary-panel.tsx"), "utf8") + readFileSync(join(process.cwd(), "src/components/finance/positions-panel.tsx"), "utf8");
    expect(financeComponents).not.toMatch(/#[0-9a-f]{3,8}|rgb\(/i);
  });
});
