import { mapCashMovement, mapPage } from "@/server/finance/mappers";
import { financeErrorResponse, financeJson } from "@/server/finance/response";
import { financeBackendRequest } from "@/server/finance/transport";
import { safeCorrelationId, strictPagination } from "@/server/finance/validation";

export async function GET(request: Request) {
  try {
    const { page, size } = strictPagination(request);
    const result = await financeBackendRequest("/api/v1/carteira/caixa/movimentacoes?page=" + page + "&size=" + size, {
      correlationId: safeCorrelationId(request.headers.get("x-correlation-id")),
    });
    return financeJson(mapPage(result.data, mapCashMovement), result.correlationId);
  } catch (error) {
    return financeErrorResponse(error, safeCorrelationId(request.headers.get("x-correlation-id")));
  }
}
