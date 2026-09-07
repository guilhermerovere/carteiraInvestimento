import { NextResponse } from "next/server";
import { expiredAuthCookie } from "@/server/auth/cookie";
import { hasValidOrigin } from "@/server/auth/origin";

export async function POST(request: Request) {
  if (!hasValidOrigin(request.headers.get("origin"))) {
    return NextResponse.json({ status: 403, title: "Origem inválida" }, { status: 403, headers: { "Cache-Control": "no-store" } });
  }
  const response = new NextResponse(null, { status: 204, headers: { "Cache-Control": "no-store" } });
  response.cookies.set(expiredAuthCookie());
  return response;
}
