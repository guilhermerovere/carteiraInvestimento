package com.carteira.carteiraInvestimento.presentation.broker;

import com.carteira.carteiraInvestimento.application.port.CorretoraReadScope;
import com.carteira.carteiraInvestimento.application.service.BrokerPatch;
import com.carteira.carteiraInvestimento.application.service.BrokerUseCase;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/corretoras") @Tag(name="Corretoras",description="Catálogo global; leitura USER/ADMIN e mutações ROLE_ADMIN.")
@SecurityRequirement(name="bearerAuth")
public class BrokerController {
    private static final Set<String> LIST_PARAMS=Set.of("page","size"); private final BrokerUseCase useCase;
    public BrokerController(BrokerUseCase useCase){this.useCase=useCase;}

    @PostMapping @PreAuthorize("hasRole('ADMIN')") @Operation(summary="Cadastra corretora",description="ROLE_ADMIN. CNPJ pode ser formatado ou canônico; dados oficiais vêm dos providers.")
    @ApiResponse(responseCode="201",description="Corretora criada") @BrokerErrors public ResponseEntity<BrokerAdminResponse> create(@Valid @RequestBody CreateBrokerRequest body,Authentication auth,HttpServletRequest request){
        var result=useCase.criar(body.cnpj(),body.numero(),body.complemento(),actor(auth),correlation(request));
        return ResponseEntity.status(201).body(BrokerAdminResponse.from(result));
    }
    @GetMapping @PreAuthorize("hasAnyRole('USER','ADMIN')") @Operation(summary="Lista corretoras",description="page=0, size=20; 1..100; ordem fixa razão social/id. USER vê somente ativas.")
    @ApiResponse(responseCode="200",description="Página de corretoras") @BrokerErrors public BrokerListResponse list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,Authentication auth,HttpServletRequest request){
        if(!LIST_PARAMS.containsAll(request.getParameterMap().keySet()))throw new IllegalArgumentException("unknown query parameter"); boolean admin=isAdmin(auth);
        if(page<0||size<1||size>100)throw new IllegalArgumentException("invalid pagination");
        return BrokerListResponse.from(useCase.listar(page,size,admin?CorretoraReadScope.ALL:CorretoraReadScope.ACTIVE_ONLY),admin);
    }
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('USER','ADMIN')") @Operation(summary="Consulta corretora por UUID",description="USER recebe 404 para corretora inativa.")
    @ApiResponse(responseCode="200",description="Corretora no schema da role",content=@Content(schema=@Schema(oneOf={BrokerSelectionResponse.class,BrokerAdminResponse.class}))) @BrokerErrors public Object get(@PathVariable UUID id,Authentication auth){boolean admin=isAdmin(auth);var b=useCase.consultar(id,admin?CorretoraReadScope.ALL:CorretoraReadScope.ACTIVE_ONLY);return admin?BrokerAdminResponse.from(b):BrokerSelectionResponse.from(b);}
    @GetMapping("/cnpj/{cnpj}") @PreAuthorize("hasAnyRole('USER','ADMIN')") @Operation(summary="Consulta corretora por CNPJ",description="Path aceita somente 14 dígitos canônicos com checksum válido; USER recebe 404 para inativa.")
    @ApiResponse(responseCode="200",description="Corretora no schema da role",content=@Content(schema=@Schema(oneOf={BrokerSelectionResponse.class,BrokerAdminResponse.class}))) @BrokerErrors public Object getByCnpj(@PathVariable String cnpj,Authentication auth){boolean admin=isAdmin(auth);var b=useCase.consultarPorCnpj(cnpj,admin?CorretoraReadScope.ALL:CorretoraReadScope.ACTIVE_ONLY);return admin?BrokerAdminResponse.from(b):BrokerSelectionResponse.from(b);}
    @PatchMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") @Operation(summary="Edita número/complemento",description="ROLE_ADMIN. Ausente mantém; null/blank remove; no-op não grava nem audita.")
    @ApiResponse(responseCode="200",description="Corretora administrativa atualizada ou no-op") @BrokerErrors public BrokerAdminResponse edit(@PathVariable UUID id,@RequestBody UpdateBrokerRequest body,Authentication auth,HttpServletRequest request){return BrokerAdminResponse.from(useCase.editar(id,new BrokerPatch(body.numeroPresent(),body.numero(),body.complementoPresent(),body.complemento()),actor(auth),correlation(request)));}
    @PatchMapping("/{id}/ativo") @PreAuthorize("hasRole('ADMIN')") @Operation(summary="Ativa ou desativa",description="ROLE_ADMIN. Operação local idempotente, sem providers; no-op não grava nem audita.")
    @ApiResponse(responseCode="200",description="Corretora administrativa atualizada ou no-op") @BrokerErrors public BrokerAdminResponse lifecycle(@PathVariable UUID id,@Valid @RequestBody UpdateBrokerLifecycleRequest body,Authentication auth,HttpServletRequest request){return BrokerAdminResponse.from(useCase.definirLifecycle(id,body.ativo(),actor(auth),correlation(request)));}
    private boolean isAdmin(Authentication auth){return auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));}
    private UUID actor(Authentication auth){try{return UUID.fromString(auth.getName());}catch(RuntimeException ignored){return null;}}
    private UUID correlation(HttpServletRequest request){Object value=request.getAttribute(CorrelationIdFilter.ATTRIBUTE);return value instanceof UUID id?id:UUID.randomUUID();}
}
