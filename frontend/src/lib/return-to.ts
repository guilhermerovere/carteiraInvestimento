import type { Role } from "@/lib/auth-contracts";

export const DEFAULT_RETURN_TO = "/carteira";

function isSafeInternalPath(value: string): boolean {
  if (!value.startsWith("/") || value.startsWith("//") || /[\\\\\s\u0000-\u001f]/.test(value) || /%(?![0-9a-f]{2})|%2f|%5c/i.test(value)) {
    return false;
  }
  try {
    const url = new URL(value, "http://return-to.invalid");
    return url.origin === "http://return-to.invalid" && url.pathname.startsWith("/");
  } catch {
    return false;
  }
}

export function safeReturnTo(value: string | null | undefined, fallback = DEFAULT_RETURN_TO): string {
  const safeFallback = isSafeInternalPath(fallback) ? fallback : DEFAULT_RETURN_TO;
  return typeof value === "string" && isSafeInternalPath(value) ? value : safeFallback;
}

export function naturalRouteForRole(role: Role): "/carteira" | "/admin" {
  return role === "ROLE_ADMIN" ? "/admin" : "/carteira";
}

function matchesRoute(pathname: string, route: string): boolean {
  return pathname === route || pathname.startsWith(`${route}/`);
}

export function safeReturnToForRole(value: string | null | undefined, role: Role): string {
  const fallback = naturalRouteForRole(role);
  const destination = safeReturnTo(value, fallback);
  const pathname = new URL(destination, "http://return-to.invalid").pathname;
  const requiresUser = matchesRoute(pathname, "/carteira") || matchesRoute(pathname, "/inicio");
  const requiresAdmin = matchesRoute(pathname, "/admin");

  if ((requiresUser && role !== "ROLE_USER") || (requiresAdmin && role !== "ROLE_ADMIN")) {
    return fallback;
  }
  return destination;
}
