import { NextResponse } from "next/server";
import { backendClient } from "@/server/auth/backend-client";
import { hasValidOrigin } from "@/server/auth/origin";
import { authErrorResponse } from "@/server/auth/route-error";
import { registerInputFrom } from "@/server/auth/types";

export async function POST(request: Request) {
  if (!hasValidOrigin(request.headers.get("origin"))) {
    return NextResponse.json({ status: 403, title: "Origem inválida" }, { status: 403, headers: { "Cache-Control": "no-store" } });
  }
  try {
    await backendClient.register(registerInputFrom(await request.json()));
    return new NextResponse(null, { status: 201, headers: { "Cache-Control": "no-store" } });
  } catch (error) {
    return authErrorResponse(error);
  }
}
