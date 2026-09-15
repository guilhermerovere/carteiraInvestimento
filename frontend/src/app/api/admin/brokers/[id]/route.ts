import { hasValidOrigin } from "@/server/auth/origin";
import { brokerEditBody } from "@/server/admin/validation";
import { financeErrorResponse, financeJson } from "@/server/finance/response";
import { financeBackendRequest } from "@/server/finance/transport";
import { FinanceValidationError, assertNoQuery, assertUuid, safeCorrelationId, strictJson } from "@/server/finance/validation";
export async function PATCH(request: Request, context: { params: Promise<{ id: string }> }) { const correlationId = safeCorrelationId(request.headers.get("x-correlation-id")); try { assertNoQuery(request); if (!hasValidOrigin(request.headers.get("origin"))) throw new FinanceValidationError("Origem invalida."); const id = assertUuid((await context.params).id); const result = await financeBackendRequest("/api/v1/corretoras/" + encodeURIComponent(id), { method: "PATCH", body: brokerEditBody(await strictJson(request)), correlationId }); return financeJson(result.data, result.correlationId); } catch (error) { return financeErrorResponse(error, correlationId); } }
