package org.learnProject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.learnProject.converter.CurrencyConverter;
import org.learnProject.converter.ExchangeRateConverter;
import org.learnProject.dao.ExchangeRateDao;
import org.learnProject.databaseConnection.DatabaseConnection;
import org.learnProject.dto.ExchangeRateDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ExchangeRateService {
    private ExchangeRateDao exchangeRateDao;


    public ExchangeRateService() {
        this.exchangeRateDao = new ExchangeRateDao(DatabaseConnection.getConnection());
    }

    public void getAllExchangeRate(HttpServletResponse response) {
        log.info("Вызван метод получения всех ExchangeRate");
        List<ExchangeRateDto> allExchangeRates = exchangeRateDao.getAllExchangeRate();
        String allExchangeRatesJson;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            allExchangeRatesJson = objectMapper.writeValueAsString(allExchangeRates);
            log.info("Сущность ExchangeRate успешно преобразована в json {}", allExchangeRatesJson);
        } catch (JsonProcessingException e) {
            log.error("Ошибка маппинга сущности ExchangeRate в json {}", e);
            throw new RuntimeException(e);
        }
        response.setContentType("application/json; charset=UTF-8");
        try {
            if (!allExchangeRates.isEmpty()) {
                response.setStatus(200);
                response.getWriter().write(allExchangeRatesJson);
            } else {
                response.setStatus(404);
                response.getWriter().write("Список ExchangeRate пуст");
            }
        } catch (IOException e) {
            log.error("Ошибка потока ввода/вывода {}", e);
            throw new RuntimeException(e);
        }
    }


    private boolean isValid(String input) {
        String regex = "^[a-zA-Z]{6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public void getExchangeRateByCode(HttpServletResponse response, String exchangeCodes) {
        log.info("Вызван метод получения сущности ExchangeRate по кодам");
        ObjectMapper objectMapper = new ObjectMapper();
        if (!isValid(exchangeCodes)) {
            log.warn("Введен неверный код валют {}", exchangeCodes);
            response.setStatus(400);
            return;
        }
        log.info("Происходит поиск кодов {}, {}", exchangeCodes.substring(0, 3), exchangeCodes.substring(3));
        ExchangeRateDto exchangeRateDto = exchangeRateDao.getExchangeRateByCode(exchangeCodes.substring(0, 3), exchangeCodes.substring(3));
        if (exchangeRateDto != null) {
            log.info("Полученные данные {}", exchangeRateDto);
            response.setStatus(200);
            response.setContentType("application/json; charset=UTF-8");

            try {
                response.getWriter().write(objectMapper.writeValueAsString(exchangeRateDto));
            } catch (JsonProcessingException e) {
                log.error("Ошибка при преобразовании данных в JSON", e);
                response.setStatus(500);
            } catch (IOException e) {
                log.error("Ошибка при отправке данных в ответ", e);
                response.setStatus(500);
            }
        } else {
            log.warn("Не найден курс валют по общему коду в базе данных {}", exchangeCodes);
            response.setStatus(404);
        }
    }


    public void createExchange(HttpServletRequest request, HttpServletResponse response) {
        log.info("Вызван метод создания нового курса валют");
        ObjectMapper objectMapper = new ObjectMapper();
        ExchangeRateDto exchangeRateDto = null;
        response.setContentType("text/json; charset=UTF-8");
        try {
            exchangeRateDto = objectMapper.readValue(CurrencyService.getJsonBody(request), ExchangeRateDto.class);

            if ((exchangeRateDto.getBaseCurrency() == null) || (exchangeRateDto.getTargetCurrency() == null) || (exchangeRateDto.getRate() == null) || exchangeRateDto.getRate() < 0.0) {
                log.warn("Введены некорректные данные");
                response.setStatus(400);
                response.getWriter().write("Введены некорректные данные");
            } else {
                boolean isExchangeRate = exchangeRateDao.createExchange(exchangeRateDto);
                if (!isExchangeRate) {
                    log.warn("Сущность уже существует в базе данных");
                    response.setStatus(409);
                    response.getWriter().write("Сущность уже существует в базе данных");
                } else {
                    log.info("Сущность {} сохранена ", exchangeRateDto);
                    response.setStatus(200);
                    response.getWriter().write("Сущность сохранена успешно");
                }
            }
        } catch (IOException e) {
            log.error("Ошибка дессериализации данных");
            response.setStatus(400);
            try {
                response.getWriter().write("Введены некорректные данные");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void updateExchange(HttpServletRequest request, HttpServletResponse response, String exchangeCodes) {
        log.info("Вызван метод изменения курса валют");
        response.setContentType("text/json; charset=UTF-8");
        if (!isValid(exchangeCodes)) {
            log.warn("Введен неверный код валют {}", exchangeCodes);
            response.setStatus(400);
            try {
                response.getWriter().write("Введен неверный код валют " + exchangeCodes);
            } catch (IOException e) {
                log.error("Ошибка потока ввода вывода", e);
                throw new RuntimeException(e);
            }
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        ExchangeRateDto exchangeRateDto = null;
        response.setContentType("application/json; charset=UTF-8");
        log.info("Происходит поиск кодов {}, {}", exchangeCodes.substring(0, 3), exchangeCodes.substring(3));
        try {
            exchangeRateDto = objectMapper.readValue(CurrencyService.getJsonBody(request), ExchangeRateDto.class);
            if (exchangeRateDto.getRate() == null || exchangeRateDto.getRate() < 0.0) {
                log.warn("Введены некорректные данные");
                response.setStatus(400);
                response.getWriter().write("Введены некорректные данные");
            } else {
                boolean isExchangeRate = exchangeRateDao.updateExchange(exchangeCodes.substring(0, 3), exchangeCodes.substring(3), exchangeRateDto.getRate());
                if (!isExchangeRate) {
                    log.warn("Сущность в базе данных не найдена");
                    response.setStatus(404);
                    response.getWriter().write("Сущность в базе данных не найдена");
                } else {
                    log.info("Изменение прошло успешно");
                    response.setStatus(200);
                    response.getWriter().write("Изменение сущности сохранено успешно");
                }
            }
        } catch (IOException e) {
            log.error("Ошибка дессериализации данных");
            log.warn("Введены некорректные данные");
            response.setStatus(400);
            try {
                response.getWriter().write("Введены некорректные данные");
            } catch (IOException ex) {
                log.error("Хз как мы тут вообще оказались");
                throw new RuntimeException(ex);
            }
        }
    }

    public void currencyCalculation(String fromCurrencyCode, String toCurrencyCode, Double amount, HttpServletResponse response) {
        log.info("Вызван метод расчёта валюты");
        response.setContentType("application/json; charset=UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJson = "";
        try {
            ExchangeRateDto exchangeRateDto = exchangeRateDao.currencyCalculation(fromCurrencyCode, toCurrencyCode);
            ObjectNode additionalFields = objectMapper.createObjectNode();
            additionalFields.put("amount", amount);
            additionalFields.put("convertedAmount", amount * exchangeRateDto.getRate());
            ObjectNode rootNode = objectMapper.valueToTree(exchangeRateDto);
            rootNode.set("Расчёты", additionalFields);
            responseJson = objectMapper.writeValueAsString(rootNode);
            response.setStatus(200);
            response.getWriter().write(responseJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException exc) {
            log.error("Ошибка дессериализации данных");
            log.warn("Введены некорректные данные");
            response.setStatus(404);
            try {
                response.getWriter().write("Не возможно расчитать данный курс валют");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}