export const DEFAULT_RETURN_TO = "/inicio";

function isSafeInternalPath(value: string): boolean {
  if (!value.startsWith("/") || value.startsWith("//") || /[\\\\\s\u0000-\u001f]/.test(value) || /%(?![0-9a-f]{2})|%2f|%5c/i.test(value)) {
    return false;
  }
  try {
    const url = new URL(value, "http://return-to.invalid");
    return url.origin === "http://return-to.invalid" && url.pathname.startsWith("/");
  } catch {
    return false;
  }
}

export function safeReturnTo(value: string | null | undefined, fallback = DEFAULT_RETURN_TO): string {
  const safeFallback = isSafeInternalPath(fallback) ? fallback : DEFAULT_RETURN_TO;
  return typeof value === "string" && isSafeInternalPath(value) ? value : safeFallback;
}
