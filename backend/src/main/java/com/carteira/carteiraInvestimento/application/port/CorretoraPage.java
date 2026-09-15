package com.carteira.carteiraInvestimento.application.port;
import com.carteira.carteiraInvestimento.domain.broker.Corretora;
import java.util.List;
public record CorretoraPage(List<Corretora> items, int page, int size, long totalElements, int totalPages) { }
