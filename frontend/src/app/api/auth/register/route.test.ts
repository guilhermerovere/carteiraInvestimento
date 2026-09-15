import { beforeEach, describe, expect, it, vi } from "vitest";
import { BackendHttpError, BackendNetworkError } from "@/server/auth/types";

const { registerMock, originMock } = vi.hoisted(() => ({ registerMock: vi.fn(), originMock: vi.fn() }));
vi.mock("@/server/auth/backend-client", () => ({ backendClient: { register: registerMock } }));
vi.mock("@/server/auth/origin", () => ({ hasValidOrigin: originMock }));

import { POST } from "./route";

const request = (origin?: string) => new Request("http://app.test/api/auth/register", { method: "POST", headers: { "Content-Type": "application/json", ...(origin ? { Origin: origin } : {}) }, body: JSON.stringify({ nome: "Usuário", email: "user@example.test", senha: "Password1!", accessToken: "ignored" }) });

describe("POST /api/auth/register", () => {
  beforeEach(() => { registerMock.mockReset(); originMock.mockReset(); originMock.mockReturnValue(true); });

  it("encaminha somente o contrato, preserva 201 e não cria sessão", async () => {
    registerMock.mockResolvedValue({ id: "u1", nome: "Usuário", email: "user@example.test", role: "ROLE_USER", ativo: true, accessToken: "upstream-only" });
    const response = await POST(request("https://app.test"));
    expect(registerMock).toHaveBeenCalledWith({ nome: "Usuário", email: "user@example.test", senha: "Password1!" });
    expect(response.status).toBe(201);
    expect(response.headers.get("cache-control")).toBe("no-store");
    expect(response.headers.get("set-cookie")).toBeNull();
    expect(response.headers.get("authorization")).toBeNull();
    expect(await response.text()).toBe("");
  });

  it.each([undefined, "https://evil.test"]) ("rejeita Origin %s sem upstream", async (origin) => {
    originMock.mockReturnValue(false);
    const response = await POST(request(origin));
    expect(response.status).toBe(403);
    expect(registerMock).not.toHaveBeenCalled();
    expect(response.headers.get("set-cookie")).toBeNull();
  });

  it("preserva ProblemDetail e correlation ID", async () => {
    registerMock.mockRejectedValue(new BackendHttpError(409, { status: 409, title: "Conflito", detail: "E-mail em uso" }, "corr-register", "problem-detail"));
    const response = await POST(request("https://app.test"));
    expect(response.status).toBe(409);
    expect(response.headers.get("x-correlation-id")).toBe("corr-register");
    expect(await response.json()).toEqual({ status: 409, title: "Conflito", detail: "E-mail em uso", instance: undefined });
  });

  it("converte falha de rede em resposta segura", async () => {
    registerMock.mockRejectedValue(new BackendNetworkError());
    expect((await POST(request("https://app.test"))).status).toBe(503);
  });
});
