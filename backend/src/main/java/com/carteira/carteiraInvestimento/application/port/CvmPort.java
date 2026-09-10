package com.carteira.carteiraInvestimento.application.port;
import java.util.Optional;
public interface CvmPort { Optional<RegistroCvm> consultar(String cnpj); }
