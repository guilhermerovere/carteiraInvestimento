import { cleanup, render as rtlRender, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { readFileSync } from "node:fs";
import { join } from "node:path";
import userEvent from "@testing-library/user-event";
import type { ReactNode } from "react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
const { replaceMock, searchMock } = vi.hoisted(() => ({ replaceMock: vi.fn(), searchMock: { value: null as string | null } }));
vi.mock("next/navigation", () => ({ useRouter: () => ({ replace: replaceMock }), useSearchParams: () => ({ get: () => searchMock.value }) }));
import { LoginForm } from "./login/login-form";
import { RegisterForm } from "./register/register-form";
const fetchMock = vi.fn<typeof fetch>();
async function fillLogin(user: ReturnType<typeof userEvent.setup>) { await user.type(screen.getByLabelText("E-mail"), "user@example.test"); await user.type(screen.getByLabelText("Senha"), "Password1!"); await user.click(screen.getByRole("button", { name: "Entrar" })); }
function render(ui: ReactNode) {
  return rtlRender(<QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })}>{ui}</QueryClientProvider>);
}
describe("formulários BFF same-origin", () => {
  beforeEach(() => { vi.stubGlobal("fetch", fetchMock); fetchMock.mockReset(); replaceMock.mockReset(); searchMock.value = null; });
  afterEach(() => { cleanup(); vi.unstubAllGlobals(); });
  it("login confirma a sessão antes de navegar para returnTo seguro", async () => { searchMock.value = "/admin"; fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 })).mockResolvedValueOnce(new Response(JSON.stringify({ id: "u1" }), { status: 200, headers: { "Content-Type": "application/json" } })); render(<LoginForm />); await fillLogin(userEvent.setup()); await waitFor(() => expect(replaceMock).toHaveBeenCalledWith("/admin")); expect(fetchMock.mock.calls.map(([path]) => path)).toEqual(["/api/auth/login", "/api/auth/me"]); });
  it("login usa o destino padrão para returnTo externo", async () => { searchMock.value = "https://evil.example"; fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 })).mockResolvedValueOnce(new Response("{}", { status: 200, headers: { "Content-Type": "application/json" } })); render(<LoginForm />); await fillLogin(userEvent.setup()); await waitFor(() => expect(replaceMock).toHaveBeenCalledWith("/inicio")); });
  it("login exibe ProblemDetail e correlation ID sem token", async () => { fetchMock.mockResolvedValue(new Response(JSON.stringify({ status: 401, title: "Não autorizado", detail: "Credenciais inválidas" }), { status: 401, headers: { "Content-Type": "application/problem+json", "X-Correlation-ID": "corr-login" } })); render(<LoginForm />); await fillLogin(userEvent.setup()); expect(await screen.findByRole("alert")).toHaveTextContent("Credenciais inválidas"); expect(screen.getByText("Código de suporte: corr-login")).toBeInTheDocument(); });
  it("login trata falha de rede de forma segura", async () => { fetchMock.mockRejectedValue(new Error("http://spring.internal")); render(<LoginForm />); await fillLogin(userEvent.setup()); expect(await screen.findByRole("alert")).toHaveTextContent("Não foi possível iniciar a sessão."); expect(document.body.textContent).not.toContain("spring.internal"); });
  it("cadastro navega para login sem confirmar sessão", async () => { fetchMock.mockResolvedValue(new Response(null, { status: 201 })); render(<RegisterForm />); const user = userEvent.setup(); await user.type(screen.getByLabelText("Nome"), "Usuário"); await user.type(screen.getByLabelText("E-mail"), "user@example.test"); await user.type(screen.getByLabelText("Senha"), "Password1!"); await user.click(screen.getByRole("button", { name: "Cadastrar" })); await waitFor(() => expect(replaceMock).toHaveBeenCalledWith("/login")); expect(fetchMock.mock.calls.map(([path]) => path)).toEqual(["/api/auth/register"]); });
  it("cadastro exibe erro seguro com correlation ID", async () => { fetchMock.mockResolvedValue(new Response(JSON.stringify({ status: 409, detail: "E-mail em uso" }), { status: 409, headers: { "Content-Type": "application/problem+json", "X-Correlation-ID": "corr-register" } })); render(<RegisterForm />); const user = userEvent.setup(); await user.type(screen.getByLabelText("Nome"), "Usuário"); await user.type(screen.getByLabelText("E-mail"), "user@example.test"); await user.type(screen.getByLabelText("Senha"), "Password1!"); await user.click(screen.getByRole("button", { name: "Cadastrar" })); expect(await screen.findByRole("alert")).toHaveTextContent("E-mail em uso"); expect(screen.getByText("Código de suporte: corr-register")).toBeInTheDocument(); });
  it("mantém formulários no cliente sem importar segredos ou módulos server-only", () => { const sources = ["src/app/login/login-form.tsx", "src/app/register/register-form.tsx", "src/client/auth-form-api.ts"].map((path) => readFileSync(join(process.cwd(), path), "utf8")).join("\n"); expect(sources).not.toMatch(/server\/auth|BACKEND_API_URL|accessToken|Authorization|auth_session|https?:\/\//i); });
});
