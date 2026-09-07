import { requireRole } from "@/server/auth/guard";
export default async function AdminLayout({ children }: Readonly<{ children: React.ReactNode }>) { await requireRole("ROLE_ADMIN", "/admin"); return children; }
