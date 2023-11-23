package org.learnProject.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@NoArgsConstructor
@Setter
@Getter
@Accessors(chain = true)
@ToString
public class ExchangeRateDto {
    /**
     * Уникальный идентификатор курса валют.
     */
    private int id;

    /**
     * Уникальный идентификатор первой валюты, которая является базовой в данном курсе обмена.
     */
    private CurrencyDto baseCurrency;

    /**
     * Уникальный идентификатор второй валюты, которая является целевой в данном курсе обмена.
     */
    private CurrencyDto targetCurrency;

    /**
     * Курс обмена между базовой и целевой валютами.
     */
    private Double rate;
}
