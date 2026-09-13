import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import type { ReactNode } from "react";
import { describe, expect, it, vi } from "vitest";

const { setTheme, providerProps, themeState } = vi.hoisted(() => ({
  setTheme: vi.fn(),
  providerProps: { value: {} as Record<string, unknown> },
  themeState: { value: "system" },
}));
vi.mock("next-themes", () => ({
  ThemeProvider: ({ children, ...props }: { children: ReactNode } & Record<string, unknown>) => {
    providerProps.value = props;
    return <div data-testid="theme-provider">{children}</div>;
  },
  useTheme: () => ({ theme: themeState.value, setTheme }),
}));

import { ThemeToggle } from "./theme-toggle";
import { ThemeProvider } from "@/providers/theme-provider";

describe("tema Light, Dark e System", () => {
  it("integra next-themes por classe, persiste e segue o sistema", () => {
    render(<ThemeProvider><span>conteúdo</span></ThemeProvider>);
    expect(providerProps.value).toMatchObject({
      attribute: "class",
      defaultTheme: "system",
      enableSystem: true,
      disableTransitionOnChange: true,
      storageKey: "carteira-theme",
    });
  });

  it("expõe três escolhas acessíveis e envia a seleção", async () => {
    render(<ThemeToggle />);
    const dark = await screen.findByRole("button", { name: "Usar tema escuro" });
    await waitFor(() => expect(dark).toBeEnabled());
    expect(screen.getByRole("button", { name: "Usar tema claro" })).toHaveAttribute("aria-pressed", "false");
    expect(screen.getByRole("button", { name: "Usar tema sistema" })).toHaveAttribute("aria-pressed", "true");
    await userEvent.click(dark);
    expect(setTheme).toHaveBeenCalledWith("dark");
  });
});
