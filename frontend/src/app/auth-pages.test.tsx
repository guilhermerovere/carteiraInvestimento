import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

const { redirect } = vi.hoisted(() => ({ redirect: vi.fn(() => { throw new Error("NEXT_REDIRECT"); }) }));
vi.mock("next/navigation", () => ({ redirect }));
vi.mock("./login/login-form", () => ({ LoginForm: () => <form aria-label="login-form" /> }));
vi.mock("./register/register-form", () => ({ RegisterForm: () => <form aria-label="register-form" /> }));

import LoginPage from "./login/page";
import RegisterPage from "./register/page";
import InicioPage from "./(protected)/inicio/page";
import AdminPage from "./(protected)/admin/page";
import AccessDeniedPage from "./acesso-negado/page";

describe("páginas neutras de autenticação", () => {
  it("mantém login e cadastro públicos", () => {
    const { unmount } = render(<LoginPage />);
    expect(screen.getByRole("heading", { name: "Entrar" })).toBeInTheDocument();
    expect(screen.getByLabelText("login-form")).toBeInTheDocument();
    unmount();
    render(<RegisterPage />);
    expect(screen.getByRole("heading", { name: "Criar conta" })).toBeInTheDocument();
    expect(screen.getByLabelText("register-form")).toBeInTheDocument();
  });

  it("mantém admin e acesso negado legíveis", () => {
    render(<><AdminPage /><AccessDeniedPage /></>);
    expect(screen.getByRole("heading", { name: "Administração" })).toBeInTheDocument();
    expect(screen.getByText(/sessão permanece ativa/i)).toBeInTheDocument();
  });

  it("redireciona /inicio no servidor para a carteira", () => {
    expect(() => InicioPage()).toThrow("NEXT_REDIRECT");
    expect(redirect).toHaveBeenCalledWith("/carteira");
  });

  it("não expõe domínios financeiros na autenticação e administração", () => {
    render(<><AdminPage /><AccessDeniedPage /></>);
    expect(document.body.textContent).not.toMatch(/patrimônio|saldo|ativos|cotação|transação|auditoria/i);
  });
});
