"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { financeApi } from "./finance-api";

export const portfolioKeys = {
  all: ["portfolio"] as const,
  summary: ["portfolio", "summary"] as const,
  evolution: ["portfolio", "evolution"] as const,
  positions: (page: number, size: number) => ["portfolio", "positions", page, size] as const,
  position: (assetId: string) => ["portfolio", "position", assetId] as const,
  transactionPages: ["portfolio", "transactions"] as const,
  transactions: (page: number, size: number) => ["portfolio", "transactions", page, size] as const,
  transaction: (id: string) => ["portfolio", "transaction", id] as const,
  cashBalance: ["portfolio", "cash"] as const,
  cashMovements: (page: number, size: number) => ["portfolio", "cash", "movements", page, size] as const,
};

export function usePortfolioSummary() {
  return useQuery({ queryKey: portfolioKeys.summary, queryFn: financeApi.summary, retry: false, staleTime: 60_000 });
}
export function usePortfolioEvolution() {
  return useQuery({ queryKey: portfolioKeys.evolution, queryFn: financeApi.evolution, retry: false, staleTime: 60_000 });
}

export function useRefreshPortfolioSummary() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ["portfolio", "summary", "refresh"],
    mutationFn: financeApi.refreshSummary,
    retry: false,
    onSuccess: async (summary) => {
      queryClient.setQueryData(portfolioKeys.summary, summary);
      await queryClient.invalidateQueries({ queryKey: portfolioKeys.summary, exact: true });
      await queryClient.invalidateQueries({ queryKey: portfolioKeys.evolution, exact: true });
    },
  });
}

export function usePositions(page: number, size: number) {
  return useQuery({ queryKey: portfolioKeys.positions(page, size), queryFn: () => financeApi.positions(page, size), retry: false, placeholderData: (previous) => previous });
}
export function useTransactions(page: number, size: number) {
  return useQuery({ queryKey: portfolioKeys.transactions(page, size), queryFn: () => financeApi.transactions(page, size), retry: false, placeholderData: (previous) => previous });
}
export function useCashBalance() {
  return useQuery({ queryKey: portfolioKeys.cashBalance, queryFn: financeApi.cash, retry: false });
}
export function useCashMovements(page: number, size: number) {
  return useQuery({ queryKey: portfolioKeys.cashMovements(page, size), queryFn: () => financeApi.cashMovements(page, size), retry: false, placeholderData: (previous) => previous });
}
