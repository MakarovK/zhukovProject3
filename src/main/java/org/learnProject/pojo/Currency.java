package org.learnProject.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Класс представляет валюту с уникальным идентификатором, кодом, полным именем и символом.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
public class Currency {
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
