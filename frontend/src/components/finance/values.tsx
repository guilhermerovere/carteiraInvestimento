import { ArrowDownRight, ArrowUpRight, Minus } from "lucide-react";
import { cn } from "@/lib/utils";
import { decimalSign, formatDateTime, formatMoney, formatPercentage, formatQuantity } from "@/lib/finance/format";

export function Money({ value, className }: { value: string; className?: string }) {
  return <span className={cn("financial-number", className)}>{formatMoney(value)}</span>;
}
export function Percentage({ value, className }: { value: string; className?: string }) {
  return <span className={cn("financial-number", className)}>{formatPercentage(value)}</span>;
}
export function Quantity({ value, className }: { value: string; className?: string }) {
  return <span className={cn("financial-number", className)}>{formatQuantity(value)}</span>;
}
export function DateTime({ value, className }: { value: string; className?: string }) {
  return <time className={className} dateTime={value}>{formatDateTime(value)}</time>;
}

export function GainLoss({ value, percentage, realized = false }: { value: string; percentage?: string | null; realized?: boolean }) {
  const sign = decimalSign(value);
  const Icon = sign === "positive" ? ArrowUpRight : sign === "negative" ? ArrowDownRight : Minus;
  const label = sign === "positive" ? "Lucro" : sign === "negative" ? "Prejuízo" : "Resultado neutro";
  return (
    <span className={cn("gain-loss", "gain-loss--" + sign)}>
      <Icon aria-hidden="true" />
      <span>
        <span className="gain-loss__label">{realized ? label + " realizado" : label}</span>
        <Money value={value} />
        {percentage != null && <span className="gain-loss__percentage"> · <Percentage value={percentage} /></span>}
      </span>
    </span>
  );
}
