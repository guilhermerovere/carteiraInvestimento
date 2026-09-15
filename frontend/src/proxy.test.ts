import { readFileSync } from "node:fs";
import { join } from "node:path";
import { NextRequest } from "next/server";
import { describe, expect, it } from "vitest";
import { config, proxy } from "./proxy";
const request = (path: string, cookie?: string) => new NextRequest(`https://app.test${path}`, { headers: cookie ? { Cookie: cookie } : undefined });
describe("proxy de pre-check de autenticação", () => {
  it.each(["/inicio", "/admin", "/carteira", "/carteira/posicoes"])("redireciona %s sem cookie ao login com returnTo interno", (path) => { const response = proxy(request(path)); expect(response.status).toBe(307); const location = new URL(response.headers.get("location")!); expect(location.pathname).toBe("/login"); expect(location.searchParams.get("returnTo")).toBe(path); });
  it("permite rota protegida com cookie sem afirmar que a sessão é válida", () => { const response = proxy(request("/inicio", "auth_session=residual-or-expired")); expect(response.headers.get("location")).toBeNull(); expect(response.headers.get("x-middleware-next")).toBe("1"); });
  it.each(["/login", "/register", "/acesso-negado", "/api/auth/me"])("deixa rota pública %s prosseguir", (path) => { expect(proxy(request(path)).headers.get("location")).toBeNull(); });
  it("restringe o matcher e não contém lógica de sessão", () => { expect(config.matcher).toEqual(["/inicio", "/admin", "/carteira/:path*"]); const source = readFileSync(join(process.cwd(), "src/proxy.ts"), "utf8"); expect(source).not.toMatch(/backend-client|current-user|fetch\(|Authorization|jwt|decode|role|cookies\.(set|delete)/i); });
});
