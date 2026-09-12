"use client";

import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";

export function Pagination({ page, totalPages, onPageChange, disabled = false }: {
  page: number; totalPages: number; onPageChange: (page: number) => void; disabled?: boolean;
}) {
  if (totalPages <= 1) return null;
  return (
    <nav className="pagination" aria-label="Paginação">
      <Button variant="secondary" size="sm" disabled={disabled || page <= 0} onClick={() => onPageChange(page - 1)}>
        <ChevronLeft aria-hidden="true" /> Anterior
      </Button>
      <span aria-live="polite">Página <strong>{page + 1}</strong> de {totalPages}</span>
      <Button variant="secondary" size="sm" disabled={disabled || page + 1 >= totalPages} onClick={() => onPageChange(page + 1)}>
        Próxima <ChevronRight aria-hidden="true" />
      </Button>
    </nav>
  );
}
