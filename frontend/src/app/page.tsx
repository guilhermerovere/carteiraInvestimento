import { redirect } from "next/navigation";
import { naturalRouteForRole } from "@/lib/return-to";
import { resolveCurrentUserFromSessionCookie } from "@/server/auth/current-user";
import { BackendHttpError } from "@/server/auth/types";

export default async function Home() {
  try {
    const user = await resolveCurrentUserFromSessionCookie();
    redirect(user ? naturalRouteForRole(user.role) : "/login");
  } catch (error) {
    if (error instanceof BackendHttpError && error.status === 401) redirect("/login");
    throw error;
  }
}

