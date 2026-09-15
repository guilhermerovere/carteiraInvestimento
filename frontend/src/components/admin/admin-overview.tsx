import Link from "next/link";
import { Card, CardContent, CardHeader } from "@/components/ui/card";

export function AdminOverview() {
  return <section className="admin-page"><div className="page-intro"><div><span>Visão geral</span><h2>Administração Valore</h2><p>Gerencie os catálogos usados pelas operações financeiras.</p></div></div><div className="admin-overview-grid"><Card><CardHeader><h3>Corretoras</h3></CardHeader><CardContent><p>Instituições cadastradas e validadas no catálogo global.</p><Link href="/admin/corretoras">Gerenciar corretoras</Link></CardContent></Card><Card><CardHeader><h3>Ativos</h3></CardHeader><CardContent><p>Instrumentos financeiros canônicos disponíveis na plataforma.</p><Link href="/admin/ativos">Gerenciar ativos</Link></CardContent></Card></div></section>;
}
