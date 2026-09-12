import { mapTransaction } from "@/server/finance/mappers";
import { financeErrorResponse, financeJson } from "@/server/finance/response";
import { financeBackendRequest } from "@/server/finance/transport";
import { assertNoQuery, assertUuid, safeCorrelationId } from "@/server/finance/validation";

export async function GET(request: Request, context: { params: Promise<{ id: string }> }) {
  try {
    assertNoQuery(request);
    const id = assertUuid((await context.params).id);
    const result = await financeBackendRequest("/api/v1/carteira/transacoes/" + encodeURIComponent(id), {
      correlationId: safeCorrelationId(request.headers.get("x-correlation-id")),
    });
    return financeJson(mapTransaction(result.data), result.correlationId);
  } catch (error) {
    return financeErrorResponse(error, safeCorrelationId(request.headers.get("x-correlation-id")));
  }
}
