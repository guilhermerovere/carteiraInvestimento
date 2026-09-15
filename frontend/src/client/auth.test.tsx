import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { readFileSync } from "node:fs";
import { join } from "node:path";
import type { ReactNode } from "react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { authMeKey, confirmCurrentUser, useCurrentUser, useLogin, useLogout, useRegister } from "./auth";

const fetchMock = vi.fn<typeof fetch>();
const user = { id: "u1", nome: "Usuário", email: "user@example.test", role: "ROLE_USER" as const, ativo: true };

function client() {
  return new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
}

function wrapper(queryClient: QueryClient) {
  function TestQueryProvider({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return TestQueryProvider;
}

describe("estado de autenticação TanStack Query", () => {
  beforeEach(() => { vi.stubGlobal("fetch", fetchMock); fetchMock.mockReset(); });
  afterEach(() => vi.unstubAllGlobals());

  it("usa a chave exata e carrega me same-origin sem retries", async () => {
    const queryClient = client();
    fetchMock.mockResolvedValue(new Response(JSON.stringify(user), { status: 200, headers: { "Content-Type": "application/json" } }));
    const result = renderHook(() => useCurrentUser(), { wrapper: wrapper(queryClient) });
    await waitFor(() => expect(result.result.current.data).toEqual(user));
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(fetchMock).toHaveBeenCalledWith("/api/auth/me", expect.objectContaining({ method: "GET", credentials: "same-origin" }));
    expect(queryClient.getQueryData(authMeKey)).toEqual(user);
  });

  it("confirma me e preenche o cache somente depois do 204 de login", async () => {
    const queryClient = client();
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 })).mockResolvedValueOnce(new Response(JSON.stringify(user), { status: 200, headers: { "Content-Type": "application/json" } }));
    const result = renderHook(() => useLogin(), { wrapper: wrapper(queryClient) });
    await expect(result.result.current.mutateAsync({ email: "user@example.test", senha: "Password1!" })).resolves.toEqual(user);
    expect(fetchMock.mock.calls.map(([path]) => path)).toEqual(["/api/auth/login", "/api/auth/me"]);
    expect(queryClient.getQueryData(authMeKey)).toEqual(user);
  });

  it("mantém cadastro sem carregar me, sem autenticar e sem retry", async () => {
    const queryClient = client();
    fetchMock.mockResolvedValue(new Response(null, { status: 201 }));
    const result = renderHook(() => useRegister(), { wrapper: wrapper(queryClient) });
    await result.result.current.mutateAsync({ nome: "Usuário", email: "user@example.test", senha: "Password1!" });
    expect(fetchMock.mock.calls.map(([path]) => path)).toEqual(["/api/auth/register"]);
    expect(queryClient.getQueryData(authMeKey)).toBeUndefined();
  });

  it("logout limpa o cache somente após 204", async () => {
    const queryClient = client();
    queryClient.setQueryData(authMeKey, user);
    fetchMock.mockResolvedValue(new Response(null, { status: 204 }));
    const result = renderHook(() => useLogout(), { wrapper: wrapper(queryClient) });
    await result.result.current.mutateAsync();
    expect(fetchMock).toHaveBeenCalledWith("/api/auth/logout", expect.objectContaining({ method: "POST", credentials: "same-origin" }));
    expect(queryClient.getQueryData(authMeKey)).toBeUndefined();
  });

  it("401 remove identidade sem retry", async () => {
    const queryClient = client();
    queryClient.setQueryData(authMeKey, user);
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ status: 401, title: "Não autenticado" }), { status: 401, headers: { "Content-Type": "application/problem+json" } }));
    await expect(confirmCurrentUser(queryClient)).rejects.toMatchObject({ status: 401 });
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(queryClient.getQueryData(authMeKey)).toBeUndefined();
  });

  it("query de me não entra em loop após 401", async () => {
    const queryClient = client();
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ status: 401, title: "Não autenticado" }), { status: 401, headers: { "Content-Type": "application/problem+json" } }));
    renderHook(() => useCurrentUser(), { wrapper: wrapper(queryClient) });
    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(1));
    await new Promise((resolve) => setTimeout(resolve, 25));
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(queryClient.getQueryData(authMeKey)).toBeUndefined();
  });

  it("403 preserva usuário confirmado e não dispara logout", async () => {
    const queryClient = client();
    queryClient.setQueryData(authMeKey, user);
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ status: 403, title: "Acesso negado" }), { status: 403, headers: { "Content-Type": "application/problem+json" } }));
    await expect(confirmCurrentUser(queryClient)).rejects.toMatchObject({ status: 403 });
    expect(queryClient.getQueryData(authMeKey)).toEqual(user);
    expect(fetchMock.mock.calls.map(([path]) => path)).toEqual(["/api/auth/me"]);
  });

  it("não incorpora backend, JWT, Authorization ou cookie no código cliente", () => {
    const source = readFileSync(join(process.cwd(), "src/client/auth.ts"), "utf8");
    expect(source).not.toMatch(/server\/auth|BACKEND_API_URL|accessToken|Authorization|auth_session|https?:\/\//i);
  });
});
