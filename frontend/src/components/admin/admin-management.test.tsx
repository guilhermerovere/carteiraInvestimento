import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { FinanceApiError } from "@/client/finance-api";

const { api } = vi.hoisted(() => ({ api: { brokers: vi.fn(), createBroker: vi.fn(), editBroker: vi.fn(), setBrokerActive: vi.fn(), assets: vi.fn(), createAsset: vi.fn(), editAsset: vi.fn(), setAssetActive: vi.fn() } }));
vi.mock("@/client/management-api", () => ({ adminApi: api }));
import { BrokerManagement } from "./broker-management";
import { AssetManagement } from "./asset-management";

const broker = { id: "11111111-1111-4111-8111-111111111111", cnpj: "00000000000191", razaoSocial: "Corretora Exemplo S.A.", nomeFantasia: "Corretora Exemplo", cep: "01001000", logradouro: "Praça da Sé", bairro: "Sé", cidade: "São Paulo", uf: "SP", numero: "10", complemento: null, ativo: true, logoProvider: null, logoReference: null, criadoEm: "2026-01-01", atualizadoEm: "2026-01-01" };
const asset = { id: "22222222-2222-4222-8222-222222222222", ticker: "ACME3", nome: "Acme", tipo: "ACAO", mercado: "B3", moeda: "BRL", ativo: true, logoProvider: null, logoReference: null, criadoEm: "2026-01-01", atualizadoEm: "2026-01-01" };
const page = <T,>(item: T) => ({ items: [item], page: 0, size: 100, totalElements: 1, totalPages: 1 });
function wrapper(children: React.ReactNode) { return <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>{children}</QueryClientProvider>; }

describe("administração dos catálogos", () => {
  beforeEach(() => { vi.clearAllMocks(); api.brokers.mockResolvedValue(page(broker)); api.assets.mockResolvedValue(page(asset)); api.createBroker.mockResolvedValue(broker); api.editBroker.mockResolvedValue(broker); api.setBrokerActive.mockResolvedValue({ ...broker, ativo: false }); api.createAsset.mockResolvedValue(asset); api.editAsset.mockResolvedValue(asset); api.setAssetActive.mockResolvedValue({ ...asset, ativo: false }); vi.spyOn(window, "confirm").mockReturnValue(true); });
  afterEach(cleanup);
  it("aplica a máscara numérica ao CNPJ de estabelecimento e envia somente os 14 dígitos", async () => {
    render(wrapper(<BrokerManagement />)); const user = userEvent.setup(); await user.click(await screen.findByRole("button", { name: "Nova corretora" }));
    const input = screen.getByRole("textbox", { name: "CNPJ" }); await user.type(input, "02332886001682");
    expect(input).toHaveValue("02.332.886/0016-82"); expect(input).toHaveAttribute("inputmode", "numeric");
    await user.click(screen.getByRole("button", { name: "Cadastrar corretora" }));
    await waitFor(() => expect(api.createBroker).toHaveBeenCalledWith(expect.objectContaining({ cnpj: "02332886001682" })));
  });
  it("ignora letras e caracteres arbitrários e aceita paste com CNPJ mascarado", async () => {
    render(wrapper(<BrokerManagement />)); const user = userEvent.setup(); await user.click(await screen.findByRole("button", { name: "Nova corretora" }));
    const input = screen.getByRole("textbox", { name: "CNPJ" }); await user.type(input, "74abc!014747000135");
    expect(input).toHaveValue("74.014.747/0001-35"); expect((input as HTMLInputElement).value).not.toMatch(/[a-z!]/i);
    await user.clear(input); await user.click(input); await user.paste("74.014.747/0001-35");
    expect(input).toHaveValue("74.014.747/0001-35");
    await user.clear(input); await user.click(input); await user.paste("74014747000135");
    expect(input).toHaveValue("74.014.747/0001-35");
    await user.click(screen.getByRole("button", { name: "Cadastrar corretora" }));
    await waitFor(() => expect(api.createBroker).toHaveBeenCalledWith(expect.objectContaining({ cnpj: "74014747000135" })));
  });
  it("mantém backspace e delete naturais junto aos separadores da máscara", async () => {
    render(wrapper(<BrokerManagement />)); const user = userEvent.setup(); await user.click(await screen.findByRole("button", { name: "Nova corretora" }));
    const input = screen.getByRole("textbox", { name: "CNPJ" }) as HTMLInputElement; await user.type(input, "74014747000135"); input.setSelectionRange(3, 3); await user.keyboard("{Backspace}");
    expect(input).toHaveValue("70.147.470/0013-5");
    await user.clear(input); await user.type(input, "74014747000135"); input.setSelectionRange(2, 2); await user.keyboard("{Delete}");
    expect(input).toHaveValue("74.147.470/0013-5");
  });
  it("apresenta CNPJ inválido antes de chamar o backend", async () => {
    render(wrapper(<BrokerManagement />)); const user = userEvent.setup(); await user.click(await screen.findByRole("button", { name: "Nova corretora" }));
    const input = screen.getByRole("textbox", { name: "CNPJ" }); await user.type(input, "74014747000134"); await user.click(screen.getByRole("button", { name: "Cadastrar corretora" }));
    expect(screen.getByText("Informe um CNPJ válido.")).toBeInTheDocument(); expect(input).toHaveAttribute("aria-invalid", "true"); expect(api.createBroker).not.toHaveBeenCalled();
  });
  it("apresenta a rejeição identificada pela CVM e mantém 422 sem código genérico", async () => {
    const user = userEvent.setup();
    api.createBroker.mockRejectedValueOnce(new FinanceApiError(422, { status: 422, title: "Broker rejected", detail: "The broker did not pass regulatory validation.", code: "BROKER_CVM_NOT_REGISTERED" }));
    render(wrapper(<BrokerManagement />)); await user.click(await screen.findByRole("button", { name: "Nova corretora" }));
    await user.type(screen.getByRole("textbox", { name: "CNPJ" }), "74014747000135"); await user.click(screen.getByRole("button", { name: "Cadastrar corretora" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Este CNPJ não está cadastrado na CVM.");
    expect(document.body).not.toHaveTextContent("Broker rejected"); expect(document.body).not.toHaveTextContent("The broker did not pass regulatory validation.");
    api.createBroker.mockRejectedValueOnce(new FinanceApiError(422, { status: 422, title: "Unprocessable Entity", code: undefined }));
    await user.click(screen.getByRole("button", { name: "Cadastrar corretora" }));
    expect(await screen.findByRole("alert")).not.toHaveTextContent("Este CNPJ não está cadastrado na CVM.");
  });
  it("mostra a mensagem específica para corretora já cadastrada", async () => {
    api.createBroker.mockRejectedValueOnce(new FinanceApiError(409, { status: 409, title: "CNPJ already registered", detail: "A broker with this CNPJ already exists.", code: "BROKER_ALREADY_REGISTERED" }));
    const user = userEvent.setup(); render(wrapper(<BrokerManagement />)); await user.click(await screen.findByRole("button", { name: "Nova corretora" }));
    await user.type(screen.getByRole("textbox", { name: "CNPJ" }), "74014747000135"); await user.click(screen.getByRole("button", { name: "Cadastrar corretora" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Esta corretora já está cadastrada.");
    expect(document.body).not.toHaveTextContent("A broker with this CNPJ already exists.");
  });
  it("traduz indisponibilidade regulatória sem renderizar o ProblemDetail técnico", async () => {
    api.createBroker.mockRejectedValueOnce(new FinanceApiError(502, { status: 502, title: "Broker provider unavailable", detail: "Provider timeout", code: "BROKER_VALIDATION_UNAVAILABLE" }));
    const user = userEvent.setup(); render(wrapper(<BrokerManagement />)); await user.click(await screen.findByRole("button", { name: "Nova corretora" }));
    await user.type(screen.getByRole("textbox", { name: "CNPJ" }), "74014747000135"); await user.click(screen.getByRole("button", { name: "Cadastrar corretora" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Não foi possível validar a corretora agora. Tente novamente.");
    expect(document.body).not.toHaveTextContent("Broker provider unavailable"); expect(document.body).not.toHaveTextContent("Provider timeout");
  });
  it("usa diálogos compactos para criar e editar corretoras", async () => {
    render(wrapper(<BrokerManagement />)); const user = userEvent.setup(); expect(await screen.findByText("Corretora Exemplo")).toBeVisible(); expect(screen.queryByRole("textbox", { name: /logo/i })).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Nova corretora" })); expect(screen.getByRole("dialog", { name: "Nova corretora" })).toBeVisible(); await user.type(screen.getByLabelText("CNPJ"), "00.000.000/0001-91"); await user.click(screen.getByRole("button", { name: "Cadastrar corretora" })); await waitFor(() => expect(api.createBroker).toHaveBeenCalled());
    await user.click(screen.getByRole("button", { name: "Editar endereço" })); const number = screen.getByLabelText(/^Número/); await user.clear(number); await user.type(number, "20"); await user.click(screen.getByRole("button", { name: "Salvar endereço" })); await waitFor(() => expect(api.editBroker).toHaveBeenCalledWith(broker.id, expect.objectContaining({ numero: "20" })));
    await user.click(screen.getByRole("button", { name: "Desativar" })); expect(api.setBrokerActive).toHaveBeenCalledWith(broker.id, false);
  });
  it("usa diálogos compactos para criar e renomear ativos", async () => {
    render(wrapper(<AssetManagement />)); const user = userEvent.setup(); expect(await screen.findByText("ACME3")).toBeVisible(); await user.click(screen.getByRole("button", { name: "Adicionar ativo" })); await user.type(screen.getByLabelText("Ticker"), "XPTO3"); await user.click(screen.getByRole("button", { name: "Cadastrar ativo" })); await waitFor(() => expect(api.createAsset).toHaveBeenCalledWith({ ticker: "XPTO3", mercado: "B3" }));
    await user.click(screen.getByRole("button", { name: "Editar nome" })); const name = screen.getByLabelText("Nome"); await user.clear(name); await user.type(name, "Acme Nova"); await user.click(screen.getByRole("button", { name: "Salvar nome" })); await waitFor(() => expect(api.editAsset).toHaveBeenCalledWith(asset.id, "Acme Nova"));
    await user.click(screen.getByRole("button", { name: "Desativar" })); expect(api.setAssetActive).toHaveBeenCalledWith(asset.id, false); expect(document.body.textContent).not.toMatch(/[{}]|logoReference/);
  });
});
