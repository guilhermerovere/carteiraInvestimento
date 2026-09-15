import "server-only";
import { FinanceValidationError, assertEnum, strictObject } from "@/server/finance/validation";

function text(value: unknown, label: string, max: number): string {
  if (typeof value !== "string" || !value.trim() || value.length > max) throw new FinanceValidationError(label + " invalido.");
  return value;
}

export function profileBody(value: unknown): string {
  const body = strictObject(value, [], ["nome", "email"]);
  if (Object.keys(body).length !== 1) throw new FinanceValidationError("Informe apenas nome ou email.");
  if ("nome" in body) return JSON.stringify({ nome: text(body.nome, "Nome", 255).trim() });
  return JSON.stringify({ email: text(body.email, "Email", 320).trim().toLowerCase() });
}

export function passwordBody(value: unknown): string {
  const body = strictObject(value, ["senhaAtual", "novaSenha"]);
  return JSON.stringify({ senhaAtual: text(body.senhaAtual, "Senha atual", 512), novaSenha: text(body.novaSenha, "Nova senha", 512) });
}

export function closureBody(value: unknown): string {
  const body = strictObject(value, ["senhaAtual", "confirmacao"]);
  return JSON.stringify({ senhaAtual: text(body.senhaAtual, "Senha atual", 512), confirmacao: assertEnum(body.confirmacao, ["EXCLUIR MINHA CONTA"]) });
}
