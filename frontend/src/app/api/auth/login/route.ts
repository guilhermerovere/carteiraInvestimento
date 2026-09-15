import { NextResponse } from "next/server";
import { backendClient } from "@/server/auth/backend-client";
import { authCookie } from "@/server/auth/cookie";
import { hasValidOrigin } from "@/server/auth/origin";
import { authErrorResponse } from "@/server/auth/route-error";
import { loginInputFrom } from "@/server/auth/types";

export async function POST(request: Request) {
  if (!hasValidOrigin(request.headers.get("origin"))) {
    return NextResponse.json({ status: 403, title: "Origem inválida" }, { status: 403, headers: { "Cache-Control": "no-store" } });
  }
  try {
    const login = await backendClient.login(loginInputFrom(await request.json()));
    const response = new NextResponse(null, { status: 204, headers: { "Cache-Control": "no-store" } });
    response.cookies.set(authCookie(login.accessToken, login.expiresIn));
    return response;
  } catch (error) {
    return authErrorResponse(error);
  }
}
