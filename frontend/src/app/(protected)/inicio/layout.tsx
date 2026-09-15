import { requireRole } from "@/server/auth/guard";
export default async function InicioLayout({ children }: Readonly<{ children: React.ReactNode }>) { await requireRole("ROLE_USER", "/inicio"); return children; }
