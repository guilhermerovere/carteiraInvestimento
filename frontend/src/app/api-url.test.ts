import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
describe("configuração pública", () => { it("não mantém NEXT_PUBLIC_API_URL como dependência funcional", () => { const compose = readFileSync("../docker-compose.yml", "utf8"); const dockerfile = readFileSync("Dockerfile", "utf8"); expect(compose).not.toContain("NEXT_PUBLIC_API_URL"); expect(dockerfile).not.toContain("NEXT_PUBLIC_API_URL"); }); });
