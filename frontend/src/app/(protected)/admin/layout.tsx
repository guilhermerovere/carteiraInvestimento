import { requireRole } from "@/server/auth/guard";
import { AdminShell } from "@/components/admin/admin-shell";
export default async function AdminLayout({ children }: Readonly<{ children: React.ReactNode }>) { const user = await requireRole("ROLE_ADMIN", "/admin"); return <AdminShell user={user}>{children}</AdminShell>; }
