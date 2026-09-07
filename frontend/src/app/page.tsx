import { CircleCheck } from "lucide-react";

export default function Home() {
  return (
    <main className="foundation-shell">
      <section className="foundation-card" aria-labelledby="foundation-title">
        <span className="foundation-kicker">Base técnica</span>
        <CircleCheck aria-hidden="true" className="foundation-icon" />
        <h1 id="foundation-title">Fundação do projeto pronta</h1>
        <p>
          Next.js, TypeScript, Tailwind CSS, Shadcn UI, TanStack Query e Recharts
          estão preparados para as próximas changes.
        </p>
      </section>
    </main>
  );
}

