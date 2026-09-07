import { NextResponse } from "next/server";
import { resolveCurrentUserFromSessionCookie } from "@/server/auth/current-user";
import { expiredAuthCookie } from "@/server/auth/cookie";
import { BackendHttpError } from "@/server/auth/types";
import { authErrorResponse } from "@/server/auth/route-error";

export async function GET() {
  try {
    const user = await resolveCurrentUserFromSessionCookie();
    if (!user) return NextResponse.json({ status: 401, title: "Não autenticado" }, { status: 401, headers: { "Cache-Control": "no-store" } });
    return NextResponse.json(user, { headers: { "Cache-Control": "no-store" } });
  } catch (error) {
    const response = authErrorResponse(error);
    if (error instanceof BackendHttpError && error.status === 401) response.cookies.set(expiredAuthCookie());
    return response;
  }
}
