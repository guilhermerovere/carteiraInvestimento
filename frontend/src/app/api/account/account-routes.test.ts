import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const { backendRequest, validOrigin } = vi.hoisted(() => ({ backendRequest: vi.fn(), validOrigin: vi.fn(() => true) }));
vi.mock("@/server/finance/transport", async (load) => { const actual = await load<typeof import("@/server/finance/transport")>(); return { ...actual, financeBackendRequest: backendRequest }; });
vi.mock("@/server/auth/origin", () => ({ hasValidOrigin: validOrigin }));
import { PATCH as patchProfile } from "./profile/route";
import { POST as postPassword } from "./password/route";
import { POST as postClosure } from "./closure/route";

const request = (path: string, method: string, body: unknown) => new Request("https://app.test" + path, { method, headers: { Origin: "https://app.test", "Content-Type": "application/json" }, body: JSON.stringify(body) });

describe("BFF de configurações da conta", () => {
  beforeEach(() => { vi.stubEnv("BACKEND_API_URL", "http://spring.internal:8080"); vi.stubEnv("APP_ORIGIN", "https://app.test"); backendRequest.mockReset(); validOrigin.mockReset(); validOrigin.mockReturnValue(true); });
  afterEach(() => vi.unstubAllEnvs());

  it("normaliza nome e email e encaminha apenas o próprio perfil", async () => {
    backendRequest.mockResolvedValue({ data: { id: "user-1", nome: "Ada", email: "novo@example.test", role: "ROLE_USER", ativo: true }, correlationId: "corr" });
    const nameResponse = await patchProfile(request("/api/account/profile", "PATCH", { nome: "  Ada  " })); expect(nameResponse.status).toBe(200);
    expect(backendRequest).toHaveBeenLastCalledWith("/api/v1/account/profile", expect.objectContaining({ method: "PATCH", body: JSON.stringify({ nome: "Ada" }) }));
    const emailResponse = await patchProfile(request("/api/account/profile", "PATCH", { email: "  NOVO@EXAMPLE.TEST  " })); expect(emailResponse.status).toBe(200);
    expect(backendRequest).toHaveBeenLastCalledWith("/api/v1/account/profile", expect.objectContaining({ body: JSON.stringify({ email: "novo@example.test" }) }));
  });

  it("rejeita campos extras, múltiplas alterações e Origin inválida", async () => {
    expect((await patchProfile(request("/api/account/profile", "PATCH", { nome: "Ada", email: "ada@example.test" }))).status).toBe(400);
    expect((await patchProfile(request("/api/account/profile", "PATCH", { nome: "Ada", usuarioId: "outro" }))).status).toBe(400);
    validOrigin.mockReturnValue(false); expect((await patchProfile(request("/api/account/profile", "PATCH", { nome: "Ada" }))).status).toBe(400);
    expect(backendRequest).not.toHaveBeenCalled();
  });

  it("troca senha sem expor resposta e encerra a sessão BFF", async () => {
    backendRequest.mockResolvedValue({ data: undefined, correlationId: "corr-password" });
    const response = await postPassword(request("/api/account/password", "POST", { senhaAtual: "Atual123!", novaSenha: "Nova123!" }));
    expect(response.status).toBe(204); expect(await response.text()).toBe(""); expect(response.headers.get("set-cookie")).toMatch(/auth_session=;/i);
    expect(backendRequest).toHaveBeenCalledWith("/api/v1/account/password", expect.objectContaining({ body: JSON.stringify({ senhaAtual: "Atual123!", novaSenha: "Nova123!" }) }));
  });

  it("exige confirmação textual exata para encerrar e limpa o cookie", async () => {
    const invalid = await postClosure(request("/api/account/closure", "POST", { senhaAtual: "Atual123!", confirmacao: "sim" })); expect(invalid.status).toBe(400); expect(backendRequest).not.toHaveBeenCalled();
    backendRequest.mockResolvedValue({ data: undefined, correlationId: "corr-close" });
    const response = await postClosure(request("/api/account/closure", "POST", { senhaAtual: "Atual123!", confirmacao: "EXCLUIR MINHA CONTA" }));
    expect(response.status).toBe(204); expect(response.headers.get("set-cookie")).toMatch(/auth_session=;/i);
    expect(backendRequest).toHaveBeenCalledWith("/api/v1/account/closure", expect.objectContaining({ body: JSON.stringify({ senhaAtual: "Atual123!", confirmacao: "EXCLUIR MINHA CONTA" }) }));
  });
});
