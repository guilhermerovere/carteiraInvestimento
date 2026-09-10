package com.carteira.carteiraInvestimento.presentation.broker;
import com.carteira.carteiraInvestimento.application.port.CorretoraPage;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
public record BrokerListResponse(
        @ArraySchema(schema=@Schema(oneOf={BrokerSelectionResponse.class,BrokerAdminResponse.class})) List<?> items,
        int page,int size,long totalElements,int totalPages){
    static BrokerListResponse from(CorretoraPage p,boolean admin){return new BrokerListResponse(admin?p.items().stream().map(BrokerAdminResponse::from).toList():p.items().stream().map(BrokerSelectionResponse::from).toList(),p.page(),p.size(),p.totalElements(),p.totalPages());}
}
