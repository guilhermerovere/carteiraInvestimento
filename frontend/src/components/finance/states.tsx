"use client";

import { AlertTriangle, Clipboard, Inbox, RotateCcw } from "lucide-react";
import { useState } from "react";
import { FinanceApiError } from "@/client/finance-api";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export function FinancialSkeleton({ rows = 3, label = "Carregando dados financeiros" }: { rows?: number; label?: string }) {
  return (
    <Card role="status" aria-label={label}>
      <CardContent className="financial-skeleton">
        <span className="sr-only">{label}</span>
        <Skeleton className="financial-skeleton__title" />
        {Array.from({ length: rows }, (_, index) => <Skeleton key={index} className="financial-skeleton__row" />)}
      </CardContent>
    </Card>
  );
}

export function EmptyState({ title, description }: { title: string; description: string }) {
  return (
    <Card>
      <CardContent className="empty-state">
        <Inbox aria-hidden="true" />
        <h2>{title}</h2>
        <p>{description}</p>
      </CardContent>
    </Card>
  );
}

function messageFor(error: unknown): string {
  if (!(error instanceof FinanceApiError)) return "Não foi possível carregar estes dados agora.";
  if (error.status === 401) return "Sua sessão precisa ser confirmada novamente.";
  if (error.status === 403) return "Você não tem permissão para acessar estes dados.";
  if (error.status === 409) return "Os dados da carteira mudaram durante a atualização. Tente novamente.";
  if (error.status === 502) return "Não foi possível atualizar os dados de mercado agora.";
  return error.problem.detail ?? error.problem.title;
}

export function ProblemDetailAlert({ error, onRetry, compact = false }: { error: unknown; onRetry?: () => void; compact?: boolean }) {
  const [copied, setCopied] = useState(false);
  const correlationId = error instanceof FinanceApiError ? error.correlationId : undefined;
  return (
    <div className={compact ? "problem-alert problem-alert--compact" : "problem-alert"} role="alert" aria-live="polite">
      <AlertTriangle aria-hidden="true" />
      <div>
        <strong>{messageFor(error)}</strong>
        {correlationId && (
          <div className="problem-alert__support">
            <span>Código de suporte: <code>{correlationId}</code></span>
            <Button
              variant="ghost"
              size="sm"
              onClick={async () => {
                await navigator.clipboard?.writeText(correlationId);
                setCopied(true);
              }}
            >
              <Clipboard aria-hidden="true" /> {copied ? "Copiado" : "Copiar"}
            </Button>
          </div>
        )}
        {onRetry && <Button variant="secondary" size="sm" onClick={onRetry}><RotateCcw aria-hidden="true" /> Tentar novamente</Button>}
      </div>
    </div>
  );
}
