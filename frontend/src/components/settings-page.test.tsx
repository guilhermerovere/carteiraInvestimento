import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const { router, currentUser, updateName, updateEmail, changePassword, close, cash, positions } = vi.hoisted(() => ({
  router: { replace: vi.fn(), refresh: vi.fn() },
  currentUser: { value: { id: "user-1", nome: "Ada", email: "ada@example.test", role: "ROLE_USER" as const, ativo: true } },
  updateName: vi.fn(), updateEmail: vi.fn(), changePassword: vi.fn(), close: vi.fn(), cash: vi.fn(), positions: vi.fn(),
}));
vi.mock("next/navigation", () => ({ useRouter: () => router }));
vi.mock("@/client/auth", () => ({ authMeKey: ["auth", "me"], useCurrentUser: () => ({ data: currentUser.value, isPending: false }) }));
vi.mock("@/client/management-api", () => ({ accountApi: { updateName, updateEmail, changePassword, close } }));
vi.mock("@/client/finance-api", async (load) => { const actual = await load<typeof import("@/client/finance-api")>(); return { ...actual, financeApi: { ...actual.financeApi, cash, positions } }; });

import { FinanceApiError } from "@/client/finance-api";
import { SettingsPage } from "./settings-page";

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return render(<QueryClientProvider client={client}><SettingsPage /></QueryClientProvider>);
}

describe("configurações da conta", () => {
  beforeEach(() => {
    vi.clearAllMocks(); sessionStorage.clear();
    currentUser.value = { id: "user-1", nome: "Ada", email: "ada@example.test", role: "ROLE_USER", ativo: true };
    cash.mockResolvedValue({ saldoCaixaBrl: "0.00000000" });
    positions.mockResolvedValue({ items: [], page: 0, size: 1, totalElements: 0, totalPages: 0 });
    Object.defineProperty(HTMLDialogElement.prototype, "showModal", { configurable: true, value() { this.setAttribute("open", ""); } });
    Object.defineProperty(HTMLDialogElement.prototype, "close", { configurable: true, value() { this.removeAttribute("open"); } });
  });
  afterEach(cleanup);

  it("atualiza nome e email exibidos sem renderizar resposta técnica", async () => {
    updateName.mockResolvedValue({ ...currentUser.value, nome: "Ada Lovelace" });
    updateEmail.mockResolvedValue({ ...currentUser.value, nome: "Ada Lovelace", email: "novo@example.test" });
    renderPage();
    const user = userEvent.setup();
    const name = screen.getByLabelText(/^Nome/); await user.clear(name); await user.type(name, " Ada Lovelace "); await user.click(screen.getByRole("button", { name: "Salvar nome" }));
    expect(await screen.findByText("Nome atualizado com sucesso.")).toBeVisible(); expect(updateName).toHaveBeenCalledWith(" Ada Lovelace ");
    const email = screen.getByLabelText("Email"); await user.clear(email); await user.type(email, "NOVO@EXAMPLE.TEST"); await user.click(screen.getByRole("button", { name: "Salvar email" }));
    expect(await screen.findByText("Email atualizado com sucesso.")).toBeVisible(); expect(updateEmail).toHaveBeenCalledWith("NOVO@EXAMPLE.TEST");
    expect(document.body.textContent).not.toMatch(/[{}]|"id"|saldoCaixaBrl/);
  });

  it("traduz email duplicado e senha atual incorreta", async () => {
    updateEmail.mockRejectedValue(new FinanceApiError(409, { status: 409, title: "Conflict", detail: "technical", code: "EMAIL_IN_USE" }));
    changePassword.mockRejectedValue(new FinanceApiError(400, { status: 400, title: "Bad password", code: "CURRENT_PASSWORD_INCORRECT" }));
    renderPage(); const user = userEvent.setup();
    await user.click(screen.getByRole("button", { name: "Salvar email" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Este email já está em uso.");
    await user.type(screen.getAllByLabelText("Senha atual")[0], "Errada123!"); await user.type(screen.getByLabelText(/^Nova senha/), "NovaSenha123!"); await user.type(screen.getByLabelText("Confirmar nova senha"), "NovaSenha123!"); await user.click(screen.getByRole("button", { name: "Alterar senha" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("A senha atual está incorreta.");
    expect(document.body).not.toHaveTextContent("Bad password");
  });

  it("altera a senha, limpa os campos e exige novo login", async () => {
    changePassword.mockResolvedValue(undefined); renderPage(); const user = userEvent.setup();
    await user.type(screen.getAllByLabelText("Senha atual")[0], "Atual123!"); await user.type(screen.getByLabelText(/^Nova senha/), "NovaSenha123!"); await user.type(screen.getByLabelText("Confirmar nova senha"), "NovaSenha123!"); await user.click(screen.getByRole("button", { name: "Alterar senha" }));
    expect(await screen.findByText("Senha alterada com sucesso.")).toBeVisible();
    expect(screen.getAllByLabelText("Senha atual")[0]).toHaveValue(""); expect(screen.getByLabelText(/^Nova senha/)).toHaveValue("");
    await waitFor(() => expect(router.replace).toHaveBeenCalledWith("/login"), { timeout: 1200 });
  });

  it("bloqueia a zona de perigo quando há caixa ou posições", async () => {
    cash.mockResolvedValue({ saldoCaixaBrl: "10.00" }); positions.mockResolvedValue({ items: [{}], page: 0, size: 1, totalElements: 1, totalPages: 1 }); renderPage();
    expect(await screen.findByText(/primeiro deixe o saldo em caixa/)).toBeVisible(); expect(screen.getByText(/Venda suas posições/)).toBeVisible(); expect(screen.getByRole("button", { name: "Excluir conta" })).toBeDisabled();
  });

  it("confirma o encerramento com senha e texto forte e encerra a sessão", async () => {
    close.mockResolvedValue(undefined); renderPage(); const user = userEvent.setup();
    const trigger = screen.getByRole("button", { name: "Excluir conta" }); await waitFor(() => expect(trigger).toBeEnabled()); await user.click(trigger);
    const passwordFields = screen.getAllByLabelText("Senha atual"); await user.type(passwordFields[1], "Atual123!"); await user.type(screen.getByLabelText("Digite EXCLUIR MINHA CONTA"), "EXCLUIR MINHA CONTA"); await user.click(screen.getByRole("button", { name: "Excluir minha conta" }));
    expect(close).toHaveBeenCalledWith("Atual123!", "EXCLUIR MINHA CONTA"); expect(await screen.findByText("Sua conta foi excluída com sucesso.")).toBeVisible();
    await waitFor(() => expect(router.replace).toHaveBeenCalledWith("/login"), { timeout: 1200 });
  });
});
