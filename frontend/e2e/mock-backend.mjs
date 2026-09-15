import http from "node:http";
import { randomUUID } from "node:crypto";

const host = "127.0.0.1";
const port = 18080;
const correlationId = "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa";
let refreshed = false;
let userProfile = { id: "88888888-8888-4888-8888-888888888888", nome: "Pessoa Investidora", email: "user@example.test", role: "ROLE_USER", ativo: true };

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
  "posicoes":[{"ativoId":"11111111-1111-4111-8111-111111111111","ticker":"ACME3","tipo":"ACAO","mercado":"B3","moeda":"BRL","quantidade":0.10000001,"precoMedioBrl":123456789.12345678,"totalInvestidoBrl":12345678.91234567,"cotacaoAtual":125000000.12345678,"providerCotacao":"BRAPI","instanteCotacao":"2026-09-12T13:28:00Z","valorAtualOrigem":12500000.26250001,"valorAtualBrl":12500000.26250001,"lucroNaoRealizadoBrl":154321.35015434,"rentabilidadePercentual":1.25000001}]
}`;

const positions = `{"items":[{"id":"33333333-3333-4333-8333-333333333333","ativoId":"11111111-1111-4111-8111-111111111111","ticker":"ACME3","nome":"Acme Brasil S.A.","mercado":"B3","moeda":"BRL","ativo":false,"logoProvider":null,"logoReference":null,"quantidade":0.10000001,"precoMedioBrl":123456789.12345678,"totalInvestidoBrl":12345678.91234567,"lucroRealizadoAcumuladoBrl":-123456789.12345678,"ultimaAtualizacao":"2026-09-12T12:00:00Z"}],"page":0,"size":20,"totalElements":1,"totalPages":1}`;
const transactions = `{"items":[{"id":"44444444-4444-4444-8444-444444444444","ativoId":"11111111-1111-4111-8111-111111111111","ticker":"ACME3","corretoraId":"55555555-5555-4555-8555-555555555555","exchangeRateId":null,"tipo":"SELL","quantidade":0.00000001,"moeda":"BRL","precoUnitario":123456789.12345678,"taxas":0.10000001,"taxaCambioBrl":1.00000000,"valorTotalBrl":1.23456789,"resultadoRealizadoBrl":-0.10000001,"dataNegociacao":"2026-09-11T15:00:00Z","dataRegistro":"2026-09-11T15:01:00Z"}],"page":0,"size":20,"totalElements":1,"totalPages":1}`;
let transactionLedger = JSON.parse(transactions).items;
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
    if (token === "Bearer user-token") return json(response, 200, userProfile);
    return json(response, 401, { status: 401, title: "Não autenticado" }, "application/problem+json");
  }
  if (url.pathname === "/api/v1/auth/register" && request.method === "POST") {
    return json(response, 201, { id: "88888888-8888-4888-8888-888888888888", nome: "Pessoa", email: "user@example.test", role: "ROLE_USER", ativo: true });
  }

  const authorization = request.headers.authorization;
  if (["/api/v1/account/profile", "/api/v1/account/password", "/api/v1/account/closure"].includes(url.pathname)) {
    if (!["Bearer user-token", "Bearer admin-token"].includes(authorization)) return json(response, 401, { status: 401, title: "Não autenticado" }, "application/problem+json");
    if (url.pathname === "/api/v1/account/profile" && request.method === "PATCH") {
      const body = JSON.parse(await bodyOf(request));
      userProfile = { ...userProfile, ...(body.nome ? { nome: body.nome.trim() } : {}), ...(body.email ? { email: body.email.trim().toLowerCase() } : {}) };
      return json(response, 200, userProfile);
    }
    response.writeHead(204, { "Cache-Control": "no-store", "X-Correlation-ID": correlationId }); response.end(); return;
  }

  if (authorization === "Bearer admin-token") {
    const adminBrokers = { items: [{ id: "55555555-5555-4555-8555-555555555555", cnpj: "19131243000197", razaoSocial: "XP Investimentos CCTVM S.A.", nomeFantasia: "XP Investimentos", cep: "01310100", logradouro: "Avenida Paulista", bairro: "Bela Vista", cidade: "São Paulo", uf: "SP", numero: "1000", complemento: null, ativo: true, logoProvider: "LOGO_DEV", logoReference: "xpi.com.br", criadoEm: "2026-01-01T00:00:00Z", atualizadoEm: "2026-01-01T00:00:00Z" }], page: 0, size: 100, totalElements: 1, totalPages: 1 };
    const adminAssets = { items: [{ id: "11111111-1111-4111-8111-111111111111", ticker: "ACME3", nome: "Acme Brasil S.A.", tipo: "ACAO", mercado: "B3", moeda: "BRL", ativo: true, logoProvider: null, logoReference: null, criadoEm: "2026-01-01T00:00:00Z", atualizadoEm: "2026-01-01T00:00:00Z" }], page: 0, size: 100, totalElements: 1, totalPages: 1 };
    if (url.pathname === "/api/v1/corretoras" && request.method === "GET") return json(response, 200, adminBrokers);
    if (url.pathname === "/api/v1/corretoras" && request.method === "POST") return json(response, 201, adminBrokers.items[0]);
    if (url.pathname.startsWith("/api/v1/corretoras/") && request.method === "PATCH") return json(response, 200, { ...adminBrokers.items[0], ativo: !url.pathname.endsWith("/ativo") });
    if (url.pathname === "/api/v1/acoes" && request.method === "GET") return json(response, 200, adminAssets);
    if (url.pathname === "/api/v1/acoes" && request.method === "POST") return json(response, 201, adminAssets.items[0]);
    if (url.pathname.startsWith("/api/v1/acoes/") && request.method === "PATCH") return json(response, 200, { ...adminAssets.items[0], ativo: !url.pathname.endsWith("/ativo") });
  }

  if (authorization !== "Bearer user-token") {
    return json(response, 403, { status: 403, title: "Acesso negado" }, "application/problem+json");
  }
  if (url.pathname === "/api/v1/carteira/resumo" && request.method === "GET") return json(response, 200, summary());
  if (url.pathname === "/api/v1/carteira/graficos/evolucao" && request.method === "GET") return json(response, 200, [{dataReferencia:"2026-09-10",totalInvestidoBrl:120000000.00,resultadoNaoRealizadoBrl:1200000.00,valorPosicoesBrl:121200000.00,patrimonioTotalBrl:121201200.00},{dataReferencia:"2026-09-12",totalInvestidoBrl:123456789.12345678,resultadoNaoRealizadoBrl:1553211.00,valorPosicoesBrl:125000000.00,patrimonioTotalBrl:125001200.00}]);
  if (url.pathname === "/api/v1/carteira/resumo/atualizar" && request.method === "POST") {
    await new Promise((resolve) => setTimeout(resolve, 300));
    refreshed = true;
    return json(response, 200, summary());
  }
  if (url.pathname === "/api/v1/carteira/posicoes" && request.method === "GET") return json(response, 200, positions);
  if (url.pathname === "/api/v1/carteira/posicoes/11111111-1111-4111-8111-111111111111" && request.method === "GET") return json(response, 200, JSON.parse(positions).items[0]);
  if (url.pathname.startsWith("/api/v1/carteira/posicoes/") && request.method === "GET") return json(response, 404, {status:404,title:"Posição não encontrada"}, "application/problem+json");
  if (url.pathname === "/api/v1/carteira/transacoes" && request.method === "GET") { const page=Number(url.searchParams.get("page")??0),size=Number(url.searchParams.get("size")??20); const totalElements=transactionLedger.length; return json(response,200,{items:transactionLedger.slice(page*size,(page+1)*size),page,size,totalElements,totalPages:Math.ceil(totalElements/size)}); }
  if (url.pathname === "/api/v1/carteira/caixa" && request.method === "GET") return json(response, 200, "{\"saldoCaixaBrl\":1200.10000001}");
  if (url.pathname === "/api/v1/carteira/caixa/movimentacoes" && request.method === "GET") return json(response, 200, movements);
  if (["/api/v1/carteira/caixa/deposito","/api/v1/carteira/caixa/saque"].includes(url.pathname) && request.method === "POST") return json(response, 201, {movimentacao:{id:"77777777-7777-4777-8777-777777777777",tipo:url.pathname.endsWith("deposito")?"DEPOSITO":"SAQUE",valorBrl:10.00,descricao:"Operação de QA",dataHora:new Date().toISOString()},saldoResultante:url.pathname.endsWith("deposito")?1210.10:1190.10});
  if (url.pathname === "/api/v1/carteira/transacoes" && request.method === "POST") {
    const body=JSON.parse(await bodyOf(request)); const sale=body.tipo==="SELL"; const gross=Number(body.quantidade)*Number(body.precoUnitario); const transaction={id:randomUUID(),ativoId:body.ativoId,ticker:"ACME3",nome:"Acme Brasil S.A.",logoProvider:null,logoReference:null,corretoraId:body.corretoraId,corretoraNome:"XP Investimentos",exchangeRateId:body.exchangeRateId??null,tipo:body.tipo,quantidade:body.quantidade,moeda:"BRL",precoUnitario:body.precoUnitario,taxas:body.taxas,taxaCambioBrl:1.00000000,valorTotalBrl:gross+(sale?-Number(body.taxas):Number(body.taxas)),resultadoRealizadoBrl:sale?2.00:null,dataNegociacao:body.dataNegociacao,dataRegistro:new Date().toISOString()}; transactionLedger.unshift(transaction); return json(response,201,{transacao:transaction,valorOrigem:gross,saldoCaixaBrl:sale?1210.10:1190.10,posicao:{id:"33333333-3333-4333-8333-333333333333",ativoId:body.ativoId,ticker:"ACME3",nome:"Acme Brasil S.A.",mercado:"B3",moeda:"BRL",ativo:true,logoProvider:null,logoReference:null,quantidade:sale?0:body.quantidade,precoMedioBrl:sale?0:body.precoUnitario,totalInvestidoBrl:sale?0:gross,lucroRealizadoAcumuladoBrl:sale?2.00:0,ultimaAtualizacao:new Date().toISOString()}});
  }  if (url.pathname === "/api/v1/acoes" && request.method === "GET") return json(response, 200, {items:[{id:"11111111-1111-4111-8111-111111111111",ticker:"ACME3",nome:"Acme Brasil S.A.",tipo:"ACAO",mercado:"B3",moeda:"BRL",ativo:true,logoProvider:null,logoReference:null},{id:"22222222-2222-4222-8222-222222222222",ticker:"AAPL",nome:"Apple Inc.",tipo:"ACAO",mercado:"US",moeda:"USD",ativo:true,logoProvider:"LOGO_DEV",logoReference:"ticker/AAPL"}],page:0,size:20,totalElements:2,totalPages:1});
  if (url.pathname.startsWith("/api/v1/acoes/ticker/") && request.method === "GET") return json(response, 404, {status:404,title:"Ativo não encontrado"}, "application/problem+json");
  if (url.pathname === "/api/v1/acoes" && request.method === "POST") return json(response, 201, {id:"99999999-9999-4999-8999-999999999998",ticker:"BBAS3",nome:"Banco do Brasil S.A.",tipo:"ACAO",mercado:"B3",moeda:"BRL",ativo:true,logoProvider:null,logoReference:null});
  if (url.pathname === "/api/v1/corretoras" && request.method === "GET") return json(response, 200, {items:[{id:"55555555-5555-4555-8555-555555555555",razaoSocial:"XP Investimentos CCTVM S.A.",nomeFantasia:"XP Investimentos",logoProvider:"LOGO_DEV",logoReference:"xpi.com.br"}],page:0,size:20,totalElements:1,totalPages:1});
  if (url.pathname.endsWith("/cotacao") && request.method === "GET") return json(response, 200, {id:"66666666-6666-4666-8666-666666666666",ativoId:url.pathname.split("/")[4],preco:10.1234,moeda:url.pathname.includes("22222222")?"USD":"BRL",instanteCotacao:new Date().toISOString(),provider:"BRAPI",recebidoEm:new Date().toISOString()});
  if (url.pathname === "/api/v1/cambio/usd-brl" && request.method === "GET") return json(response, 200, {id:"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaab",moedaOrigem:"USD",moedaDestino:"BRL",taxa:5.12345678,instanteCotacao:new Date().toISOString(),provider:"TWELVE_DATA",registradoEm:new Date().toISOString()});
  return json(response, 404, { status: 404, title: "Não encontrado" }, "application/problem+json");
});

server.listen(port, host);
for (const signal of ["SIGINT", "SIGTERM"]) process.on(signal, () => {
  server.close();
  process.exit(0);
});
