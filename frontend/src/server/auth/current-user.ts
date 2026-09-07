import "server-only";
import { cookies } from "next/headers";
import { backendClient } from "./backend-client";
import { AUTH_SESSION_COOKIE } from "./cookie";
import type { CurrentUser } from "./types";

export async function resolveCurrentUser(token: string): Promise<CurrentUser> {
  const user = await backendClient.me(token);
  return { id: user.id, nome: user.nome, email: user.email, role: user.role, ativo: user.ativo };
}

export async function resolveCurrentUserFromSessionCookie(): Promise<CurrentUser | null> {
  const token = (await cookies()).get(AUTH_SESSION_COOKIE)?.value;
  return token ? resolveCurrentUser(token) : null;
}
