import type { ReactNode } from "react";

export function FinancialPageIntro({ title, description, action, titleId }: { title: string; description: string; action?: ReactNode; titleId?: string }) {
  return <div className="page-intro"><div><h2 id={titleId}>{title}</h2><p>{description}</p></div>{action}</div>;
}
