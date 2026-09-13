import { beforeEach, describe, expect, it, vi } from "vitest";

const { redirectMock, resolveMock } = vi.hoisted(() => ({ redirectMock: vi.fn(), resolveMock: vi.fn() }));
vi.mock("next/navigation", () => ({ redirect: redirectMock }));
vi.mock("@/server/auth/current-user", () => ({ resolveCurrentUserFromSessionCookie: resolveMock }));

import Home from "./page";

describe("landing raiz server-side", () => {
  beforeEach(() => {
    resolveMock.mockReset();
    redirectMock.mockReset();
    redirectMock.mockImplementation((target: string) => { throw new Error(`NEXT_REDIRECT:${target}`); });
  });

  it("redireciona sessão ausente para /login", async () => {
    resolveMock.mockResolvedValue(null);
    await expect(Home()).rejects.toThrow("NEXT_REDIRECT:/login");
  });

  it.each([
    ["ROLE_USER", "/carteira"],
    ["ROLE_ADMIN", "/admin"],
  ] as const)("redireciona %s confirmado para %s", async (role, destination) => {
    resolveMock.mockResolvedValue({ id: "u1", nome: "Pessoa", email: "pessoa@example.test", role, ativo: true });
    await expect(Home()).rejects.toThrow(`NEXT_REDIRECT:${destination}`);
  });
});
