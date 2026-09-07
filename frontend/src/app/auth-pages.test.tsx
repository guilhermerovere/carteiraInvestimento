import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
vi.mock("./login/login-form", () => ({ LoginForm: () => <form aria-label="login-form" /> }));
vi.mock("./register/register-form", () => ({ RegisterForm: () => <form aria-label="register-form" /> }));
import LoginPage from "./login/page";
import RegisterPage from "./register/page";
import InicioPage from "./(protected)/inicio/page";
import AdminPage from "./(protected)/admin/page";
import AccessDeniedPage from "./acesso-negado/page";
describe("páginas neutras de autenticação", () => {
  it("mantém login e cadastro públicos", () => { const { unmount } = render(<LoginPage />); expect(screen.getByRole("heading", { name: "Entrar" })).toBeInTheDocument(); expect(screen.getByLabelText("login-form")).toBeInTheDocument(); unmount(); render(<RegisterPage />); expect(screen.getByRole("heading", { name: "Criar conta" })).toBeInTheDocument(); expect(screen.getByLabelText("register-form")).toBeInTheDocument(); });
  it("renderiza as áreas neutras e preserva a sessão na tela de acesso negado", () => { const pages = [<InicioPage key="inicio" />, <AdminPage key="admin" />, <AccessDeniedPage key="denied" />]; render(<>{pages}</>); expect(screen.getByRole("heading", { name: "Início" })).toBeInTheDocument(); expect(screen.getByRole("heading", { name: "Administração" })).toBeInTheDocument(); expect(screen.getByText(/sessão permanece ativa/i)).toBeInTheDocument(); });
  it("não expõe domínios financeiros ou administrativos funcionais", () => { render(<><InicioPage /><AdminPage /><AccessDeniedPage /></>); expect(document.body.textContent).not.toMatch(/patrimônio|saldo|ativos|cotaç|transaç|auditoria/i); });
});
