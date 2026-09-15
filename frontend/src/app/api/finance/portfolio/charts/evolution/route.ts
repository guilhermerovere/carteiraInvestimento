import { mapEvolution } from "@/server/finance/mappers";
import { financeErrorResponse, financeJson } from "@/server/finance/response";
import { financeBackendRequest } from "@/server/finance/transport";
import { safeCorrelationId } from "@/server/finance/validation";

export async function GET(request: Request) {
  const correlationId = safeCorrelationId(request.headers.get("x-correlation-id"));
  try {
    const result = await financeBackendRequest("/api/v1/carteira/graficos/evolucao", { correlationId });
    return financeJson(mapEvolution(result.data), result.correlationId);
  } catch (error) {
    return financeErrorResponse(error, correlationId);
  }
}
