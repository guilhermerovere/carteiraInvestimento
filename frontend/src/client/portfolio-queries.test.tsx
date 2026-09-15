import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { act, renderHook, waitFor } from "@testing-library/react";
import type { ReactNode } from "react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { summary } from "@/test/finance-fixtures";
import { financeApi } from "./finance-api";
import { portfolioKeys, useRefreshPortfolioSummary } from "./portfolio-queries";

describe("queries de portfólio", () => {
  afterEach(() => vi.restoreAllMocks());

  it("refresh substitui e invalida somente portfolio.summary, sem estado otimista", async () => {
    const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false }, queries: { retry: false } } });
    queryClient.setQueryData(portfolioKeys.summary, { ...summary, patrimonioTotalBrl: "1.00" });
    queryClient.setQueryData(portfolioKeys.positions(0, 20), { marker: "custódia" });
    const request = vi.spyOn(financeApi, "refreshSummary").mockResolvedValue(summary);
    const invalidate = vi.spyOn(queryClient, "invalidateQueries");
    const wrapper = ({ children }: { children: ReactNode }) => <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
    const { result } = renderHook(() => useRefreshPortfolioSummary(), { wrapper });

    expect(queryClient.getQueryData(portfolioKeys.summary)).toMatchObject({ patrimonioTotalBrl: "1.00" });
    await act(async () => { await result.current.mutateAsync(); });
    await waitFor(() => expect(request).toHaveBeenCalledOnce());
    expect(queryClient.getQueryData(portfolioKeys.summary)).toEqual(summary);
    expect(queryClient.getQueryData(portfolioKeys.positions(0, 20))).toEqual({ marker: "custódia" });
    expect(invalidate).toHaveBeenCalledTimes(2);
    expect(invalidate).toHaveBeenCalledWith({ queryKey: portfolioKeys.summary, exact: true });
    expect(invalidate).toHaveBeenCalledWith({ queryKey: portfolioKeys.evolution, exact: true });
  });
});
