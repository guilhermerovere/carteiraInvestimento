import type { Metadata } from "next";
import type { ReactNode } from "react";
import { QueryProvider } from "@/providers/query-provider";
import "./globals.css";

export const metadata: Metadata = {
  title: "Carteira Investimento",
  description: "Fundação técnica da plataforma de investimentos.",
};

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="pt-BR">
      <body>
        <QueryProvider>{children}</QueryProvider>
      </body>
    </html>
  );
}

