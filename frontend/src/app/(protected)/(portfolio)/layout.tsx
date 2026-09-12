import { AppShell } from "@/components/app-shell";
import { requireRole } from "@/server/auth/guard";

export default async function PortfolioLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  const user = await requireRole("ROLE_USER", "/carteira");
  return <AppShell user={user}>{children}</AppShell>;
}
