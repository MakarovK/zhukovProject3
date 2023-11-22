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
public class CurrencyDto {
    /**
     * Уникальный идентификатор валюты.
     */
    private int id;

    /**
     * Код валюты, представленный в виде строкового значения.
     */
    private String code;

    /**
     * Полное имя валюты.
     */
    private String fullName;

    /**
     * Символ, обозначающий валюту.
     */
    private String sign;

}
