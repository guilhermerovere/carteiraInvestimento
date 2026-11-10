import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { act, cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { FinanceApiError, financeApi } from "@/client/finance-api";
import type { Asset, AssetDiscovery, Broker } from "@/lib/finance/contracts";
import { AssetSelector } from "./asset-selector";
import { BrokerSelector } from "./broker-selector";

vi.mock("@/client/finance-api", async (load) => {
  const actual = await load<typeof import("@/client/finance-api")>();
  return { ...actual, financeApi: { ...actual.financeApi } };
});

const asset: Asset = { id:"11111111-1111-4111-8111-111111111111", ticker:"ACME3", nome:"Acme", tipo:"ACAO", mercado:"B3", moeda:"BRL", ativo:true, logoProvider:null, logoReference:null };
const usAsset: Asset = { ...asset, id:"99999999-9999-4999-8999-999999999999", ticker:"AAPL", nome:"Apple Inc.", mercado:"US", moeda:"USD" };
const broker: Broker = { id:"22222222-2222-4222-8222-222222222222", razaoSocial:"XP Investimentos CCTVM S.A.", nomeFantasia:"XP Investimentos", logoProvider:null, logoReference:null };
const discoveryPreview = (ticker: string, nome = ticker): AssetDiscovery => ({ ticker, nome, tipo:"ACAO", mercado:"B3", moeda:"BRL", logoProvider:null, logoReference:null, cotacaoAtual:"10.00" });
const usPreview: AssetDiscovery = { ticker:"AAPL", nome:"Apple Inc.", tipo:"ACAO", mercado:"US", moeda:"USD", logoProvider:"LOGO_DEV", logoReference:"ticker/AAPL", cotacaoAtual:"201.53500000" };
function wrapper(children: React.ReactNode) { return <QueryClientProvider client={new QueryClient({defaultOptions:{queries:{retry:false}}})}>{children}</QueryClientProvider>; }
function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => { resolve = resolvePromise; reject = rejectPromise; });
  return { promise, resolve, reject };
}

describe("operational selectors",()=>{
  beforeEach(()=>{vi.restoreAllMocks();vi.spyOn(financeApi,"discoverAssets").mockResolvedValue([]);});
  afterEach(cleanup);

  it("offers a B3 discovery result",async()=>{
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[],page:0,size:20,totalElements:0,totalPages:0});
    vi.spyOn(financeApi,"discoverAssets").mockResolvedValue([discoveryPreview("PETR4","Petrobras")]);
    vi.spyOn(financeApi,"exactAsset").mockResolvedValue({...asset,ticker:"PETR4"});
    const onChange=vi.fn(); render(wrapper(<AssetSelector value={null} onChange={onChange}/>));
    await userEvent.type(screen.getByPlaceholderText("Ticker ou nome"),"PET");
    await userEvent.click(await screen.findByRole("option",{name:/PETR4/}));
    await waitFor(()=>expect(onChange).toHaveBeenCalledWith(expect.objectContaining({ticker:"PETR4"})));
  });

  it("selects an exact active catalog match",async()=>{
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[asset],page:0,size:20,totalElements:1,totalPages:1});
    vi.spyOn(financeApi,"exactAsset").mockResolvedValue(asset);
    const onChange=vi.fn(); render(wrapper(<AssetSelector value={null} onChange={onChange}/>));
    await userEvent.type(screen.getByPlaceholderText("Ticker ou nome")," acme3 ");
    await userEvent.click(screen.getByRole("button",{name:"Verificar ticker"}));
    await waitFor(()=>expect(onChange).toHaveBeenCalledWith(asset));
    expect(screen.queryByText(/Cadastrar ticker/)).not.toBeInTheDocument();
  });

  it("shows and selects only catalog assets from the chosen market",async()=>{
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[asset,usAsset],page:0,size:20,totalElements:2,totalPages:1});
    const onChange=vi.fn(); render(wrapper(<AssetSelector value={null} onChange={onChange}/>));
    expect(await screen.findByRole("option",{name:/ACME3/})).toBeVisible();
    expect(screen.queryByRole("option",{name:/AAPL/})).not.toBeInTheDocument();
    await userEvent.selectOptions(screen.getByLabelText("Mercado"),"US");
    expect(await screen.findByRole("option",{name:/AAPL/})).toBeVisible();
    expect(screen.queryByRole("option",{name:/ACME3/})).not.toBeInTheDocument();
    await userEvent.click(screen.getByRole("option",{name:/AAPL/}));
    expect(onChange).toHaveBeenCalledWith(usAsset);
  });

  it("does not select an exact catalog match from another market",async()=>{
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[],page:0,size:20,totalElements:0,totalPages:0});
    vi.spyOn(financeApi,"discoverAssets").mockResolvedValue([usPreview]);
    vi.spyOn(financeApi,"exactAsset").mockResolvedValue({...asset,ticker:"AAPL"});
    const onChange=vi.fn(); render(wrapper(<AssetSelector value={null} onChange={onChange}/>));
    await userEvent.selectOptions(screen.getByLabelText("Mercado"),"US");
    await userEvent.type(screen.getByPlaceholderText("Ticker ou nome"),"AAPL");
    await userEvent.click(await screen.findByRole("option",{name:/AAPL/}));
    expect(await screen.findByLabelText("Pr\u00e9via de AAPL")).toBeVisible();
    expect(onChange).not.toHaveBeenCalled();
  });

  it("ignores a delayed exact selection after changing market",async()=>{
    const exact=deferred<Asset>();
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[],page:0,size:20,totalElements:0,totalPages:0});
    const exactAsset=vi.spyOn(financeApi,"exactAsset").mockReturnValue(exact.promise);
    const onChange=vi.fn(); render(wrapper(<AssetSelector value={null} onChange={onChange}/>));
    await userEvent.type(screen.getByPlaceholderText("Ticker ou nome"),"ACME3");
    await userEvent.click(screen.getByRole("button",{name:"Verificar ticker"}));
    await waitFor(()=>expect(exactAsset).toHaveBeenCalled());
    await userEvent.selectOptions(screen.getByLabelText("Mercado"),"US");
    exact.resolve(asset);
    await waitFor(()=>expect(screen.getByRole("button",{name:"Verificar ticker"})).toBeEnabled());
    expect(onChange).not.toHaveBeenCalled();
  });

  it("ignores a delayed missing preview after changing market",async()=>{
    const exact=deferred<Asset>();
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[],page:0,size:20,totalElements:0,totalPages:0});
    vi.spyOn(financeApi,"discoverAssets").mockResolvedValue([discoveryPreview("PETR4","Petrobras")]);
    vi.spyOn(financeApi,"exactAsset").mockReturnValue(exact.promise);
    render(wrapper(<AssetSelector value={null} onChange={vi.fn()}/>));
    await userEvent.type(screen.getByPlaceholderText("Ticker ou nome"),"PETR");
    await userEvent.click(await screen.findByRole("option",{name:/PETR4/}));
    await userEvent.selectOptions(screen.getByLabelText("Mercado"),"US");
    exact.reject(new FinanceApiError(404,{status:404,title:"ausente"}));
    await waitFor(()=>expect(screen.getByRole("button",{name:"Verificar ticker"})).toBeEnabled());
    expect(screen.queryByLabelText("Pr\u00e9via de PETR4")).not.toBeInTheDocument();
  });

  it("ignores a delayed validation error after changing market",async()=>{
    const exact=deferred<Asset>();
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[],page:0,size:20,totalElements:0,totalPages:0});
    vi.spyOn(financeApi,"exactAsset").mockReturnValue(exact.promise);
    render(wrapper(<AssetSelector value={null} onChange={vi.fn()}/>));
    await userEvent.type(screen.getByPlaceholderText("Ticker ou nome"),"ACME3");
    await userEvent.click(screen.getByRole("button",{name:"Verificar ticker"}));
    await userEvent.selectOptions(screen.getByLabelText("Mercado"),"US");
    exact.reject(new FinanceApiError(502,{status:502,title:"indispon\u00edvel"}));
    await waitFor(()=>expect(screen.getByRole("button",{name:"Verificar ticker"})).toBeEnabled());
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    expect(screen.queryByText("N\u00e3o foi poss\u00edvel validar este ativo agora.")).not.toBeInTheDocument();
  });

  it("requires an explicit US market, clears previous state, and registers only ticker and market",async()=>{
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[],page:0,size:20,totalElements:0,totalPages:0});
    const discover=vi.spyOn(financeApi,"discoverAssets").mockImplementation(async (_q, mercado) => mercado === "US" ? [usPreview] : []);
    vi.spyOn(financeApi,"exactAsset").mockRejectedValue(new FinanceApiError(404,{status:404,title:"ausente"}));
    const register=vi.spyOn(financeApi,"registerAsset").mockResolvedValue({...asset,...usPreview,id:"99999999-9999-4999-8999-999999999998"});
    render(wrapper(<AssetSelector value={null} onChange={vi.fn()}/>));
    await userEvent.selectOptions(screen.getByLabelText("Mercado"),"US");
    await userEvent.type(screen.getByPlaceholderText("Ticker ou nome"),"AAPL");
    await userEvent.click(await screen.findByRole("option",{name:/AAPL/}));
    await expect(screen.findByLabelText("Pr\u00e9via de AAPL")).resolves.toBeVisible();
    expect(screen.getByLabelText("Pr\u00e9via de AAPL")).toHaveTextContent(/US.*USD/);
    await userEvent.click(screen.getByRole("button",{name:"Cadastrar ativo"}));
    await waitFor(()=>expect(register).toHaveBeenCalledWith("AAPL","US"));
    expect(discover).toHaveBeenCalledWith("AAPL","US");
  });

  it("ignores a delayed registration after changing market",async()=>{
    const registration=deferred<Asset>();
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[],page:0,size:20,totalElements:0,totalPages:0});
    vi.spyOn(financeApi,"discoverAssets").mockImplementation(async (_q, mercado) => mercado === "US" ? [usPreview] : []);
    vi.spyOn(financeApi,"exactAsset").mockRejectedValue(new FinanceApiError(404,{status:404,title:"ausente"}));
    const register=vi.spyOn(financeApi,"registerAsset").mockReturnValue(registration.promise);
    const onChange=vi.fn(); render(wrapper(<AssetSelector value={null} onChange={onChange}/>));
    await userEvent.selectOptions(screen.getByLabelText("Mercado"),"US");
    await userEvent.type(screen.getByPlaceholderText("Ticker ou nome"),"AAPL");
    await userEvent.click(await screen.findByRole("option",{name:/AAPL/}));
    await userEvent.click(await screen.findByRole("button",{name:"Cadastrar ativo"}));
    await waitFor(()=>expect(register).toHaveBeenCalledWith("AAPL","US"));
    await userEvent.selectOptions(screen.getByLabelText("Mercado"),"B3");
    await act(async()=>{ registration.resolve(usAsset); await registration.promise; });
    expect(screen.getByLabelText("Mercado")).toHaveValue("B3");
    expect(onChange).not.toHaveBeenCalled();
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("Pr\u00e9via de AAPL")).not.toBeInTheDocument();
  });

  it("ignores a delayed conflict lookup after changing market",async()=>{
    const conflictLookup=deferred<Asset>();
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[],page:0,size:20,totalElements:0,totalPages:0});
    vi.spyOn(financeApi,"discoverAssets").mockImplementation(async (_q, mercado) => mercado === "US" ? [usPreview] : []);
    const exactAsset=vi.spyOn(financeApi,"exactAsset")
      .mockRejectedValueOnce(new FinanceApiError(404,{status:404,title:"ausente"}))
      .mockReturnValueOnce(conflictLookup.promise);
    vi.spyOn(financeApi,"registerAsset").mockRejectedValue(new FinanceApiError(409,{status:409,title:"conflito"}));
    const onChange=vi.fn(); render(wrapper(<AssetSelector value={null} onChange={onChange}/>));
    await userEvent.selectOptions(screen.getByLabelText("Mercado"),"US");
    await userEvent.type(screen.getByPlaceholderText("Ticker ou nome"),"AAPL");
    await userEvent.click(await screen.findByRole("option",{name:/AAPL/}));
    await userEvent.click(await screen.findByRole("button",{name:"Cadastrar ativo"}));
    await waitFor(()=>expect(exactAsset).toHaveBeenCalledTimes(2));
    await userEvent.selectOptions(screen.getByLabelText("Mercado"),"B3");
    await act(async()=>{ conflictLookup.resolve(usAsset); await conflictLookup.promise; });
    expect(screen.getByLabelText("Mercado")).toHaveValue("B3");
    expect(onChange).not.toHaveBeenCalled();
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("Pr\u00e9via de AAPL")).not.toBeInTheDocument();
  });

  it("does not offer a US candidate without a positive quote",async()=>{
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[],page:0,size:20,totalElements:0,totalPages:0});
    vi.spyOn(financeApi,"discoverAssets").mockResolvedValue([{...usPreview,cotacaoAtual:"0"}]);
    render(wrapper(<AssetSelector value={null} onChange={vi.fn()}/>));
    await userEvent.selectOptions(screen.getByLabelText("Mercado"),"US");
    await userEvent.type(screen.getByPlaceholderText("Ticker ou nome"),"AAPL");
    await waitFor(()=>expect(screen.queryByRole("option",{name:/AAPL/})).not.toBeInTheDocument());
    expect(screen.queryByRole("button",{name:"Cadastrar ativo"})).not.toBeInTheDocument();
  });

  it("blocks an inactive catalog match",async()=>{
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[],page:0,size:20,totalElements:0,totalPages:0});
    vi.spyOn(financeApi,"exactAsset").mockResolvedValue({...asset,ativo:false});
    render(wrapper(<AssetSelector value={null} onChange={vi.fn()}/>));
    await userEvent.type(screen.getByPlaceholderText("Ticker ou nome"),"ACME3");
    await userEvent.click(screen.getByRole("button",{name:"Verificar ticker"}));
    expect(await screen.findByText(/inativo/)).toBeVisible();
  });

  it("preserves ticker and blocks registration when validation is unavailable",async()=>{
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[],page:0,size:20,totalElements:0,totalPages:0});
    vi.spyOn(financeApi,"exactAsset").mockRejectedValue(new FinanceApiError(502,{status:502,title:"The financial asset validation service is temporarily unavailable."}));
    render(wrapper(<AssetSelector value={null} onChange={vi.fn()}/>));
    const input=screen.getByPlaceholderText("Ticker ou nome"); await userEvent.type(input,"ACME3");
    await userEvent.click(screen.getByRole("button",{name:"Verificar ticker"}));
    expect(await screen.findByText("N\u00e3o foi poss\u00edvel validar este ativo agora.")).toBeVisible();
    expect(input).toHaveValue("ACME3");
    expect(screen.queryByText(/Cadastrar ticker/)).not.toBeInTheDocument();
  });

  it("shows a B3 preview and registers only after explicit confirmation",async()=>{
    const preview={ticker:"HGLG11",nome:"CSHG Logistica FII",tipo:"FII",mercado:"B3",moeda:"BRL",logoProvider:null,logoReference:null,cotacaoAtual:"158.73000000"} satisfies AssetDiscovery;
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[],page:0,size:20,totalElements:0,totalPages:0});
    vi.spyOn(financeApi,"discoverAssets").mockResolvedValue([preview]);
    vi.spyOn(financeApi,"exactAsset").mockRejectedValue(new FinanceApiError(404,{status:404,title:"ausente"}));
    const register=vi.spyOn(financeApi,"registerAsset").mockResolvedValue({...asset,ticker:"HGLG11",nome:"CSHG Logistica FII",tipo:"FII"});
    render(wrapper(<AssetSelector value={null} onChange={vi.fn()}/>));
    await userEvent.type(screen.getByPlaceholderText("Ticker ou nome"),"HGLG");
    await userEvent.click(await screen.findByRole("option",{name:/HGLG11/}));
    expect(await screen.findByLabelText("Pr\u00e9via de HGLG11")).toBeVisible();
    expect(register).not.toHaveBeenCalled();
    await userEvent.click(screen.getByRole("button",{name:"Cadastrar ativo"}));
    await waitFor(()=>expect(register).toHaveBeenCalledWith("HGLG11","B3"));
  });

  it("abandons a preview without persisting",async()=>{
    vi.spyOn(financeApi,"assets").mockResolvedValue({items:[],page:0,size:20,totalElements:0,totalPages:0});
    vi.spyOn(financeApi,"discoverAssets").mockResolvedValue([discoveryPreview("PETR4","Petrobras")]);
    vi.spyOn(financeApi,"exactAsset").mockRejectedValue(new FinanceApiError(404,{status:404,title:"ausente"}));
    const register=vi.spyOn(financeApi,"registerAsset"); render(wrapper(<AssetSelector value={null} onChange={vi.fn()}/>));
    const input=screen.getByPlaceholderText("Ticker ou nome"); await userEvent.type(input,"PET");
    await userEvent.click(await screen.findByRole("option",{name:/PETR4/}));
    expect(await screen.findByLabelText("Pr\u00e9via de PETR4")).toBeVisible();
    await userEvent.clear(input); expect(screen.queryByLabelText("Pr\u00e9via de PETR4")).not.toBeInTheDocument(); expect(register).not.toHaveBeenCalled();
  });

  it("pages brokers and filters only loaded entries",async()=>{
    vi.spyOn(financeApi,"brokers").mockResolvedValueOnce({items:[broker],page:0,size:20,totalElements:2,totalPages:2}).mockResolvedValueOnce({items:[{...broker,id:"33333333-3333-4333-8333-333333333333",nomeFantasia:"BTG Pactual"}],page:1,size:20,totalElements:2,totalPages:2});
    render(wrapper(<BrokerSelector value={null} onChange={vi.fn()}/>));
    expect(await screen.findByRole("option",{name:/XP Investimentos/})).toBeVisible();
    await userEvent.click(screen.getByRole("button",{name:"Carregar mais"}));
    expect(await screen.findByRole("option",{name:/BTG Pactual/})).toBeVisible();
    await userEvent.type(screen.getByPlaceholderText("Filtrar p\u00e1ginas carregadas"),"XP");
    expect(screen.queryByRole("option",{name:/BTG/})).not.toBeInTheDocument();
  });
});
