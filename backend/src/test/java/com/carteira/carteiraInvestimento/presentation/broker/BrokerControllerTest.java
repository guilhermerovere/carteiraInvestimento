package com.carteira.carteiraInvestimento.presentation.broker;

import static org.assertj.core.api.Assertions.*; import static org.mockito.ArgumentMatchers.*; import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.carteira.carteiraInvestimento.application.port.*; import com.carteira.carteiraInvestimento.application.service.*; import com.carteira.carteiraInvestimento.domain.broker.Corretora;
import com.carteira.carteiraInvestimento.infrastructure.security.AccessDeniedAuditingService; import com.carteira.carteiraInvestimento.presentation.error.*;
import java.time.*; import java.util.*; import org.junit.jupiter.api.*; import org.mockito.*; import org.springframework.http.MediaType; import org.springframework.security.authentication.TestingAuthenticationToken; import org.springframework.test.web.servlet.*; import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class BrokerControllerTest {
    BrokerUseCase useCase; MockMvc mvc; TestingAuthenticationToken admin,user; Corretora broker;
    @BeforeEach void setup(){useCase=mock(BrokerUseCase.class);mvc=MockMvcBuilders.standaloneSetup(new BrokerController(useCase)).setControllerAdvice(new GlobalExceptionHandler(new ProblemDetailFactory(),new AccessDeniedAuditingService(e->{}))).build();admin=new TestingAuthenticationToken(UUID.randomUUID().toString(),"", "ROLE_ADMIN");user=new TestingAuthenticationToken(UUID.randomUUID().toString(),"", "ROLE_USER");broker=Corretora.nova("19131243000197","XP",null,"01310100","Paulista","Bela Vista","São Paulo","SP",null,null,Clock.fixed(Instant.parse("2026-09-10T10:00:00Z"),ZoneOffset.UTC));}
    @Test void adminAndUserReceiveExactDifferentSchemas() throws Exception {when(useCase.consultar(eq(broker.id()),any())).thenReturn(broker);
        mvc.perform(get("/api/v1/corretoras/{id}",broker.id()).principal(user)).andExpect(status().isOk()).andExpect(jsonPath("$.cnpj").value(broker.cnpj())).andExpect(jsonPath("$.cep").doesNotExist()).andExpect(jsonPath("$.criadoEm").doesNotExist());
        mvc.perform(get("/api/v1/corretoras/{id}",broker.id()).principal(admin)).andExpect(status().isOk()).andExpect(jsonPath("$.cep").value("01310100")).andExpect(jsonPath("$.criadoEm").exists());
    }
    @Test void listUsesFixedPaginationAndRoleScope() throws Exception {when(useCase.listar(anyInt(),anyInt(),any())).thenReturn(new CorretoraPage(List.of(broker),0,20,1,1));mvc.perform(get("/api/v1/corretoras").principal(user)).andExpect(status().isOk()).andExpect(jsonPath("$.items[0].cep").doesNotExist());ArgumentCaptor<CorretoraReadScope> scope=ArgumentCaptor.forClass(CorretoraReadScope.class);verify(useCase).listar(eq(0),eq(20),scope.capture());assertThat(scope.getValue()).isEqualTo(CorretoraReadScope.ACTIVE_ONLY);for(String q:List.of("page=-1","size=0","size=101","sort=cnpj","q=x"))mvc.perform(get("/api/v1/corretoras?"+q).principal(admin)).andExpect(status().isBadRequest());}
    @Test void strictRequestsRejectOfficialUnknownAndEmptyPatch() throws Exception {
        mvc.perform(post("/api/v1/corretoras").principal(admin).contentType(MediaType.APPLICATION_JSON).content("{\"cnpj\":\"19131243000197\",\"razaoSocial\":\"fake\"}")).andExpect(status().isBadRequest());
        mvc.perform(patch("/api/v1/corretoras/{id}",broker.id()).principal(admin).contentType(MediaType.APPLICATION_JSON).content("{\"cnpj\":\"19131243000197\"}")).andExpect(status().isBadRequest());
        when(useCase.editar(eq(broker.id()),any(),any(),any())).thenThrow(new IllegalArgumentException());mvc.perform(patch("/api/v1/corretoras/{id}",broker.id()).principal(admin).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(patch("/api/v1/corretoras/{id}/ativo",broker.id()).principal(admin).contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":true,\"extra\":1}")).andExpect(status().isBadRequest());
    }
    @Test void patchDistinguishesAbsentNullAndBlank() throws Exception {when(useCase.editar(eq(broker.id()),any(),any(),any())).thenReturn(broker);mvc.perform(patch("/api/v1/corretoras/{id}",broker.id()).principal(admin).contentType(MediaType.APPLICATION_JSON).content("{\"numero\":null,\"complemento\":\"  \"}")).andExpect(status().isOk());ArgumentCaptor<BrokerPatch> patch=ArgumentCaptor.forClass(BrokerPatch.class);verify(useCase).editar(eq(broker.id()),patch.capture(),any(),any());assertThat(patch.getValue()).isEqualTo(new BrokerPatch(true,null,true,"  "));}
    @Test void cnpjPathValidationMapsTo400AndErrorsStaySanitized() throws Exception {when(useCase.consultarPorCnpj(any(),any())).thenThrow(new IllegalArgumentException("secret"));mvc.perform(get("/api/v1/corretoras/cnpj/19.131.2430001-97").principal(user)).andExpect(status().isBadRequest()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret"))));}
}
