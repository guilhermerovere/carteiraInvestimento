import { NextResponse, type NextRequest } from "next/server";
import { AUTH_SESSION_COOKIE, isProtectedRoute } from "./auth-routes";
import { safeReturnTo } from "./lib/return-to";

export function proxy(request: NextRequest) {
  if (isProtectedRoute(request.nextUrl.pathname) && !request.cookies.has(AUTH_SESSION_COOKIE)) {
    const url = new URL("/login", request.url);
    url.searchParams.set("returnTo", safeReturnTo(request.nextUrl.pathname + request.nextUrl.search));
    return NextResponse.redirect(url);
  }
  return NextResponse.next();
}

export const config = { matcher: ["/inicio", "/admin", "/carteira/:path*"] };
