import { mapPosition } from "@/server/finance/mappers";
import { financeErrorResponse, financeJson } from "@/server/finance/response";
import { financeBackendRequest } from "@/server/finance/transport";
import { assertNoQuery, assertUuid, safeCorrelationId } from "@/server/finance/validation";

export async function GET(request: Request, context: { params: Promise<{ assetId: string }> }) {
  try {
    assertNoQuery(request);
    const assetId = assertUuid((await context.params).assetId);
    const result = await financeBackendRequest("/api/v1/carteira/posicoes/" + encodeURIComponent(assetId), {
      correlationId: safeCorrelationId(request.headers.get("x-correlation-id")),
    });
    return financeJson(mapPosition(result.data), result.correlationId);
  } catch (error) {
    return financeErrorResponse(error, safeCorrelationId(request.headers.get("x-correlation-id")));
  }
}
