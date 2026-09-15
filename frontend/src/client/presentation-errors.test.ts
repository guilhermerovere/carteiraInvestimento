import { describe, expect, it } from "vitest";
import { FinanceApiError } from "@/client/finance-api";
import { ptBrError } from "@/client/presentation-errors";

describe("broker creation error presentation", () => {
  const problem = (status: number, code?: "BROKER_ALREADY_REGISTERED" | "BROKER_CVM_NOT_REGISTERED" | "BROKER_VALIDATION_UNAVAILABLE") =>
    new FinanceApiError(status, { status, title: "safe", code });

  it("maps recognized broker outcomes without reading arbitrary provider detail", () => {
    expect(ptBrError(problem(409, "BROKER_ALREADY_REGISTERED"), "broker-create")).toBe("Esta corretora já está cadastrada.");
    expect(ptBrError(problem(422, "BROKER_CVM_NOT_REGISTERED"), "broker-create")).toBe("Este CNPJ não está cadastrado na CVM.");
    expect(ptBrError(problem(422), "broker-create")).toBe("Este CNPJ não corresponde a uma corretora válida.");
    expect(ptBrError(problem(502, "BROKER_VALIDATION_UNAVAILABLE"), "broker-create")).toBe("Não foi possível validar a corretora agora. Tente novamente.");
  });
});
