import { describe, expect, it } from "vitest";
import { DEFAULT_RETURN_TO, safeReturnTo } from "./return-to";
describe("safeReturnTo", () => {
  it.each(["/inicio", "/admin", "/inicio?tab=perfil", "/inicio#resumo"])("aceita caminho interno %s", (value) => { expect(safeReturnTo(value)).toBe(value); });
  it.each(["https://evil.example", "http://evil.example", "//evil.example", "javascript:alert(1)", "data:text/plain,x", "mailto:user@example.test", "admin", "\\evil", "", "   ", "/\\evil", "/%", "/%2f%2fevil", "/%5cevil"])("rejeita destino inseguro ou malformado %s", (value) => { expect(safeReturnTo(value)).toBe(DEFAULT_RETURN_TO); });
  it("usa somente um fallback interno seguro", () => { expect(safeReturnTo(null, "/admin")).toBe("/admin"); expect(safeReturnTo(null, "https://evil.example")).toBe(DEFAULT_RETURN_TO); });
});
