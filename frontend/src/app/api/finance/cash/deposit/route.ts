import { assertMutationRequest, cashBody } from "@/server/finance/mutation";
import { financeErrorResponse, financeJson } from "@/server/finance/response";
import { financeBackendRequest } from "@/server/finance/transport";
import { assertNoQuery, safeCorrelationId, strictJson } from "@/server/finance/validation";
export async function POST(request: Request) { const correlationId=safeCorrelationId(request.headers.get("x-correlation-id")); try { assertNoQuery(request); const key=assertMutationRequest(request); const body=cashBody(await strictJson(request)); const result=await financeBackendRequest("/api/v1/carteira/caixa/deposito",{method:"POST",body,idempotencyKey:key,correlationId}); return financeJson(result.data,result.correlationId); } catch(error){return financeErrorResponse(error,correlationId);} }
