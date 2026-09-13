import "server-only";
import { parse } from "lossless-json";

export function parseLosslessJson(text: string): unknown {
  return parse(text, null, { parseNumber: (source) => source });
}
