import { beforeEach, describe, expect, it, vi } from "vitest";

const { backendRequest, validOrigin } = vi.hoisted(() => ({ backendRequest: vi.fn(), validOrigin: vi.fn(() => true) }));
vi.mock("@/server/finance/transport", async (load) => { const actual = await load<typeof import("@/server/finance/transport")>(); return { ...actual, financeBackendRequest: backendRequest }; });
vi.mock("@/server/auth/origin", () => ({ hasValidOrigin: validOrigin }));
import { GET as getBrokers, POST as postBroker } from "./brokers/route";
import { PATCH as patchBroker } from "./brokers/[id]/route";
import { PATCH as patchBrokerLifecycle } from "./brokers/[id]/lifecycle/route";
import { GET as getAssets, POST as postAsset } from "./assets/route";
import { PATCH as patchAsset } from "./assets/[id]/route";
import { PATCH as patchAssetLifecycle } from "./assets/[id]/lifecycle/route";

const id = "11111111-1111-4111-8111-111111111111";
const context = { params: Promise.resolve({ id }) };
const request = (path: string, method = "GET", body?: unknown) => new Request("https://app.test" + path, { method, headers: { Origin: "https://app.test", ...(body ? { "Content-Type": "application/json" } : {}) }, ...(body ? { body: JSON.stringify(body) } : {}) });

describe("BFF administrativo", () => {
  beforeEach(() => { backendRequest.mockReset(); validOrigin.mockReset(); validOrigin.mockReturnValue(true); backendRequest.mockResolvedValue({ data: { id }, correlationId: "corr-admin" }); });

  it("lista corretoras e ativos com paginação permitida", async () => {
    expect((await getBrokers(request("/api/admin/brokers?page=1&size=50"))).status).toBe(200); expect(backendRequest).toHaveBeenLastCalledWith("/api/v1/corretoras?page=1&size=50", expect.anything());
    expect((await getAssets(request("/api/admin/assets?page=0&size=20&sort=ticker&direction=asc"))).status).toBe(200); expect(backendRequest).toHaveBeenLastCalledWith("/api/v1/acoes?page=0&size=20&sort=ticker&direction=asc", expect.anything());
    expect((await getAssets(request("/api/admin/assets?secret=x"))).status).toBe(400);
  });

  it("cadastra corretora somente com os campos do contrato", async () => {
    const response = await postBroker(request("/api/admin/brokers", "POST", { cnpj: "00.000.000/0001-91", numero: "10", complemento: null })); expect(response.status).toBe(200);
    expect(backendRequest).toHaveBeenCalledWith("/api/v1/corretoras", expect.objectContaining({ method: "POST", body: JSON.stringify({ cnpj: "00.000.000/0001-91", numero: "10", complemento: null }) }));
    expect((await postBroker(request("/api/admin/brokers", "POST", { cnpj: "1", logo: "https://unsafe" }))).status).toBe(400);
  });

  it("edita e altera o ciclo de corretora sem permitir identidade estrutural", async () => {
    expect((await patchBroker(request(`/api/admin/brokers/${id}`, "PATCH", { numero: "20" }), context)).status).toBe(200); expect(backendRequest).toHaveBeenLastCalledWith(`/api/v1/corretoras/${id}`, expect.objectContaining({ method: "PATCH", body: JSON.stringify({ numero: "20" }) }));
    expect((await patchBrokerLifecycle(request(`/api/admin/brokers/${id}/lifecycle`, "PATCH", { ativo: false }), context)).status).toBe(200); expect(backendRequest).toHaveBeenLastCalledWith(`/api/v1/corretoras/${id}/ativo`, expect.objectContaining({ body: JSON.stringify({ ativo: false }) }));
  });

  it("cadastra, renomeia e altera o ciclo de ativo", async () => {
    expect((await postAsset(request("/api/admin/assets", "POST", { ticker: "ACME3", nome: "Acme", tipo: "ACAO", mercado: "B3" }))).status).toBe(200); expect(backendRequest).toHaveBeenLastCalledWith("/api/v1/acoes", expect.objectContaining({ method: "POST" }));
    expect((await patchAsset(request(`/api/admin/assets/${id}`, "PATCH", { nome: "Acme Nova" }), context)).status).toBe(200); expect(backendRequest).toHaveBeenLastCalledWith(`/api/v1/acoes/${id}`, expect.objectContaining({ body: JSON.stringify({ nome: "Acme Nova" }) }));
    expect((await patchAssetLifecycle(request(`/api/admin/assets/${id}/lifecycle`, "PATCH", { ativo: false }), context)).status).toBe(200); expect(backendRequest).toHaveBeenLastCalledWith(`/api/v1/acoes/${id}/ativo`, expect.objectContaining({ body: JSON.stringify({ ativo: false }) }));
  });

  it("rejeita Origin inválida e query em mutação", async () => {
    validOrigin.mockReturnValue(false); expect((await postAsset(request("/api/admin/assets", "POST", { ticker: "A", nome: "A", tipo: "ACAO", mercado: "B3" }))).status).toBe(400);
    validOrigin.mockReturnValue(true); expect((await postBroker(request("/api/admin/brokers?debug=true", "POST", { cnpj: "1" }))).status).toBe(400);
    expect(backendRequest).not.toHaveBeenCalled();
  });
});
