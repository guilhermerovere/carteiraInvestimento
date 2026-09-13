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
  const parsed = parts(value);
  const absolute = group(parsed.integer) + "," + parsed.fraction.padEnd(2, "0");
  return (parsed.negative ? "- " : "") + "R$ " + absolute;
}

export function formatPercentage(value: string): string {
  return formatDecimal(value, 2) + "%";
}

export function formatQuantity(value: string): string {
  return formatDecimal(value);
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
