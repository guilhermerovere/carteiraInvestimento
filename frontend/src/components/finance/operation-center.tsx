"use client";

import { createPortal } from "react-dom";
import { useEffect, useMemo, useRef, useState } from "react";
import { ArrowDownToLine, ArrowUpFromLine, ShoppingCart, X } from "lucide-react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { financeApi, FinanceApiError } from "@/client/finance-api";
import { ptBrError } from "@/client/presentation-errors";
import { freezeIntent, RECOVERY_KEY, saveAmbiguous } from "@/client/operation-intent";
import { portfolioKeys } from "@/client/portfolio-queries";
import { decimalGreaterThan, estimateBuyTotal, estimateCash, estimateSellNet, estimateSellResult, localDateTimeWithOffset } from "@/lib/finance/decimal";
import type { Asset, Broker, CashOperationResponse, CashPayload, ExchangeRate, FrozenOperationIntent, InvestmentOperationResponse, OperationKind, Position, TransactionPayload } from "@/lib/finance/contracts";
import { Button } from "@/components/ui/button";
import { AssetSelector } from "./asset-selector";
import { BrokerSelector } from "./broker-selector";
import { DecimalInput, MoneyInput, PriceInput, QuantityInput } from "./decimal-input";
import { EntityLogo } from "./entity-logo";
import { AveragePrice, Money, Quantity } from "./values";
import { useDismissibleDetails } from "@/components/use-dismissible-details";
import { formatAveragePrice, formatCurrency, formatCurrencyInput } from "@/lib/finance/format";

declare global { interface WindowEventMap { "valore:sell": CustomEvent<Position>; "valore:buy": CustomEvent<Asset> } }

type OperationResponse = CashOperationResponse | InvestmentOperationResponse;
type PortalTargets = { desktop: HTMLElement | null; mobile: HTMLElement | null };
const nowLocal = () => { const date = new Date(); date.setMinutes(date.getMinutes() - date.getTimezoneOffset()); return date.toISOString().slice(0, 16); };
const operationLabel = (kind: OperationKind) => ({ DEPOSIT: "Depósito", WITHDRAW: "Saque", BUY: "Compra", SELL: "Venda" })[kind];

function readRecovery(): FrozenOperationIntent | null {
  try {
    const raw = sessionStorage.getItem(RECOVERY_KEY);
    if (!raw) return null;
    const value = JSON.parse(raw) as FrozenOperationIntent;
    return value.version === 1 && value.ambiguous && typeof value.key === "string" ? value : null;
  } catch { return null; }
}

function OperationMenu({ launch, collapsed = false, mobile = false }: { launch: (kind: OperationKind) => void; collapsed?: boolean; mobile?: boolean }) {
  const menu = useDismissibleDetails();
  return <div className={`operation-launcher${mobile ? " operation-launcher--mobile" : ""}`}>
    <details ref={menu}>
      <summary className={mobile ? "shell-nav__link" : "operation-launcher__summary"} aria-label="Operar">
        <ShoppingCart aria-hidden="true" /><span className={collapsed ? "sr-only" : "operation-launcher__label"}>Operar</span>
        {collapsed && <span className="sidebar-tooltip" role="tooltip">Operar</span>}
      </summary>
      <div className="operation-launcher__menu" role="menu" aria-label="Operações financeiras">
        <button role="menuitem" onClick={() => launch("BUY")}><ShoppingCart aria-hidden="true" />Comprar</button>
        <button role="menuitem" onClick={() => launch("DEPOSIT")}><ArrowDownToLine aria-hidden="true" />Depositar</button>
        <button role="menuitem" onClick={() => launch("WITHDRAW")}><ArrowUpFromLine aria-hidden="true" />Sacar</button>
      </div>
    </details>
  </div>;
}

export function OperationLauncher({ collapsed = false }: { collapsed?: boolean }) {
  const [open, setOpen] = useState(false);
  const [kind, setKind] = useState<OperationKind>("BUY");
  const [sell, setSell] = useState<Position | null>(null);
  const [buyAsset, setBuyAsset] = useState<Asset | null>(null);
  const [targets, setTargets] = useState<PortalTargets>({ desktop: null, mobile: null });
  const [recovery, setRecovery] = useState<FrozenOperationIntent | null>(() => typeof window === "undefined" ? null : readRecovery());

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      setTargets({ desktop: document.getElementById("desktop-operation-slot"), mobile: document.getElementById("mobile-operation-slot") });
    });
    const handler = (event: CustomEvent<Position>) => { setBuyAsset(null); setSell(event.detail); setKind("SELL"); setOpen(true); };
    const buyHandler = (event: CustomEvent<Asset>) => { setSell(null); setBuyAsset(event.detail); setKind("BUY"); setOpen(true); };
    window.addEventListener("valore:sell", handler);
    window.addEventListener("valore:buy", buyHandler);
    return () => { window.cancelAnimationFrame(frame); window.removeEventListener("valore:sell", handler); window.removeEventListener("valore:buy", buyHandler); };
  }, []);

  const launch = (next: OperationKind) => { setSell(null); setBuyAsset(null); setKind(next); setOpen(true); };
  return <>
    {targets.desktop && createPortal(<OperationMenu launch={launch} collapsed={collapsed} />, targets.desktop)}
    {targets.mobile && createPortal(<OperationMenu launch={launch} mobile />, targets.mobile)}
    {recovery && <button className="recovery-banner" onClick={() => { setKind(recovery.kind); setOpen(true); }}>
      <strong>Existe uma operação com resultado pendente.</strong><span>Revisar operação</span>
    </button>}
    {open && <OperationDialog initialKind={kind} sellPosition={sell} initialAsset={buyAsset} recovery={recovery?.kind === kind ? recovery : null} onRecovery={setRecovery} onClose={() => setOpen(false)} />}
  </>;
}

function ReviewSummary({ intent, asset, broker, totalBrl }: { intent: FrozenOperationIntent; asset: Asset | null; broker: Broker | null; totalBrl: string | null }) {
  if (intent.kind === "DEPOSIT" || intent.kind === "WITHDRAW") {
    const payload = intent.payload as CashPayload;
    return <dl><div><dt>Operação</dt><dd>{operationLabel(intent.kind)}</dd></div><div><dt>Valor</dt><dd><Money value={payload.valor} /></dd></div>{payload.descricao && <div><dt>Descrição</dt><dd>{payload.descricao}</dd></div>}</dl>;
  }
  const payload = intent.payload as TransactionPayload;
  return <dl>
    <div><dt>Operação</dt><dd>{operationLabel(intent.kind)}</dd></div>
    <div><dt>Ativo</dt><dd>{asset?.ticker ?? "Ativo selecionado"}</dd></div>
    <div><dt>Corretora</dt><dd>{broker?.nomeFantasia ?? broker?.razaoSocial ?? "Corretora selecionada"}</dd></div>
    <div><dt>Quantidade</dt><dd><Quantity value={payload.quantidade} /></dd></div>
    <div><dt>Preço unitário</dt><dd>{formatCurrency(payload.precoUnitario, asset?.moeda ?? "BRL")}</dd></div>
    <div><dt>Taxas</dt><dd><Money value={payload.taxas} /></dd></div>
    {totalBrl && <div><dt>Total a pagar</dt><dd><Money value={totalBrl} /></dd></div>}
    <div><dt>Data da negociação</dt><dd>{new Date(payload.dataNegociacao).toLocaleString("pt-BR")}</dd></div>
  </dl>;
}

export function OperationResult({ kind, result, asset, broker, onClose }: { kind: OperationKind; result: OperationResponse; asset: Asset | null; broker: Broker | null; onClose: () => void }) {
  if ("movimentacao" in result) {
    const deposit = kind === "DEPOSIT";
    return <div className="operation-success" aria-live="polite">
      <strong>{deposit ? "Depósito realizado com sucesso" : "Saque realizado com sucesso"}</strong>
      <dl className="operation-result-grid">
        <div><dt>{deposit ? "Valor" : "Valor sacado"}</dt><dd><Money value={result.movimentacao.valorBrl} /></dd></div>
        <div><dt>Novo saldo</dt><dd><Money value={result.saldoResultante} /></dd></div>
        {result.movimentacao.descricao && <div><dt>Descrição</dt><dd>{result.movimentacao.descricao}</dd></div>}
      </dl>
      <Button onClick={onClose}>Concluir</Button>
    </div>;
  }
  const sale = kind === "SELL";
  return <div className="operation-success" aria-live="polite">
    <strong>{sale ? "Venda registrada com sucesso" : "Compra registrada com sucesso"}</strong>
    <div className="locked-asset"><EntityLogo provider={asset?.logoProvider ?? result.posicao.logoProvider} reference={asset?.logoReference ?? result.posicao.logoReference} label={result.transacao.ticker} /><span><strong>{result.transacao.ticker}</strong><small>{asset?.nome ?? result.posicao.nome}</small></span></div>
    <dl className="operation-result-grid">
      <div><dt>Quantidade</dt><dd><Quantity value={result.transacao.quantidade} /></dd></div>
      <div><dt>Preço</dt><dd>{formatCurrency(result.transacao.precoUnitario, result.transacao.moeda)}</dd></div>
      <div><dt>Valor da operação</dt><dd><Money value={result.transacao.valorTotalBrl} /></dd></div>
      {broker && <div><dt>Corretora</dt><dd>{broker.nomeFantasia ?? broker.razaoSocial}</dd></div>}
      <div><dt>Saldo resultante</dt><dd><Money value={result.saldoCaixaBrl} /></dd></div>
      {sale && result.transacao.resultadoRealizadoBrl != null && <div><dt>Resultado realizado</dt><dd><Money value={result.transacao.resultadoRealizadoBrl} /></dd></div>}
      <div><dt>Quantidade restante</dt><dd><Quantity value={result.posicao.quantidade} /></dd></div>
      <div><dt>Preço médio restante</dt><dd><AveragePrice value={result.posicao.precoMedioBrl} /></dd></div>
    </dl>
    <Button onClick={onClose}>Concluir</Button>
  </div>;
}

function OperationDialog({ initialKind, sellPosition, initialAsset, recovery, onRecovery, onClose }: { initialKind: OperationKind; sellPosition: Position | null; initialAsset: Asset | null; recovery: FrozenOperationIntent | null; onRecovery: (value: FrozenOperationIntent | null) => void; onClose: () => void }) {
  const dialog = useRef<HTMLDialogElement>(null);
  const queryClient = useQueryClient();
  const [phase, setPhase] = useState<"form" | "review" | "success">(recovery ? "review" : "form");
  const [kind] = useState(initialKind);
  const [asset, setAsset] = useState<Asset | null>(sellPosition ? { id: sellPosition.ativoId, ticker: sellPosition.ticker, nome: sellPosition.nome, tipo: "ACAO", mercado: sellPosition.mercado, moeda: sellPosition.moeda, ativo: sellPosition.ativo, logoProvider: sellPosition.logoProvider, logoReference: sellPosition.logoReference } : initialAsset);
  const [broker, setBroker] = useState<Broker | null>(null);
  const [amount, setAmount] = useState("");
  const [amountValue, setAmountValue] = useState<string>();
  const [description, setDescription] = useState("");
  const [quantity, setQuantity] = useState("");
  const [quantityValue, setQuantityValue] = useState<string>();
  const [price, setPrice] = useState("");
  const [priceValue, setPriceValue] = useState<string>();
  const [fees, setFees] = useState("0,00");
  const [feesValue, setFeesValue] = useState<string>("0.00");
  const [date, setDate] = useState(nowLocal);
  const [fx, setFx] = useState<ExchangeRate | null>(null);
  const [intent, setIntent] = useState<FrozenOperationIntent | null>(recovery);
  const [message, setMessage] = useState("");
  const [result, setResult] = useState<OperationResponse | null>(null);
  const localGuard = useRef(false);
  const selectedAssetId = asset?.id;
  const cash = useQuery({ queryKey: portfolioKeys.cashBalance, queryFn: financeApi.cash, retry: false });
  const currentPosition = useQuery({ queryKey: portfolioKeys.position(asset?.id ?? "none"), queryFn: async () => { try { return await financeApi.position(asset!.id); } catch (error) { if (error instanceof FinanceApiError && error.status === 404) return null; throw error; } }, enabled: kind === "BUY" && !!asset, retry: false });

  useEffect(() => { const node = dialog.current; node?.showModal(); return () => node?.close(); }, []);
  useEffect(() => { if (asset?.mercado === "US" && !fx) financeApi.fx().then(setFx).catch((error) => setMessage(ptBrError(error))); }, [asset, fx]);
  useEffect(() => {
    if (!selectedAssetId) return;
    let cancelled = false;
    financeApi.quote(selectedAssetId).then((quote) => {
      if (!cancelled) { setPrice(formatCurrencyInput(quote.preco)); setPriceValue(quote.preco); }
    }).catch((error) => { if (!cancelled) setMessage(ptBrError(error, "quote")); });
    return () => { cancelled = true; };
  }, [selectedAssetId]);

  async function consultQuote() {
    if (!asset) return;
    setMessage("");
    try {
      const quote = await financeApi.quote(asset.id);
      setPrice(formatCurrencyInput(quote.preco)); setPriceValue(quote.preco);
      setMessage(`Cotação sugerida por ${quote.provider}, observada em ${new Date(quote.instanteCotacao).toLocaleString("pt-BR")}.`);
    } catch (error) { setMessage(ptBrError(error, "quote")); }
  }

  const payload = useMemo(() => {
    if (kind === "DEPOSIT" || kind === "WITHDRAW") return amountValue ? { valor: amountValue, ...(description.trim() ? { descricao: description.trim() } : {}) } satisfies CashPayload : null;
    if (!asset || !broker || !quantityValue || !priceValue || feesValue === undefined) return null;
    try { return { ativoId: asset.id, corretoraId: broker.id, tipo: kind, quantidade: quantityValue, precoUnitario: priceValue, taxas: feesValue, dataNegociacao: localDateTimeWithOffset(date), exchangeRateId: asset.mercado === "US" ? fx?.id ?? null : null } satisfies TransactionPayload; }
    catch { return null; }
  }, [kind, amountValue, description, asset, broker, quantityValue, priceValue, feesValue, date, fx]);

  const execute = useMutation({ mutationFn: async (current: FrozenOperationIntent): Promise<OperationResponse> => {
    if (current.kind === "DEPOSIT") return financeApi.deposit(current.payload as CashPayload, current.key);
    if (current.kind === "WITHDRAW") return financeApi.withdraw(current.payload as CashPayload, current.key);
    return financeApi.executeTransaction(current.payload as TransactionPayload, current.key);
  }, retry: false });

  async function confirm() {
    if (localGuard.current || execute.isPending || !intent) return;
    localGuard.current = true; setMessage("");
    try {
      const value = await execute.mutateAsync(intent);
      sessionStorage.removeItem(RECOVERY_KEY); onRecovery(null); setResult(value); setPhase("success");
      if (kind === "DEPOSIT" || kind === "WITHDRAW") {
        void queryClient.invalidateQueries({ queryKey: portfolioKeys.summary, exact: true });
        void queryClient.invalidateQueries({ queryKey: portfolioKeys.cashBalance, exact: true });
        void queryClient.invalidateQueries({ queryKey: ["portfolio", "cash", "movements"] });
      } else {
        void queryClient.invalidateQueries({ queryKey: portfolioKeys.summary, exact: true });
        void queryClient.invalidateQueries({ queryKey: ["portfolio", "positions"] });
        void queryClient.invalidateQueries({ queryKey: portfolioKeys.transactionPages });
        if (asset) void queryClient.invalidateQueries({ queryKey: portfolioKeys.position(asset.id), exact: true });
      }
    } catch (error) {
      if (error instanceof FinanceApiError && error.status === 0) {
        const ambiguous = { ...intent, ambiguous: true }; saveAmbiguous(ambiguous); onRecovery(ambiguous); setIntent(ambiguous);
      } else {
        sessionStorage.removeItem(RECOVERY_KEY); onRecovery(null);
        if (error instanceof FinanceApiError && error.problem.code === "FX_EXPIRED") { setIntent(null); setFx(null); setPhase("form"); }
      }
      setMessage(ptBrError(error));
    } finally { localGuard.current = false; }
  }

  function review() {
    if (!payload) { setMessage("Preencha os campos obrigatórios."); return; }
    setIntent(freezeIntent(kind, payload)); setPhase("review"); setMessage("");
  }

  const estimated = kind === "DEPOSIT" || kind === "WITHDRAW"
    ? (cash.data && amountValue ? estimateCash(cash.data.saldoCaixaBrl, amountValue, kind === "WITHDRAW") : null)
    : (quantityValue && priceValue && feesValue ? (kind === "SELL" ? estimateSellNet(quantityValue, priceValue, feesValue, asset?.mercado === "US" ? fx?.taxa ?? "1" : "1") : asset ? estimateBuyTotal(quantityValue, priceValue, feesValue, asset.mercado, fx?.taxa) : null) : null);
  const sellResultEstimate = kind === "SELL" && estimated && quantityValue && sellPosition ? estimateSellResult(estimated, quantityValue, sellPosition.quantidade, sellPosition.totalInvestidoBrl) : null;
  const close = () => { if (!execute.isPending && !intent?.ambiguous) onClose(); };

  return <dialog ref={dialog} className="operation-dialog" aria-labelledby="operation-title" onCancel={(event) => { if (execute.isPending || intent?.ambiguous) event.preventDefault(); else onClose(); }}>
    <div className="operation-dialog__header"><div><h2 id="operation-title">Operação financeira</h2><p>{recovery ? "Resultado pendente para confirmação." : "Compre ou venda ações de forma simples e segura."}</p></div><Button variant="ghost" size="icon" aria-label="Fechar" onClick={close} disabled={execute.isPending || !!intent?.ambiguous}><X aria-hidden="true" /></Button></div>
    <div className="operation-dialog__body">
      {phase === "form" && <>
        {(kind === "DEPOSIT" || kind === "WITHDRAW") ? <>
          <div className="balance-context"><span>Saldo conhecido</span><strong>{cash.data ? <Money value={cash.data.saldoCaixaBrl} /> : "—"}</strong></div>
          <MoneyInput label="Valor" currency="BRL" value={amount} onValueChange={(display, canonical) => { setAmount(display); setAmountValue(canonical); }} required />
          <label className="operation-field"><span>Descrição (opcional)</span><input maxLength={255} value={description} onChange={(event) => setDescription(event.target.value)} /></label>
          {kind === "WITHDRAW" && cash.data && amountValue && decimalGreaterThan(amountValue, cash.data.saldoCaixaBrl) && <p className="operation-warning">O valor supera o saldo conhecido. O servidor fará a validação definitiva.</p>}
        </> : <>
          <div className="operation-tabs" role="tablist" aria-label="Tipo de operação"><span role="tab" aria-selected={kind === "BUY"}>Compra</span><span role="tab" aria-selected={kind === "SELL"}>Venda</span></div>
          {kind === "BUY" ? <AssetSelector value={asset} onChange={(selected, info) => { setAsset(selected); if (info) setMessage(info); }} /> : null}
          {asset && <div className="locked-asset operation-asset-summary"><EntityLogo provider={asset.logoProvider} reference={asset.logoReference} label={asset.ticker} /><span><strong>{asset.ticker}</strong><small>{asset.nome}</small></span><span><small>Cotação atual</small><strong>{priceValue ? <span>{formatCurrency(priceValue, asset.moeda)}</span> : "—"}</strong></span>{kind === "SELL" && (currentPosition.data || sellPosition) && <span><small>Preço médio</small><strong>{formatAveragePrice((currentPosition.data ?? sellPosition)!.precoMedioBrl, asset.moeda)}</strong></span>}</div>}
          {asset && <><Button variant="secondary" onClick={consultQuote}>Atualizar cotação</Button><BrokerSelector value={broker} onChange={setBroker} /><div className="operation-grid"><QuantityInput label="Quantidade" value={quantity} onValueChange={(display, canonical) => { setQuantity(display); setQuantityValue(canonical); }} /><PriceInput label="Preço unitário" currency={asset.moeda} value={price} onValueChange={(display, canonical) => { setPrice(display); setPriceValue(canonical); }} /><DecimalInput label="Taxas" currency="BRL" kind="money" value={fees} onValueChange={(display, canonical) => { setFees(display); setFeesValue(canonical); }} scale={8} /><label className="operation-field"><span>Data e hora · fuso {Intl.DateTimeFormat().resolvedOptions().timeZone}</span><input type="datetime-local" value={date} onChange={(event) => setDate(event.target.value)} /></label></div>{asset.mercado === "US" && <div className="fx-card"><span>USD/BRL</span><strong>{fx?.taxa ?? "Carregando..."}</strong><small>{fx ? `Registro ${new Date(fx.registradoEm).toLocaleString("pt-BR")}` : "A taxa persistida é necessária para confirmar."}</small></div>}</>}
        </>}
        {estimated && <div className="estimate">{kind === "BUY" ? <><span>Total a pagar</span><strong><Money value={estimated} /></strong><span>Saldo disponível</span><strong>{cash.data ? <Money value={cash.data.saldoCaixaBrl} /> : "Indisponível"}</strong></> : <><span>{kind === "SELL" ? "Total líquido estimado" : "Total projetado estimado"}</span><strong><Money value={estimated} /></strong>{kind === "SELL" && sellPosition && <span>Preço médio: {formatAveragePrice(sellPosition.precoMedioBrl, sellPosition.moeda)}</span>}{sellResultEstimate && <strong>Resultado realizado estimado: <Money value={sellResultEstimate} /></strong>}<small>Estimativa. A resposta do servidor é a contabilização definitiva.</small></>}</div>}
        {message && <p role="status" className="operation-message">{message}</p>}
        <div className="operation-actions"><Button variant="ghost" onClick={close}>Cancelar</Button><Button onClick={review} disabled={!payload}>Revisar</Button></div>
      </>}
      {phase === "review" && intent && <>
        <div className="review-card">{asset && <div className="locked-asset"><EntityLogo provider={asset.logoProvider} reference={asset.logoReference} label={asset.ticker} /><span><strong>{asset.ticker} · {asset.nome}</strong><small>{asset.mercado} · {asset.moeda}</small></span></div>}<ReviewSummary intent={intent} asset={asset} broker={broker} totalBrl={kind === "BUY" ? estimated : null} /><p><strong>Status:</strong> {intent.ambiguous ? "Resultado ambíguo; reenvio manual com os mesmos dados." : "Pronta para confirmação"}</p></div>
        {message && <p role="alert" className="operation-error">{message}</p>}
        <div className="operation-actions">{!intent.ambiguous && <Button variant="ghost" onClick={() => { setIntent(null); setPhase("form"); }}>Editar</Button>}<Button onClick={confirm} disabled={execute.isPending}>{execute.isPending ? "Processando..." : intent.ambiguous ? "Tentar novamente" : "Confirmar operação"}</Button></div>
      </>}
      {phase === "success" && result && <OperationResult kind={kind} result={result} asset={asset} broker={broker} onClose={onClose} />}
    </div>
  </dialog>;
}
