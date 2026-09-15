import { financeErrorResponse, financeJson } from "@/server/finance/response";
import { financeBackendRequest } from "@/server/finance/transport";
import { FinanceValidationError, safeCorrelationId } from "@/server/finance/validation";

export async function GET(request: Request) {
  const correlationId = safeCorrelationId(request.headers.get("x-correlation-id"));
  try {
    const params = new URL(request.url).searchParams;
    for (const key of params.keys()) if (key !== "q") throw new FinanceValidationError();
    const q = params.get("q")?.trim();
    if (!q || q.length > 128) throw new FinanceValidationError();
    const result = await financeBackendRequest("/api/v1/acoes/discovery?q=" + encodeURIComponent(q) + "&mercado=B3", { correlationId });
    return financeJson(result.data, result.correlationId);
  } catch (error) { return financeErrorResponse(error, correlationId); }
}
