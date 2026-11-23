package com.carteira.carteiraInvestimento.infrastructure.provider;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.carteira.carteiraInvestimento.domain.asset.*;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;
import com.carteira.carteiraInvestimento.infrastructure.config.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.io.InputStream;
import java.util.Map;
import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogBrandingAdaptersTest {
  private MarketQuoteProperties properties() {
    var configured=new MarketQuoteProperties.Provider("https://provider.test","credential");
    return new MarketQuoteProperties(configured,configured,configured);
  }
  @Test void brapiMapsObservedV2MetadataAndTrustedLogoForB3Stocks() throws Exception {
    BrapiClient client=mock(BrapiClient.class);
    var mapper = new ObjectMapper();
    var adapter = new BrapiQuoteAdapter(client,properties());
    for (var fixture : List.of(
        new String[]{"PETR4", "Petroleo Brasileiro SA Pfd", "49"},
        new String[]{"VALE3", "Vale SA", "62.15"},
        new String[]{"ITUB4", "Itau Unibanco Holding SA Pref", "41.20"})) {
      BrapiResponse response = mapper.readValue("""
          {"results":[{"requestedSymbol":"%s","symbol":"%s","changed":false,"data":{
          "shortName":"%s","longName":"%s","currency":"BRL","regularMarketPrice":%s,
          "logourl":"https://icons.brapi.dev/icons/%s.svg"}}]}
          """.formatted(fixture[0], fixture[0], fixture[0], fixture[1], fixture[2], fixture[0]), BrapiResponse.class);
      when(client.tickers(fixture[0])).thenReturn(new BrapiTickersResponse(List.of(
        new BrapiTickerIdentity(fixture[0],fixture[1],"stock","https://icons.brapi.dev/icons/"+fixture[0]+".svg"))));
      when(client.quote(eq(fixture[0]),any())).thenReturn(response);
      var metadata=adapter.validate(fixture[0]);
      assertThat(metadata.ticker()).isEqualTo(fixture[0]);
      assertThat(metadata.name()).isEqualTo(fixture[1]);
      assertThat(metadata.type()).isEqualTo(TipoAtivo.ACAO);
      assertThat(metadata.logoProvider()).isEqualTo(LogoProvider.BRAPI);
      assertThat(metadata.logoReference()).isEqualTo("https://icons.brapi.dev/icons/" + fixture[0] + ".svg");
      assertThat(adapter.obter(fixture[0]).preco()).isEqualByComparingTo(fixture[2]);
      assertThat(adapter.obter(fixture[0]).moeda()).isEqualTo(Moeda.BRL);
    }
  }
  @Test void brapiKeepsFinancialAssetWhenLogoIsAbsentOrUntrustedAndRejectsUnsupportedType() {
    BrapiClient client=mock(BrapiClient.class);
    when(client.tickers("IVVB11")).thenReturn(new BrapiTickersResponse(List.of(
      new BrapiTickerIdentity("IVVB11","IVVB11","etf","https://evil.test/logo.svg"))));
    when(client.quote(eq("IVVB11"),any())).thenReturn(new BrapiResponse(List.of(new BrapiResult(
      "IVVB11","IVVB11",false,new BrapiQuoteData(BigDecimal.TEN,"BRL","2026-09-13T10:00:00Z")))));
    assertThat(new BrapiQuoteAdapter(client,properties()).validate("IVVB11").logoReference()).isNull();
    when(client.tickers("FUND11")).thenReturn(new BrapiTickersResponse(List.of(
      new BrapiTickerIdentity("FUND11","Fund","mutual fund",null))));
    when(client.quote(eq("FUND11"),any())).thenReturn(new BrapiResponse(List.of(new BrapiResult(
      "FUND11","FUND11",false,new BrapiQuoteData(BigDecimal.TEN,"BRL","2026-09-13T10:00:00Z")))));
    assertThatThrownBy(()->new BrapiQuoteAdapter(client,properties()).validate("FUND11")).isInstanceOf(IllegalArgumentException.class);
  }
  @Test void logoDevRequiresOneExactDomainAndUsesMatchAndSecretOnlyServerSide() {
    LogoDevSearchClient client=mock(LogoDevSearchClient.class);
    var adapter=new LogoDevBrokerBrandingAdapter(client,new LogoDevProperties("https://api.logo.dev","secret"));
    when(client.search(eq("XP Investimentos"),eq("match"),eq("Bearer secret"))).thenReturn(List.of(new LogoDevSearchResult("XP Investimentos","www.xpi.com.br")));
    assertThat(adapter.resolve(null,"XP Investimentos")).contains("xpi.com.br");
    var ambiguousClient=mock(LogoDevSearchClient.class);
    when(ambiguousClient.search(any(),eq("match"),any())).thenReturn(List.of(new LogoDevSearchResult("XP Investimentos","xpi.com.br"),new LogoDevSearchResult("XP Investimentos","xp.com")));
    assertThat(new LogoDevBrokerBrandingAdapter(ambiguousClient,new LogoDevProperties("https://api.logo.dev","secret")).resolve(null,"XP Investimentos")).isEmpty();
  }
  @Test void brapiUsesPublicTickerIdentityDiscoveryAndAuthenticatedQuotes() {
    BrapiClient client=mock(BrapiClient.class);
    when(client.tickers("PETR4")).thenReturn(new BrapiTickersResponse(List.of(
      new BrapiTickerIdentity("PETR4","Petrobras","stock",null),
      new BrapiTickerIdentity("PETR4F","Petrobras fracionário","stock",null))));
    when(client.quote(eq("PETR4,PETR4F"),eq("Bearer credential"))).thenReturn(new BrapiResponse(List.of(
      new BrapiResult("PETR4","PETR4",false,new BrapiQuoteData(BigDecimal.TEN,"BRL",null)),
      new BrapiResult("PETR4F","PETR4F",false,new BrapiQuoteData(BigDecimal.ONE,"BRL",null)))));
    assertThat(new BrapiQuoteAdapter(client,properties()).discover("PETR4"))
      .extracting(com.carteira.carteiraInvestimento.application.port.AssetDiscoveryProviderPort.AssetPreview::ticker)
      .containsExactly("PETR4","PETR4F");
    verify(client).tickers("PETR4");
  }

  @Test void brapiClassifiesRealShapedIdentitiesWhenQuotesOmitType() throws Exception {
    BrapiClient client=mock(BrapiClient.class);
    BrapiTickersResponse identities=fixture("/provider/brapi/tickers-identity.json",BrapiTickersResponse.class);
    BrapiResponse quotes=fixture("/provider/brapi/quotes-without-type.json",BrapiResponse.class);
    when(client.tickers(anyString())).thenReturn(identities);
    when(client.quote(anyString(),any())).thenReturn(quotes);
    var adapter=new BrapiQuoteAdapter(client,properties());
    Map<String,TipoAtivo> expected=Map.of("SANB11",TipoAtivo.ACAO,"HGLG11",TipoAtivo.FII,
      "BOVA11",TipoAtivo.ETF,"PETR4",TipoAtivo.ACAO);
    for(var entry:expected.entrySet()) {
      var metadata=adapter.validate(entry.getKey());
      assertThat(metadata.ticker()).isEqualTo(entry.getKey());
      assertThat(metadata.type()).isEqualTo(entry.getValue());
    }
  }

  @Test void brapiParsesCurrentTickersResultsPayloadForSanb11BeforeQuoting() throws Exception {
    BrapiClient client=mock(BrapiClient.class);
    var identities = new ObjectMapper().readValue("""
        {"results":[{"symbol":"SANB11","name":"Banco Santander (Brasil) S.A.","assetType":"stock",
        "subType":"unit","exchange":"B3","currency":"BRL","logoUrl":"https://icons.brapi.dev/icons/SANB11.svg"}],
        "pagination":{"page":1,"limit":20,"totalItems":1,"totalPages":1,"hasNextPage":false}}
        """, BrapiTickersResponse.class);
    when(client.tickers("SANB11")).thenReturn(identities);
    when(client.quote(eq("SANB11"),any())).thenReturn(new BrapiResponse(List.of(new BrapiResult(
        "SANB11","SANB11",false,new BrapiQuoteData(new BigDecimal("27.41"),"BRL","2026-11-03T12:00:00Z")))));

    var metadata = new BrapiQuoteAdapter(client,properties()).validate("SANB11");

    assertThat(metadata.ticker()).isEqualTo("SANB11");
    assertThat(metadata.type()).isEqualTo(TipoAtivo.ACAO);
    assertThat(metadata.market()).isEqualTo(Mercado.B3);
  }

  @Test void brapiRejectsUnsupportedMissingAndDivergentIdentityWithoutGuessing() {
    BrapiClient client=mock(BrapiClient.class);
    var adapter=new BrapiQuoteAdapter(client,properties());
    when(client.tickers("BDRX11")).thenReturn(new BrapiTickersResponse(List.of(
      new BrapiTickerIdentity("BDRX11","Unsupported", "bdr", null))));
    assertThatThrownBy(()->adapter.validate("BDRX11")).isInstanceOf(IllegalArgumentException.class);
    when(client.tickers("FUND11")).thenReturn(new BrapiTickersResponse(List.of(
      new BrapiTickerIdentity("FUND11","Missing", null, null))));
    assertThatThrownBy(()->adapter.validate("FUND11")).isInstanceOf(IllegalArgumentException.class);
    when(client.tickers("ENPT3")).thenReturn(new BrapiTickersResponse(List.of(
      new BrapiTickerIdentity("ENPT3"," ", "stock", null))));
    assertThatThrownBy(()->adapter.validate("ENPT3")).isInstanceOf(com.carteira.carteiraInvestimento.application.service.AssetValidationUnavailableException.class);
    when(client.tickers("SANB11")).thenReturn(new BrapiTickersResponse(List.of(
      new BrapiTickerIdentity("SANB11","Santander Units", "unit", null))));
    when(client.quote(eq("SANB11"),any())).thenReturn(new BrapiResponse(List.of(new BrapiResult(
      "SANB11","HGLG11",false,new BrapiQuoteData(BigDecimal.TEN,"BRL","2026-10-30T20:07:00Z")))));
    assertThatThrownBy(()->adapter.validate("SANB11")).isInstanceOf(com.carteira.carteiraInvestimento.application.service.AssetValidationUnavailableException.class);
    when(client.quote(eq("SANB11"),any())).thenReturn(new BrapiResponse(List.of(new BrapiResult(
      "SANB11","SANB11",false,new BrapiQuoteData(BigDecimal.TEN,"USD","2026-10-30T20:07:00Z")))));
    assertThatThrownBy(()->adapter.validate("SANB11")).isInstanceOf(com.carteira.carteiraInvestimento.application.service.AssetValidationUnavailableException.class);
    when(client.quote(eq("SANB11"),any())).thenReturn(new BrapiResponse(List.of(new BrapiResult(
      "SANB11","SANB11",false,new BrapiQuoteData(null,"BRL","2026-10-30T20:07:00Z")))));
    assertThatThrownBy(()->adapter.validate("SANB11")).isInstanceOf(com.carteira.carteiraInvestimento.application.service.AssetValidationUnavailableException.class);
  }

  private <T> T fixture(String path,Class<T> type) throws Exception {
    try(InputStream input=getClass().getResourceAsStream(path)) {
      assertThat(input).isNotNull();
      return new ObjectMapper().readValue(input,type);
    }
  }
}
