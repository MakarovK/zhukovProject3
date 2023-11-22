package org.learnProject.converter;

import lombok.extern.slf4j.Slf4j;
import org.learnProject.dto.CurrencyDto;
import org.learnProject.pojo.Currency;
@Slf4j
public class CurrencyConverter {

    public static Currency dtoToEntity(CurrencyDto currencyDto) {
        return new Currency().setId(currencyDto.getId())
                .setCode(currencyDto.getCode())
                .setFullName(currencyDto.getFullName())
                .setSign(currencyDto.getSign());
    }

    public static CurrencyDto entityToDto(Currency currency) {
        return new CurrencyDto().setId(currency.getId())
                .setCode(currency.getCode())
                .setFullName(currency.getFullName())
                .setSign(currency.getSign());
    }
}
