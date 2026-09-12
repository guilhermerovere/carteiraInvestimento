import { mapSummary } from "@/server/finance/mappers";
import { financeErrorResponse, financeJson } from "@/server/finance/response";
import { financeBackendRequest } from "@/server/finance/transport";
import { assertEmptyBody, assertNoQuery, safeCorrelationId } from "@/server/finance/validation";

export async function POST(request: Request) {
  try {
    assertNoQuery(request);
    await assertEmptyBody(request);
    const result = await financeBackendRequest("/api/v1/carteira/resumo/atualizar", {
      method: "POST",
      correlationId: safeCorrelationId(request.headers.get("x-correlation-id")),
    });
    return financeJson(mapSummary(result.data), result.correlationId);
  } catch (error) {
    return financeErrorResponse(error, safeCorrelationId(request.headers.get("x-correlation-id")));
  }
}
