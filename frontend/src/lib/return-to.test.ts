import { describe, expect, it } from "vitest";
import { DEFAULT_RETURN_TO, naturalRouteForRole, safeReturnTo, safeReturnToForRole } from "./return-to";
describe("safeReturnTo", () => {
  it.each(["/inicio", "/admin", "/inicio?tab=perfil", "/inicio#resumo"])("aceita caminho interno %s", (value) => { expect(safeReturnTo(value)).toBe(value); });
  it.each(["https://evil.example", "http://evil.example", "//evil.example", "javascript:alert(1)", "data:text/plain,x", "mailto:user@example.test", "admin", "\\evil", "", "   ", "/\\evil", "/%", "/%2f%2fevil", "/%5cevil"])("rejeita destino inseguro ou malformado %s", (value) => { expect(safeReturnTo(value)).toBe(DEFAULT_RETURN_TO); });
  it("usa somente um fallback interno seguro", () => { expect(safeReturnTo(null, "/admin")).toBe("/admin"); expect(safeReturnTo(null, "https://evil.example")).toBe(DEFAULT_RETURN_TO); });
  it("define a landing natural pela role confirmada", () => { expect(naturalRouteForRole("ROLE_USER")).toBe("/carteira"); expect(naturalRouteForRole("ROLE_ADMIN")).toBe("/admin"); });
  it("preserva returnTo seguro compatível e evita destino protegido da role errada", () => {
    expect(safeReturnToForRole("/carteira/posicoes?pagina=2", "ROLE_USER")).toBe("/carteira/posicoes?pagina=2");
    expect(safeReturnToForRole("/admin", "ROLE_ADMIN")).toBe("/admin");
    expect(safeReturnToForRole("/admin", "ROLE_USER")).toBe("/carteira");
    expect(safeReturnToForRole("/carteira", "ROLE_ADMIN")).toBe("/admin");
    expect(safeReturnToForRole("/inicio", "ROLE_ADMIN")).toBe("/admin");
  });
  it.each(["https://evil.example", "//evil.example", "javascript:alert(1)"])("rejeita returnTo malicioso por role %s", (value) => {
    expect(safeReturnToForRole(value, "ROLE_ADMIN")).toBe("/admin");
  });
});
