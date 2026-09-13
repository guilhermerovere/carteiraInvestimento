import type { ReactNode } from "react";
import { ThemeToggle } from "@/components/theme-toggle";

type AuthShellProps = {
  children: ReactNode;
  variant: "login" | "register";
};

const heroCopy = {
  login: {
    title: <>Invista com clareza.<br />Cresça com confiança.</>,
    description: "Acompanhe seu patrimônio, suas posições e movimentações em uma experiência moderna, segura e feita para decisões conscientes.",
  },
  register: {
    title: <>Construa uma visão melhor<br />do seu patrimônio.</>,
    description: "Organize sua jornada financeira em uma experiência clara, segura e preparada para decisões conscientes.",
  },
} as const;

export function AuthShell({ children, variant }: AuthShellProps) {
  const copy = heroCopy[variant];

  return (
    <main className="auth-layout">
      <a className="skip-link" href="#auth-form-panel">Ir para o formulário</a>
      <section className="auth-hero" aria-label="Valore — acompanhamento de investimentos">
        <div className="auth-brand">
          <span className="auth-brand__mark" aria-hidden="true">V</span>
          <span>Valore</span>
        </div>
        <div className="auth-hero__copy">
          <span className="auth-eyebrow">Patrimônio com propósito</span>
          <p className="auth-hero__title">{copy.title}</p>
          <p className="auth-hero__description">{copy.description}</p>
        </div>
        <div className="auth-abstract" aria-hidden="true">
          <span /><span /><span /><span />
        </div>
        <p className="auth-hero__footnote">Clareza para acompanhar. Confiança para decidir.</p>
      </section>

      <section id="auth-form-panel" className="auth-panel">
        <div className="auth-toolbar"><ThemeToggle /></div>
        <div className="auth-panel__content">
          <div className="auth-brand auth-brand--mobile" aria-label="Valore">
            <span className="auth-brand__mark" aria-hidden="true">V</span>
            <span>Valore</span>
          </div>
          {children}
        </div>
      </section>
    </main>
  );
}
