import { hasValidOrigin } from "@/server/auth/origin";
import { profileBody } from "@/server/account/validation";
import { financeErrorResponse, financeJson } from "@/server/finance/response";
import { financeBackendRequest } from "@/server/finance/transport";
import { FinanceValidationError, assertNoQuery, safeCorrelationId, strictJson } from "@/server/finance/validation";

export async function PATCH(request: Request) {
  const correlationId = safeCorrelationId(request.headers.get("x-correlation-id"));
  try {
    assertNoQuery(request);
    if (!hasValidOrigin(request.headers.get("origin"))) throw new FinanceValidationError("Origem invalida.");
    const result = await financeBackendRequest("/api/v1/account/profile", { method: "PATCH", body: profileBody(await strictJson(request)), correlationId });
    return financeJson(result.data, result.correlationId);
  } catch (error) { return financeErrorResponse(error, correlationId); }
}
