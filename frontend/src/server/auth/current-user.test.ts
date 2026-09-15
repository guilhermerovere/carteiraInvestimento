import { readFileSync } from "node:fs";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { BackendHttpError, BackendNetworkError } from "./types";

const { meMock, cookiesMock } = vi.hoisted(() => ({ meMock: vi.fn(), cookiesMock: vi.fn() }));
vi.mock("./backend-client", () => ({ backendClient: { me: meMock } }));
vi.mock("next/headers", () => ({ cookies: cookiesMock }));

import { resolveCurrentUser, resolveCurrentUserFromSessionCookie } from "./current-user";

describe("resolvedor server-only do usuário atual", () => {
  beforeEach(() => { meMock.mockReset(); cookiesMock.mockReset(); });
  afterEach(() => vi.clearAllMocks());

  it("encaminha somente o token explícito ao backend e devolve usuário seguro", async () => {
    meMock.mockResolvedValue({ id: "u1", nome: "Usuário", email: "user@example.test", role: "ROLE_USER", ativo: true, accessToken: "leak", senha: "leak", internal: true });
    await expect(resolveCurrentUser("jwt-explicit")).resolves.toEqual({ id: "u1", nome: "Usuário", email: "user@example.test", role: "ROLE_USER", ativo: true });
    expect(meMock).toHaveBeenCalledWith("jwt-explicit");
  });

  it.each([
    [new BackendHttpError(401, { status: 401, title: "Não autenticado" }, "corr-401", "problem-detail"), 401],
    [new BackendHttpError(403, { status: 403, title: "Proibido" }, "corr-403", "problem-detail"), 403],
    [new BackendNetworkError(), undefined],
  ])("preserva a classificação segura de erro", async (failure, status) => {
    meMock.mockRejectedValue(failure);
    const error = await resolveCurrentUser("jwt-explicit").catch((cause: unknown) => cause);
    expect(error).toBe(failure);
    if (status) expect(error).toMatchObject({ status, correlationId: `corr-${status}` });
    expect(JSON.stringify(error)).not.toContain("jwt-explicit");
  });

  it("o adaptador de cookie só fornece token ao resolvedor e não apaga sessão", async () => {
    cookiesMock.mockResolvedValue({ get: vi.fn().mockReturnValue({ value: "cookie-jwt" }) });
    meMock.mockResolvedValue({ id: "u1", nome: "Usuário", email: "user@example.test", role: "ROLE_USER", ativo: true });
    await resolveCurrentUserFromSessionCookie();
    expect(meMock).toHaveBeenCalledWith("cookie-jwt");
    expect(cookiesMock).not.toHaveBeenCalledWith(expect.anything());
  });

  it("não chama backend sem cookie", async () => {
    cookiesMock.mockResolvedValue({ get: vi.fn().mockReturnValue(undefined) });
    await expect(resolveCurrentUserFromSessionCookie()).resolves.toBeNull();
    expect(meMock).not.toHaveBeenCalled();
  });

  it("não decodifica JWT e mantém a fronteira server-only", () => {
    const source = readFileSync(join(process.cwd(), "src/server/auth/current-user.ts"), "utf8");
    expect(source).toMatch(/^import "server-only";/);
    expect(source).not.toMatch(/decode|jsonwebtoken|jose|Authorization/);
  });
});
