import "server-only";
import { redirect } from "next/navigation";
import { safeReturnTo } from "@/lib/return-to";
import { resolveCurrentUserFromSessionCookie } from "./current-user";
import { BackendHttpError, type CurrentUser, type Role } from "./types";

function redirectToLogin(returnTo: string): never {
  redirect(`/login?returnTo=${encodeURIComponent(safeReturnTo(returnTo))}`);
}

export async function requireCurrentUser(returnTo: string): Promise<CurrentUser> {
  try {
    const user = await resolveCurrentUserFromSessionCookie();
    if (!user) return redirectToLogin(returnTo);
    return user;
  } catch (error) {
    if (error instanceof BackendHttpError && error.status === 403) redirect("/acesso-negado");
    if (error instanceof BackendHttpError && error.status === 401) return redirectToLogin(returnTo);
    throw error;
  }
}

export async function requireRole(role: Role, returnTo: string): Promise<CurrentUser> {
  const user = await requireCurrentUser(returnTo);
  if (user.role !== role) redirect("/acesso-negado");
  return user;
}
