package com.carteira.carteiraInvestimento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CarteiraInvestimentoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarteiraInvestimentoApplication.class, args);
	}

}
