import "server-only";
import { LosslessNumber, parse, stringify } from "lossless-json";

export function parseLosslessJson(text: string): unknown {
  return parse(text, null, { parseNumber: (source) => source });
}

export function stringifyExactJson(value: Record<string, unknown>, decimalFields: ReadonlySet<string>): string {
  const exact = Object.fromEntries(Object.entries(value).map(([key, item]) => [key,
    decimalFields.has(key) && typeof item === "string" ? new LosslessNumber(item) : item,
  ]));
  return stringify(exact) as string;
}
