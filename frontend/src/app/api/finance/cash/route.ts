import { mapCashBalance } from "@/server/finance/mappers";
import { financeErrorResponse, financeJson } from "@/server/finance/response";
import { financeBackendRequest } from "@/server/finance/transport";
import { assertNoQuery, safeCorrelationId } from "@/server/finance/validation";

export async function GET(request: Request) {
  try {
    assertNoQuery(request);
    const result = await financeBackendRequest("/api/v1/carteira/caixa", {
      correlationId: safeCorrelationId(request.headers.get("x-correlation-id")),
    });
    return financeJson(mapCashBalance(result.data), result.correlationId);
  } catch (error) {
    return financeErrorResponse(error, safeCorrelationId(request.headers.get("x-correlation-id")));
  }
}
