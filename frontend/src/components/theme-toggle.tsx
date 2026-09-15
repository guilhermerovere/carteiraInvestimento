"use client";

import { Laptop, Moon, Sun } from "lucide-react";
import { useTheme } from "next-themes";
import { useSyncExternalStore } from "react";

const options = [
  { value: "light", label: "Claro", icon: Sun },
  { value: "dark", label: "Escuro", icon: Moon },
  { value: "system", label: "Sistema", icon: Laptop },
] as const;

export function ThemeToggle() {
  const { theme, setTheme } = useTheme();
  const mounted = useSyncExternalStore(() => () => undefined, () => true, () => false);

  return (
    <fieldset className="theme-toggle" aria-label="Tema da interface" disabled={!mounted}>
      <legend className="sr-only">Tema da interface</legend>
      {options.map(({ value, label, icon: Icon }) => (
        <button
          key={value}
          type="button"
          className="theme-toggle__option"
          aria-label={"Usar tema " + label.toLowerCase()}
          aria-pressed={mounted && theme === value}
          title={label}
          onClick={() => setTheme(value)}
        >
          <Icon aria-hidden="true" />
          <span>{label}</span>
        </button>
      ))}
    </fieldset>
  );
}
