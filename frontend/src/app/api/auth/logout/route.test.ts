import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { POST } from "./route";

const fetchMock = vi.fn<typeof fetch>();
const request = (origin?: string) => new Request("http://app.test/api/auth/logout", { method: "POST", headers: origin ? { Origin: origin } : undefined });

describe("POST /api/auth/logout", () => {
  beforeEach(() => {
    vi.stubEnv("BACKEND_API_URL", "http://spring.internal:8080");
    vi.stubEnv("APP_ORIGIN", "https://app.test");
    vi.stubGlobal("fetch", fetchMock);
    fetchMock.mockReset();
  });
  afterEach(() => { vi.unstubAllEnvs(); vi.unstubAllGlobals(); });

  it("remove somente auth_session com os atributos compartilhados e responde 204", async () => {
    const response = await POST(request("https://app.test"));
    const setCookie = response.headers.get("set-cookie") ?? "";
    expect(response.status).toBe(204);
    expect(response.headers.get("cache-control")).toBe("no-store");
    expect(await response.text()).toBe("");
    expect(setCookie).toMatch(/auth_session=; Path=\//i);
    expect(setCookie).toMatch(/Max-Age=0; Secure; HttpOnly; SameSite=Lax/i);
    expect(setCookie).not.toMatch(/Domain=/i);
    expect(response.headers.get("authorization")).toBeNull();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("usa Secure=false para APP_ORIGIN HTTP sem mudar os demais atributos", async () => {
    vi.stubEnv("APP_ORIGIN", "http://localhost:3000");
    const response = await POST(request("http://localhost:3000"));
    expect(response.headers.get("set-cookie")).toMatch(/Max-Age=0; HttpOnly; SameSite=Lax/i);
    expect(response.headers.get("set-cookie")).not.toMatch(/Secure/i);
  });

  it.each([undefined, "https://evil.test", "not-a-url"]) ("rejeita Origin %s sem chamar backend ou alterar sessão", async (origin) => {
    const response = await POST(request(origin));
    expect(response.status).toBe(403);
    expect(response.headers.get("cache-control")).toBe("no-store");
    expect(response.headers.get("set-cookie")).toBeNull();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("não importa cliente Spring nem expõe token", () => {
    const source = readFileSync(join(process.cwd(), "src/app/api/auth/logout/route.ts"), "utf8");
    expect(source).not.toMatch(/backend-client|fetch\(|Authorization|accessToken|refreshToken/i);
  });
});
