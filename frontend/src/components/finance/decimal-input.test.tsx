import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { PriceInput, QuantityInput } from "./decimal-input";

function ControlledInput({ kind, onValueChange }: { kind: "quantity" | "price"; onValueChange: (display: string, canonical: string) => void }) {
  const [value, setValue] = useState("");
  const change = (display: string, canonical: string) => { setValue(display); onValueChange(display, canonical); };
  return kind === "quantity"
    ? <QuantityInput label="Quantidade" value={value} onValueChange={change} />
    : <PriceInput label="Preco unitario" currency="USD" value={value} onValueChange={change} />;
}

describe("inputs de quantidade e preco", () => {
  afterEach(cleanup);

  it("aceita apenas quantidade inteira positiva", async () => {
    const onValueChange = vi.fn();
    render(<ControlledInput kind="quantity" onValueChange={onValueChange} />);
    const input = screen.getByLabelText("Quantidade");

    await userEvent.type(input, "5");
    expect(onValueChange).toHaveBeenLastCalledWith("5", "5");

    await userEvent.clear(input);
    await userEvent.type(input, "0");
    expect(screen.getByRole("alert")).toHaveTextContent("quantidade inteira positiva");
    expect(onValueChange).toHaveBeenLastCalledWith("0", "");

    await userEvent.clear(input);
    await userEvent.type(input, "1,5");
    expect(input).toHaveValue("15");
    expect(onValueChange).toHaveBeenLastCalledWith("15", "15");
  });

  it("exibe prefixo USD e duas casas sem arredondar o valor canonico", async () => {
    const onValueChange = vi.fn();
    render(<ControlledInput kind="price" onValueChange={onValueChange} />);
    const input = screen.getByRole("textbox");

    expect(screen.getByText("US$")).toBeInTheDocument();
    await userEvent.type(input, "42,73000000");
    await userEvent.tab();

    expect(input).toHaveValue("42,73");
    expect(onValueChange).toHaveBeenLastCalledWith("42,73", "42.73000000");
  });
});
