export const AUTH_SESSION_COOKIE = "auth_session";
export function isProtectedRoute(pathname: string): boolean {
  return pathname === "/inicio" || pathname === "/admin" || pathname === "/carteira" || pathname.startsWith("/carteira/");
}
