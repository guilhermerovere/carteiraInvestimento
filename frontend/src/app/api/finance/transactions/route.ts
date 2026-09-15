import { mapPage, mapTransaction } from "@/server/finance/mappers";
import { financeErrorResponse, financeJson } from "@/server/finance/response";
import { financeBackendRequest } from "@/server/finance/transport";
import { safeCorrelationId, strictPagination } from "@/server/finance/validation";
import { assertMutationRequest, transactionBody } from "@/server/finance/mutation";
import { assertNoQuery, strictJson } from "@/server/finance/validation";

export async function GET(request: Request) {
  try {
    const { page, size } = strictPagination(request);
    const result = await financeBackendRequest("/api/v1/carteira/transacoes?page=" + page + "&size=" + size, {
      correlationId: safeCorrelationId(request.headers.get("x-correlation-id")),
    });
    return financeJson(mapPage(result.data, mapTransaction), result.correlationId);
  } catch (error) {
    return financeErrorResponse(error, safeCorrelationId(request.headers.get("x-correlation-id")));
  }
}

export async function POST(request: Request) {
  const correlationId = safeCorrelationId(request.headers.get("x-correlation-id"));
  try {
    assertNoQuery(request); const key = assertMutationRequest(request); const body = transactionBody(await strictJson(request));
    const result = await financeBackendRequest("/api/v1/carteira/transacoes", { method: "POST", body, idempotencyKey: key, correlationId });
    return financeJson(result.data, result.correlationId);
  } catch (error) { return financeErrorResponse(error, correlationId); }
}
