package com.carteira.carteiraInvestimento.application.port;
import java.util.Optional;
public interface ReceitaFederalPort { Optional<CadastroCnpj> consultar(String cnpj); }
