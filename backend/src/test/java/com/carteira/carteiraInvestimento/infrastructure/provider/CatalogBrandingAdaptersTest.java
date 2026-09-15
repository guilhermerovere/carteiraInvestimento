package com.carteira.carteiraInvestimento.infrastructure.provider;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.carteira.carteiraInvestimento.domain.asset.*;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;
import com.carteira.carteiraInvestimento.infrastructure.config.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
    when(client.quote(eq("IVVB11"),any())).thenReturn(new BrapiResponse(List.of(new BrapiResult(
      "IVVB11","IVVB11",false,new BrapiQuoteData("IVVB11",null,"etf","https://evil.test/logo.svg",BigDecimal.TEN,"BRL","2026-09-13T10:00:00Z")))));
    assertThat(new BrapiQuoteAdapter(client,properties()).validate("IVVB11").logoReference()).isNull();
    when(client.quote(eq("FUND11"),any())).thenReturn(new BrapiResponse(List.of(new BrapiResult(
      "FUND11","FUND11",false,new BrapiQuoteData("Fund",null,"mutual fund",null,BigDecimal.TEN,"BRL","2026-09-13T10:00:00Z")))));
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
  @Test void brapiUsesPublicAvailableDiscoveryWithoutCredentials() {
    BrapiClient client=mock(BrapiClient.class);
    when(client.available("PETR4")).thenReturn(new BrapiAvailableResponse(List.of("PETR4","PETR4F","BAD"),List.of()));
    assertThat(new BrapiQuoteAdapter(client,properties()).discover("PETR4")).containsExactly("PETR4","PETR4F");
  }
}
