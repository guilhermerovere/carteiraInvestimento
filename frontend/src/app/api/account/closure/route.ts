import { NextResponse } from "next/server";
import { closureBody } from "@/server/account/validation";
import { expiredAuthCookie } from "@/server/auth/cookie";
import { hasValidOrigin } from "@/server/auth/origin";
import { financeErrorResponse } from "@/server/finance/response";
import { financeBackendRequest } from "@/server/finance/transport";
import { FinanceValidationError, assertNoQuery, safeCorrelationId, strictJson } from "@/server/finance/validation";

export async function POST(request: Request) {
  const correlationId = safeCorrelationId(request.headers.get("x-correlation-id"));
  try {
    assertNoQuery(request);
    if (!hasValidOrigin(request.headers.get("origin"))) throw new FinanceValidationError("Origem invalida.");
    const result = await financeBackendRequest("/api/v1/account/closure", { method: "POST", body: closureBody(await strictJson(request)), correlationId });
    const response = new NextResponse(null, { status: 204, headers: { "Cache-Control": "no-store", ...(result.correlationId ? { "X-Correlation-ID": result.correlationId } : {}) } });
    response.cookies.set(expiredAuthCookie());
    return response;
  } catch (error) { return financeErrorResponse(error, correlationId); }
}
