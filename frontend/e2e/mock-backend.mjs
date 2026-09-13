import http from "node:http";

const host = "127.0.0.1";
const port = 18080;
const correlationId = "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa";
let refreshed = false;

const summary = () => `{
  "valuationInstant":"2026-09-12T13:30:00Z",
  "saldoCaixaBrl":1200.10000001,
  "totalInvestidoBrl":123456789.12345678,
  "valorPosicoesBrl":125000000.12345678,
  "lucroNaoRealizadoBrl":1553211.00000000,
  "lucroRealizadoAcumuladoBrl":-123.45000000,
  "patrimonioTotalBrl":${refreshed ? "125001500.22345679" : "125001200.22345679"},
  "rentabilidadeNaoRealizadaPercentual":1.25781234,
  "cambioAtual":{"taxaCambioBrl":5.12345678,"provider":"ALPHA_VANTAGE","instanteCambio":"2026-09-12T13:29:00Z"},
  "posicoes":[{"ativoId":"11111111-1111-4111-8111-111111111111","ticker":"ACME3","mercado":"B3","moeda":"BRL","quantidade":0.10000001,"precoMedioBrl":123456789.12345678,"totalInvestidoBrl":12345678.91234567,"cotacaoAtual":125000000.12345678,"providerCotacao":"BRAPI","instanteCotacao":"2026-09-12T13:28:00Z","valorAtualOrigem":12500000.26250001,"valorAtualBrl":12500000.26250001,"lucroNaoRealizadoBrl":154321.35015434,"rentabilidadePercentual":1.25000001}]
}`;

const positions = `{"items":[{"id":"33333333-3333-4333-8333-333333333333","ativoId":"11111111-1111-4111-8111-111111111111","ticker":"ACME3","quantidade":0.10000001,"precoMedioBrl":123456789.12345678,"totalInvestidoBrl":12345678.91234567,"lucroRealizadoAcumuladoBrl":-123456789.12345678,"ultimaAtualizacao":"2026-09-12T12:00:00Z"}],"page":0,"size":20,"totalElements":1,"totalPages":1}`;
const transactions = `{"items":[{"id":"44444444-4444-4444-8444-444444444444","ativoId":"11111111-1111-4111-8111-111111111111","ticker":"ACME3","corretoraId":"55555555-5555-4555-8555-555555555555","exchangeRateId":null,"tipo":"SELL","quantidade":0.00000001,"moeda":"BRL","precoUnitario":123456789.12345678,"taxas":0.10000001,"taxaCambioBrl":1.00000000,"valorTotalBrl":1.23456789,"resultadoRealizadoBrl":-0.10000001,"dataNegociacao":"2026-09-11T15:00:00Z","dataRegistro":"2026-09-11T15:01:00Z"}],"page":0,"size":20,"totalElements":1,"totalPages":1}`;
const movements = `{"items":[{"id":"66666666-6666-4666-8666-666666666666","tipo":"DEPOSITO","valorBrl":500.00000000,"descricao":"Aporte mensal","dataHora":"2026-09-10T12:00:00Z"}],"page":0,"size":20,"totalElements":1,"totalPages":1}`;

function json(response, status, body, contentType = "application/json") {
  response.writeHead(status, { "Content-Type": contentType, "Cache-Control": "no-store", "X-Correlation-ID": correlationId });
  response.end(typeof body === "string" ? body : JSON.stringify(body));
}

function bodyOf(request) {
  return new Promise((resolve) => {
    let body = "";
    request.on("data", (chunk) => { body += chunk; });
    request.on("end", () => resolve(body));
  });
}

const server = http.createServer(async (request, response) => {
  const url = new URL(request.url ?? "/", `http://${host}:${port}`);
  if (url.pathname === "/health") return json(response, 200, { status: "ok" });

  if (url.pathname === "/api/v1/auth/login" && request.method === "POST") {
    const raw = await bodyOf(request);
    let email = "";
    try { email = JSON.parse(raw).email ?? ""; } catch { /* BFF covers malformed input. */ }
    return json(response, 200, { accessToken: email.includes("admin") ? "admin-token" : "user-token", tokenType: "Bearer", expiresIn: 3600 });
  }
  if (url.pathname === "/api/v1/auth/me" && request.method === "GET") {
    const token = request.headers.authorization;
    if (token === "Bearer admin-token") return json(response, 200, { id: "99999999-9999-4999-8999-999999999999", nome: "Admin QA", email: "admin@example.test", role: "ROLE_ADMIN", ativo: true });
    if (token === "Bearer user-token") return json(response, 200, { id: "88888888-8888-4888-8888-888888888888", nome: "Pessoa Investidora", email: "user@example.test", role: "ROLE_USER", ativo: true });
    return json(response, 401, { status: 401, title: "Não autenticado" }, "application/problem+json");
  }
  if (url.pathname === "/api/v1/auth/register" && request.method === "POST") {
    return json(response, 201, { id: "88888888-8888-4888-8888-888888888888", nome: "Pessoa", email: "user@example.test", role: "ROLE_USER", ativo: true });
  }

  if (request.headers.authorization !== "Bearer user-token") {
    return json(response, 403, { status: 403, title: "Acesso negado" }, "application/problem+json");
  }
  if (url.pathname === "/api/v1/carteira/resumo" && request.method === "GET") return json(response, 200, summary());
  if (url.pathname === "/api/v1/carteira/resumo/atualizar" && request.method === "POST") {
    await new Promise((resolve) => setTimeout(resolve, 300));
    refreshed = true;
    return json(response, 200, summary());
  }
  if (url.pathname === "/api/v1/carteira/posicoes" && request.method === "GET") return json(response, 200, positions);
  if (url.pathname === "/api/v1/carteira/transacoes" && request.method === "GET") return json(response, 200, transactions);
  if (url.pathname === "/api/v1/carteira/caixa" && request.method === "GET") return json(response, 200, "{\"saldoCaixaBrl\":1200.10000001}");
  if (url.pathname === "/api/v1/carteira/caixa/movimentacoes" && request.method === "GET") return json(response, 200, movements);
  return json(response, 404, { status: 404, title: "Não encontrado" }, "application/problem+json");
});

server.listen(port, host);
for (const signal of ["SIGINT", "SIGTERM"]) process.on(signal, () => {
  server.close();
  process.exit(0);
});
