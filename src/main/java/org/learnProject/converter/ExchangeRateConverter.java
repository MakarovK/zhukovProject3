package org.learnProject.converter;

import org.learnProject.dto.ExchangeRateDto;
import org.learnProject.pojo.ExchangeRate;

public class ExchangeRateConverter {

    public static ExchangeRate dtoToEntity(ExchangeRateDto exchangeRateDto) {
        return new ExchangeRate().setId(exchangeRateDto.getId())
                .setBaseCurrency(CurrencyConverter.dtoToEntity(exchangeRateDto.getBaseCurrency()))
                .setTargetCurrency(CurrencyConverter.dtoToEntity(exchangeRateDto.getTargetCurrency()))
                .setRate(exchangeRateDto.getRate());
    }

    public static ExchangeRateDto entityToDto(ExchangeRate exchangeRate) {
        return new ExchangeRateDto().setId(exchangeRate.getId())
                .setBaseCurrency(CurrencyConverter.entityToDto(exchangeRate.getBaseCurrency()))
                .setTargetCurrency(CurrencyConverter.entityToDto(exchangeRate.getTargetCurrency()))
                .setRate(exchangeRate.getRate());
    }
}
