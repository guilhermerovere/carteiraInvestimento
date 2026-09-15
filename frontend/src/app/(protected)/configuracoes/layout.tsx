import { AccountShell } from "@/components/account-shell";
import { requireCurrentUser } from "@/server/auth/guard";
export default async function SettingsLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  const user = await requireCurrentUser("/configuracoes");
  return <AccountShell user={user}>{children}</AccountShell>;
}
