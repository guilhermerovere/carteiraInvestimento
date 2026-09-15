import { NextResponse } from "next/server";
import { BackendHttpError, BackendNetworkError, safeProblem } from "./types";
export function authErrorResponse(error: unknown) {
  if (error instanceof BackendHttpError) {
    const problem = safeProblem(error.problem, error.status);
    return NextResponse.json({ status: error.status, title: problem.title ?? "Erro de autenticação", detail: problem.detail, instance: problem.instance }, { status: error.status, headers: { "Cache-Control": "no-store", ...(error.correlationId ? { "X-Correlation-ID": error.correlationId } : {}) } });
  }
  if (error instanceof BackendNetworkError) return NextResponse.json({ status: 503, title: "Serviço indisponível" }, { status: 503, headers: { "Cache-Control": "no-store" } });
  return NextResponse.json({ status: 500, title: "Erro de autenticação" }, { status: 500, headers: { "Cache-Control": "no-store" } });
}
