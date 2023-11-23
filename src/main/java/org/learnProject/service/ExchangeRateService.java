package org.learnProject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.learnProject.converter.CurrencyConverter;
import org.learnProject.converter.ExchangeRateConverter;
import org.learnProject.dao.ExchangeRateDao;
import org.learnProject.databaseConnection.DatabaseConnection;
import org.learnProject.dto.ExchangeRateDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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
            response.getWriter().write(allExchangeRatesJson);
            if (!allExchangeRatesJson.equals("[]")) {
                response.setStatus(200);
            } else {
                response.setStatus(404);
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
            } catch (IOException e) {
                log.error("Ошибка при отправке данных в ответ", e);
                throw new RuntimeException(e);
            }
        } else if (exchangeRateDto == null) {
            log.warn("Не найден курс валют по общему коду в базе данных {}", exchangeCodes);
            response.setStatus(404);
        }
    }

    public void createExchange(HttpServletRequest request, HttpServletResponse response) {
        log.info("Вызван метод создания нового курса валют");
        ObjectMapper objectMapper = new ObjectMapper();
        ExchangeRateDto exchangeRateDto = null;
        try {
            exchangeRateDto = objectMapper.readValue(CurrencyService.getJsonBody(request), ExchangeRateDto.class);
            boolean isExchangeRate = exchangeRateDao.createExchange(exchangeRateDto);
            if ((exchangeRateDto.getBaseCurrency() == null) || (exchangeRateDto.getTargetCurrency() == null) || (exchangeRateDto.getRate() == null)) {
                log.warn("Отсутствует поле нужной формы в полученном теле запроса");
                response.setStatus(400);
            } else if (!isExchangeRate) {
                log.warn("Сущность уже существует в базе данных");
                response.setStatus(409);
            } else {
                log.info("Сущность {} сохранена ", exchangeRateDto);
                response.setStatus(200);
            }
        } catch (IOException e) {
            log.error("Ошибка потока ввода вывода", e);
            throw new RuntimeException(e);
        }
    }
}

