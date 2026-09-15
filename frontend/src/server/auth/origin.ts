import "server-only";
import { loadAuthConfig } from "./config";
export function hasValidOrigin(origin: string | null): boolean {
  if (!origin) return false;
  try {
    const candidate = new URL(origin);
    const isOriginOnly = candidate.pathname === "/" && !candidate.search && !candidate.hash && !candidate.username && !candidate.password;
    return isOriginOnly && candidate.origin === loadAuthConfig().appOrigin.origin;
  } catch {
    return false;
  }
}
