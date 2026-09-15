import Decimal from "decimal.js-light";

Decimal.set({ precision: 50, rounding: Decimal.ROUND_HALF_EVEN });
export const DECIMAL_PATTERN = /^(?:0|[1-9]\d*)(?:\.\d+)?$/;

export function parsePtBrDecimal(text: string, scale: number, precision = 18): { canonical?: string; intermediate: boolean; error?: string } {
  const raw = text.trim();
  if (raw === "" || raw === ",") return { intermediate: true };
  if (!/^\d+(?:,\d*)?$/.test(raw)) return { intermediate: false, error: "Informe um valor valido." };
  const [integer, fraction = ""] = raw.split(",");
  if (fraction.length > scale) return { intermediate: false, error: `Use no maximo ${scale} casas decimais.` };
  if ((integer.replace(/^0+/, "").length || 1) + fraction.length > precision) return { intermediate: false, error: "Valor acima do limite permitido." };
  const canonical = integer.replace(/^0+(?=\d)/, "") + (raw.includes(",") && fraction ? "." + fraction : "");
  return { canonical, intermediate: raw.endsWith(",") };
}

export function parsePtBrDecimalPaste(text: string, scale: number, precision = 18) {
  const raw = text.trim().replace(/^R\$\s*/, "");
  const normalized = /^\d{1,3}(?:\.\d{3})+,\d+$/.test(raw) ? raw.replaceAll(".", "") : raw;
  return parsePtBrDecimal(normalized, scale, precision);
}

export function editablePtBr(canonical: string): string { return DECIMAL_PATTERN.test(canonical) ? canonical.replace(".", ",") : ""; }

export function moneyEstimate(value: string | number | Decimal): string { return new Decimal(value).toDecimalPlaces(2, Decimal.ROUND_HALF_EVEN).toFixed(2); }
export function estimateCash(balance: string, amount: string, withdraw = false): string { return moneyEstimate(withdraw ? new Decimal(balance).minus(amount) : new Decimal(balance).plus(amount)); }
/** B3 prices are BRL; only US prices require the persisted USD/BRL rate. */
export function estimateBuyTotal(quantity: string, price: string, fees: string, market: "B3" | "US", fx?: string): string {
  const rate = market === "US" ? new Decimal(fx ?? "1") : new Decimal(1);
  return moneyEstimate(new Decimal(quantity).times(price).times(rate).plus(fees));
}
export function estimateSellNet(quantity: string, price: string, fees: string, fx = "1"): string { return moneyEstimate(new Decimal(quantity).times(price).times(fx).minus(fees)); }
export function estimateSellResult(net: string, sold: string, held: string, invested: string): string {
  const soldQuantity = new Decimal(sold); const heldQuantity = new Decimal(held);
  const releasedCost = soldQuantity.equals(heldQuantity) ? new Decimal(invested) : new Decimal(invested).times(soldQuantity).div(heldQuantity);
  return moneyEstimate(new Decimal(net).minus(releasedCost));
}
export function estimateAverage(quantity: string, invested: string, bought: string, cost: string): string {
  const nextQuantity = new Decimal(quantity).plus(bought);
  return nextQuantity.isZero() ? "0.00000000" : new Decimal(invested).plus(cost).div(nextQuantity).toDecimalPlaces(8, Decimal.ROUND_HALF_EVEN).toFixed(8);
}
export function decimalGreaterThan(left:string,right:string):boolean{return new Decimal(left).greaterThan(right);}

export function localDateTimeWithOffset(value: string, date = new Date(value)): string {
  if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(?::\d{2})?$/.test(value) || Number.isNaN(date.getTime())) throw new Error("Data e hora invalida.");
  const offset = -date.getTimezoneOffset(); const sign = offset >= 0 ? "+" : "-"; const two = (n: number) => String(Math.abs(n)).padStart(2, "0");
  return value + (value.length === 16 ? ":00" : "") + sign + two(Math.trunc(offset / 60)) + ":" + two(offset % 60);
}
