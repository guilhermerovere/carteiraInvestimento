import { readFileSync } from "node:fs";
import { join } from "node:path";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { BackendHttpError } from "./types";
const { redirectMock, resolveMock } = vi.hoisted(() => ({ redirectMock: vi.fn(), resolveMock: vi.fn() }));
vi.mock("next/navigation", () => ({ redirect: redirectMock }));
vi.mock("./current-user", () => ({ resolveCurrentUserFromSessionCookie: resolveMock }));
import { requireCurrentUser, requireRole } from "./guard";
const user = { id: "u1", nome: "Usuário", email: "user@example.test", role: "ROLE_USER" as const, ativo: true };
describe("guards server-only de autenticação", () => {
  beforeEach(() => { resolveMock.mockReset(); redirectMock.mockReset(); redirectMock.mockImplementation((target: string) => { throw new Error(`NEXT_REDIRECT:${target}`); }); });
  it("trata cookie ausente como não autenticado e mantém returnTo interno", async () => { resolveMock.mockResolvedValue(null); await expect(requireCurrentUser("/inicio")).rejects.toThrow("NEXT_REDIRECT:/login?returnTo=%2Finicio"); expect(resolveMock).toHaveBeenCalledTimes(1); });
  it("usa o usuário confirmado pelo resolvedor", async () => { resolveMock.mockResolvedValue(user); await expect(requireCurrentUser("/inicio")).resolves.toEqual(user); });
  it("trata 401 com cookie residual como login sem apagar cookie ou criar loop", async () => { resolveMock.mockRejectedValue(new BackendHttpError(401, { status: 401, title: "Não autenticado" })); await expect(requireCurrentUser("/admin")).rejects.toThrow("NEXT_REDIRECT:/login?returnTo=%2Fadmin"); expect(redirectMock).toHaveBeenCalledTimes(1); });
  it("aceita ROLE_USER e ROLE_ADMIN somente após confirmação", async () => { resolveMock.mockResolvedValueOnce(user).mockResolvedValueOnce({ ...user, role: "ROLE_ADMIN" }); await expect(requireRole("ROLE_USER", "/inicio")).resolves.toMatchObject({ role: "ROLE_USER" }); await expect(requireRole("ROLE_ADMIN", "/admin")).resolves.toMatchObject({ role: "ROLE_ADMIN" }); });
  it("preserva sessão em 403 ou role insuficiente e conduz a acesso negado", async () => { resolveMock.mockRejectedValueOnce(new BackendHttpError(403, { status: 403, title: "Proibido" })); await expect(requireCurrentUser("/inicio")).rejects.toThrow("NEXT_REDIRECT:/acesso-negado"); resolveMock.mockResolvedValueOnce(user); await expect(requireRole("ROLE_ADMIN", "/admin")).rejects.toThrow("NEXT_REDIRECT:/acesso-negado"); });
  it("chama diretamente o resolvedor sem HTTP Next-para-Next, decode, escrita de cookie ou logout", () => { const source = readFileSync(join(process.cwd(), "src/server/auth/guard.ts"), "utf8"); expect(source).toMatch(/^import "server-only";/); expect(source).toMatch(/resolveCurrentUserFromSessionCookie/); expect(source).not.toMatch(/fetch\(|\/api\/auth\/me|decode|jsonwebtoken|jose|expiredAuthCookie|cookies\.(set|delete)|logout/i); });
  it("conecta cada layout protegido ao guard de role correspondente", () => { const inicio = readFileSync(join(process.cwd(), "src/app/(protected)/inicio/layout.tsx"), "utf8"); const admin = readFileSync(join(process.cwd(), "src/app/(protected)/admin/layout.tsx"), "utf8"); expect(inicio).toMatch(/requireRole\("ROLE_USER", "\/inicio"\)/); expect(admin).toMatch(/requireRole\("ROLE_ADMIN", "\/admin"\)/); });
});
