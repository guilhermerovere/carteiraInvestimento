"use client";

import { useMutation, useQuery, useQueryClient, type QueryClient } from "@tanstack/react-query";
import { getCurrentUser, isUnauthorized, login, logout, register, type CurrentUser, type LoginInput, type RegisterInput } from "@/client/auth-form-api";

export const authMeKey = ["auth", "me"] as const;

export function clearAuthState(queryClient: QueryClient) {
  queryClient.removeQueries({ queryKey: authMeKey });
}

export async function confirmCurrentUser(queryClient: QueryClient): Promise<CurrentUser> {
  await queryClient.invalidateQueries({ queryKey: authMeKey, refetchType: "none" });
  try {
    return await queryClient.fetchQuery({ queryKey: authMeKey, queryFn: getCurrentUser, retry: false });
  } catch (error) {
    if (isUnauthorized(error)) clearAuthState(queryClient);
    throw error;
  }
}

export function useCurrentUser() {
  const queryClient = useQueryClient();
  return useQuery({
    queryKey: authMeKey,
    queryFn: async () => {
      try {
        return await getCurrentUser();
      } catch (error) {
        if (isUnauthorized(error)) clearAuthState(queryClient);
        throw error;
      }
    },
    retry: false,
  });
}

export function useLogin() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (input: LoginInput) => {
      await login(input);
      return confirmCurrentUser(queryClient);
    },
    retry: false,
    onError: (error) => {
      if (isUnauthorized(error)) clearAuthState(queryClient);
    },
  });
}

export function useRegister() {
  return useMutation({ mutationFn: (input: RegisterInput) => register(input), retry: false });
}

export function useLogout() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: logout,
    retry: false,
    onSuccess: () => clearAuthState(queryClient),
  });
}

export type { CurrentUser, LoginInput, RegisterInput };
