import "server-only";
import { loadAuthConfig } from "./config";
export const AUTH_SESSION_COOKIE = "auth_session";
export function authCookieBase() { return { httpOnly: true, sameSite: "lax" as const, path: "/", secure: loadAuthConfig().appOrigin.protocol === "https:" }; }
export function authCookie(token: string, expiresIn: number) { return { name: AUTH_SESSION_COOKIE, ...authCookieBase(), value: token, maxAge: Math.max(0, Math.floor(expiresIn)) }; }
export function expiredAuthCookie() { return { name: AUTH_SESSION_COOKIE, ...authCookieBase(), value: "", maxAge: 0 }; }
