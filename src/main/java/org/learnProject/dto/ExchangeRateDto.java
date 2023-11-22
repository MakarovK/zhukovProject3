package org.learnProject.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

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
    private int baseCurrencyId;

    /**
     * Уникальный идентификатор второй валюты, которая является целевой в данном курсе обмена.
     */
    private int targetCurrencyId;

    /**
     * Курс обмена между базовой и целевой валютами.
     */
    private int rate;
}
