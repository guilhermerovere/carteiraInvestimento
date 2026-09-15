import "server-only";
import { NextResponse } from "next/server";
import { FinanceValidationError } from "./validation";
import { FinanceHttpError, FinanceNetworkError } from "./transport";

const HEADERS = { "Cache-Control": "no-store" };

export function financeJson(data: unknown, correlationId?: string): NextResponse {
  return NextResponse.json(data, {
    headers: { ...HEADERS, ...(correlationId ? { "X-Correlation-ID": correlationId } : {}) },
  });
}

export function financeNoContent(correlationId?: string): NextResponse {
  return new NextResponse(null, {
    status: 204,
    headers: { ...HEADERS, ...(correlationId ? { "X-Correlation-ID": correlationId } : {}) },
  });
}

export function financeErrorResponse(error: unknown, requestCorrelationId?: string): NextResponse {
  const correlationId = error instanceof FinanceHttpError ? error.correlationId ?? requestCorrelationId : requestCorrelationId;
  const headers = { ...HEADERS, ...(correlationId ? { "X-Correlation-ID": correlationId } : {}) };
  if (error instanceof FinanceValidationError) {
    return NextResponse.json({ status: 400, title: "Requisição inválida", detail: error.message }, { status: 400, headers });
  }
  if (error instanceof FinanceHttpError) {
    return NextResponse.json(error.problem, {
      status: error.status,
      headers,
    });
  }
  if (error instanceof FinanceNetworkError) {
    return NextResponse.json({ status: 502, title: "Serviço financeiro indisponível." }, { status: 502, headers });
  }
  return NextResponse.json({ status: 502, title: "Resposta financeira inválida." }, { status: 502, headers });
}
