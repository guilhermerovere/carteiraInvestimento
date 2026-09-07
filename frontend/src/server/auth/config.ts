import "server-only";

export type AuthConfig = { backendApiUrl: URL; appOrigin: URL };
type AuthEnv = { BACKEND_API_URL?: string; APP_ORIGIN?: string };
export function loadAuthConfig(env: AuthEnv = process.env as AuthEnv): AuthConfig {
  const backend = env.BACKEND_API_URL;
  const origin = env.APP_ORIGIN;
  if (!backend || !origin) throw new Error("BACKEND_API_URL e APP_ORIGIN são obrigatórias");
  let backendApiUrl: URL; let parsedAppOrigin: URL;
  try { backendApiUrl = new URL(backend); parsedAppOrigin = new URL(origin); } catch { throw new Error("BACKEND_API_URL e APP_ORIGIN devem ser URLs válidas"); }
  if (!/^https?:$/.test(backendApiUrl.protocol) || !/^https?:$/.test(parsedAppOrigin.protocol)) throw new Error("As URLs de autenticação devem usar HTTP ou HTTPS");
  const appOrigin = new URL(parsedAppOrigin.origin);
  return { backendApiUrl, appOrigin };
}
