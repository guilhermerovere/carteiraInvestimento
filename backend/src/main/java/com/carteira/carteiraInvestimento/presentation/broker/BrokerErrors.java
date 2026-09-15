package com.carteira.carteiraInvestimento.presentation.broker;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.ProblemDetail;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
    @ApiResponse(responseCode="400",description="Entrada inválida",content=@Content(schema=@Schema(implementation=ProblemDetail.class))),
    @ApiResponse(responseCode="401",description="Não autenticado",content=@Content(schema=@Schema(implementation=ProblemDetail.class))),
    @ApiResponse(responseCode="403",description="Sem permissão",content=@Content(schema=@Schema(implementation=ProblemDetail.class))),
    @ApiResponse(responseCode="404",description="Corretora inexistente ou invisível",content=@Content(schema=@Schema(implementation=ProblemDetail.class))),
    @ApiResponse(responseCode="409",description="CNPJ duplicado",content=@Content(schema=@Schema(implementation=ProblemDetail.class))),
    @ApiResponse(responseCode="422",description="Reprovação regulatória conhecida",content=@Content(schema=@Schema(implementation=ProblemDetail.class))),
    @ApiResponse(responseCode="502",description="Falha técnica upstream",content=@Content(schema=@Schema(implementation=ProblemDetail.class))),
    @ApiResponse(responseCode="500",description="Falha interna ou de auditoria obrigatória",content=@Content(schema=@Schema(implementation=ProblemDetail.class)))
})
public @interface BrokerErrors { }
