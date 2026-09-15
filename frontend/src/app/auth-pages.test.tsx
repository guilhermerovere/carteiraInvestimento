import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

const { redirect } = vi.hoisted(() => ({ redirect: vi.fn(() => { throw new Error("NEXT_REDIRECT"); }) }));
vi.mock("next/navigation", () => ({ redirect }));
vi.mock("./login/login-form", () => ({ LoginForm: () => <form aria-label="login-form" /> }));
vi.mock("./register/register-form", () => ({ RegisterForm: () => <form aria-label="register-form" /> }));
vi.mock("@/components/theme-toggle", () => ({ ThemeToggle: () => <button aria-label="Tema da interface">Tema</button> }));

import LoginPage from "./login/page";
import RegisterPage from "./register/page";
import InicioPage from "./(protected)/inicio/page";
import AdminPage from "./(protected)/admin/page";
import AccessDeniedPage from "./acesso-negado/page";

describe("páginas Valore de autenticação", () => {
  afterEach(cleanup);

  it("mantém login e cadastro públicos, coerentes e sem capacidades falsas", () => {
    const { unmount } = render(<LoginPage />);
    expect(screen.getByRole("heading", { name: "Bem-vindo de volta" })).toBeInTheDocument();
    expect(screen.getAllByText("Valore").length).toBeGreaterThan(0);
    expect(screen.getByText(/Invista com clareza/i)).toBeInTheDocument();
    expect(screen.getByLabelText("login-form")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Tema da interface" })).toBeInTheDocument();
    expect(document.body.textContent).not.toMatch(/Google|Facebook|Apple|Esqueceu a senha/i);
    unmount();
    render(<RegisterPage />);
    expect(screen.getByRole("heading", { name: "Criar conta" })).toBeInTheDocument();
    expect(screen.getAllByText("Valore").length).toBeGreaterThan(0);
    expect(screen.getByText(/Construa uma visão melhor/i)).toBeInTheDocument();
    expect(screen.getByLabelText("register-form")).toBeInTheDocument();
  });

  it("mantém admin funcional e acesso negado legível", () => {
    render(<><AdminPage /><AccessDeniedPage /></>);
    expect(screen.getByRole("heading", { name: "Administração Valore" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Gerenciar corretoras" })).toHaveAttribute("href", "/admin/corretoras");
    expect(screen.getByRole("link", { name: "Gerenciar ativos" })).toHaveAttribute("href", "/admin/ativos");
    expect(screen.getByText(/sessão permanece ativa/i)).toBeInTheDocument();
  });

  it("redireciona /inicio no servidor para a carteira", () => {
    expect(() => InicioPage()).toThrow("NEXT_REDIRECT");
    expect(redirect).toHaveBeenCalledWith("/carteira");
  });

  it("não mistura carteira pessoal com a administração", () => {
    render(<><AdminPage /><AccessDeniedPage /></>);
    expect(document.body.textContent).not.toMatch(/patrimônio|saldo em caixa|comprar|vender|depositar|sacar/i);
  });
});
