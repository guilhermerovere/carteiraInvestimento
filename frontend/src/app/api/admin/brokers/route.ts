import { hasValidOrigin } from "@/server/auth/origin";
import { adminListQuery, brokerCreateBody } from "@/server/admin/validation";
import { financeErrorResponse, financeJson } from "@/server/finance/response";
import { financeBackendRequest } from "@/server/finance/transport";
import { FinanceValidationError, assertNoQuery, safeCorrelationId, strictJson } from "@/server/finance/validation";

export async function GET(request: Request) { const id = safeCorrelationId(request.headers.get("x-correlation-id")); try { const result = await financeBackendRequest("/api/v1/corretoras" + adminListQuery(request, "brokers"), { correlationId: id }); return financeJson(result.data, result.correlationId); } catch (error) { return financeErrorResponse(error, id); } }
export async function POST(request: Request) { const id = safeCorrelationId(request.headers.get("x-correlation-id")); try { assertNoQuery(request); if (!hasValidOrigin(request.headers.get("origin"))) throw new FinanceValidationError("Origem inválida."); const result = await financeBackendRequest("/api/v1/corretoras", { method: "POST", body: brokerCreateBody(await strictJson(request)), correlationId: id }); return financeJson(result.data, result.correlationId); } catch (error) { return financeErrorResponse(error, id); } }
