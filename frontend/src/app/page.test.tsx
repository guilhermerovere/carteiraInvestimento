import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import Home from "./page";
describe("página pública", () => { it("não revela configuração de backend", () => { render(<Home />); expect(screen.queryByTestId("api-url")).not.toBeInTheDocument(); expect(document.body.textContent).not.toContain("BACKEND_API_URL"); }); });
