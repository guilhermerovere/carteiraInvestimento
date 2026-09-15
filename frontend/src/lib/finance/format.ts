import Decimal from "decimal.js-light";

const DECIMAL = /^(-?)(0|[1-9]\d*)(?:\.(\d+))?$/;

function parts(value: string): { negative: boolean; integer: string; fraction: string } {
  const match = DECIMAL.exec(value);
  if (!match) throw new TypeError("Decimal canônico inválido.");
  return { negative: match[1] === "-", integer: match[2], fraction: match[3] ?? "" };
}

function group(integer: string): string {
  return integer.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

export function formatDecimal(value: string, minimumFractionDigits = 0): string {
  const parsed = parts(value);
  const fraction = parsed.fraction.padEnd(minimumFractionDigits, "0");
  return (parsed.negative ? "-" : "") + group(parsed.integer) + (fraction ? "," + fraction : "");
}

export function formatMoney(value: string): string {
  const parsed = parts(new Decimal(value).toDecimalPlaces(2, Decimal.ROUND_HALF_UP).toFixed(2));
  const absolute = group(parsed.integer) + "," + parsed.fraction;
  return (parsed.negative ? "- " : "") + "R$ " + absolute;
}

export function formatCurrency(value: string, currency: "BRL" | "USD"): string {
  const parsed = parts(new Decimal(value).toDecimalPlaces(2, Decimal.ROUND_HALF_UP).toFixed(2));
  const absolute = group(parsed.integer) + "," + parsed.fraction;
  return (parsed.negative ? "- " : "") + (currency === "BRL" ? "R$ " : "US$ ") + absolute;
}

export function formatAveragePrice(value: string, currency: "BRL" | "USD" = "BRL"): string {
  const rounded = parts(new Decimal(value).toDecimalPlaces(4, Decimal.ROUND_HALF_UP).toFixed(4));
  const significant = rounded.fraction.replace(/0+$/, "");
  const fraction = significant.padEnd(2, "0");
  return (rounded.negative ? "- " : "") + (currency === "BRL" ? "R$ " : "US$ ") + group(rounded.integer) + "," + fraction;
}

export function formatCurrencyInput(value: string): string {
  const rounded = parts(new Decimal(value).toDecimalPlaces(2, Decimal.ROUND_HALF_UP).toFixed(2));
  if (new Decimal(value).equals(new Decimal(`${rounded.negative ? "-" : ""}${rounded.integer}.${rounded.fraction}`))) {
    return (rounded.negative ? "-" : "") + group(rounded.integer) + "," + rounded.fraction;
  }
  const exact = parts(value);
  const fraction = exact.fraction.replace(/0+$/, "");
  return (exact.negative ? "-" : "") + group(exact.integer) + (fraction ? "," + fraction : "");
}

export function formatPercentage(value: string): string {
  return formatDecimal(value, 2) + "%";
}

export function formatQuantity(value: string): string {
  const whole = new Decimal(value).toDecimalPlaces(0, Decimal.ROUND_DOWN).toFixed(0);
  const parsed = parts(whole);
  return (parsed.negative ? "-" : "") + group(parsed.integer);
}

const DATE_TIME = new Intl.DateTimeFormat("pt-BR", {
  dateStyle: "short",
  timeStyle: "short",
  timeZone: "America/Sao_Paulo",
});

export function formatDateTime(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) throw new TypeError("Data/hora inválida.");
  return DATE_TIME.format(date);
}

export function decimalSign(value: string): "positive" | "negative" | "neutral" {
  const parsed = parts(value);
  if (/^0+$/.test(parsed.integer) && (!parsed.fraction || /^0+$/.test(parsed.fraction))) return "neutral";
  return parsed.negative ? "negative" : "positive";
}
