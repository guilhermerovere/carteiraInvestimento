import { FinanceApiError } from "@/client/finance-api";

export function ptBrError(error: unknown, context: "generic" | "quote" | "asset-validation" | "email" | "password" | "closure" | "broker-create" = "generic"): string {
  if (error instanceof FinanceApiError && error.problem.code === "BROKER_CNPJ_INVALID") return "Informe um CNPJ válido.";
  if (error instanceof FinanceApiError && (error.problem.code === "BROKER_CVM_NOT_REGISTERED" || error.problem.code === "BROKER_CVM_NOT_APPROVED")) return "Este CNPJ não está cadastrado na CVM.";
  if (error instanceof FinanceApiError && context === "broker-create" && error.status === 422) return "Este CNPJ não corresponde a uma corretora válida.";
  if (error instanceof FinanceApiError && (error.problem.code === "BROKER_ALREADY_REGISTERED" || (context === "broker-create" && error.status === 409 && error.problem.title === "CNPJ already registered"))) return "Esta corretora já está cadastrada.";
  if (error instanceof FinanceApiError && (error.problem.code === "BROKER_VALIDATION_UNAVAILABLE" || (context === "broker-create" && (error.status === 502 || error.status === 503)))) return "Não foi possível validar a corretora agora. Tente novamente.";
  if (!(error instanceof FinanceApiError)) return "Não foi possível concluir a solicitação. Tente novamente.";
  if (error.problem.code === "EMAIL_IN_USE" || (context === "email" && error.status === 409)) return "Este email já está em uso.";
  if (error.problem.code === "CURRENT_PASSWORD_INCORRECT") return "A senha atual está incorreta.";
  if (error.problem.code === "CASH_NOT_ZERO") return "Zere o saldo da conta antes de excluir.";
  if (error.problem.code === "OPEN_POSITIONS") return "Encerre suas posições antes de excluir a conta.";
  if (error.problem.code === "FX_EXPIRED") return "A cotação de câmbio expirou. Consulte uma nova taxa para continuar.";
  if (error.status === 0) return "Não foi possível confirmar o resultado da operação. Revise a operação pendente antes de tentar novamente.";
  if (error.status === 400) return "Revise os campos informados.";
  if (error.status === 401) return "Sua sessão expirou. Entre novamente.";
  if (error.status === 403) return "Você não tem permissão para realizar esta ação.";
  if (error.status === 429) return "Limite temporário do serviço de mercado atingido. Tente novamente em alguns instantes.";
  if (error.status === 404 && context === "quote") return "Cotação indisponível no momento. Você pode informar o preço manualmente.";
  if (error.status === 409) return "Não foi possível concluir a operação devido a um conflito. Atualize os dados e tente novamente.";
  if (error.status === 502 && context === "asset-validation") return "Não foi possível validar o ativo agora. Tente novamente em alguns instantes.";
  if (error.status === 502) return "Um serviço de mercado está temporariamente indisponível. Tente novamente.";
  return "Não foi possível concluir a solicitação. Tente novamente.";
}
