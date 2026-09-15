package com.carteira.carteiraInvestimento.domain.asset;

import java.util.regex.Pattern;

public enum Mercado {
	B3(Moeda.BRL, Pattern.compile("^[A-Z]{4}[0-9]{1,2}F?$")),
	US(Moeda.USD, Pattern.compile("^[A-Z]{1,5}$"));

	private final Moeda moeda;
	private final Pattern tickerPattern;

	Mercado(Moeda moeda, Pattern tickerPattern) {
		this.moeda = moeda;
		this.tickerPattern = tickerPattern;
	}

	public Moeda moeda() {
		return moeda;
	}

	public boolean aceitaTicker(String ticker) {
		return tickerPattern.matcher(ticker).matches();
	}
}
