package org.learnProject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.learnProject.converter.CurrencyConverter;
import org.learnProject.dao.CurrencyDao;
import org.learnProject.databaseConnection.DatabaseConnection;
import org.learnProject.dto.CurrencyDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CurrencyService {
    private CurrencyDao currencyDao;

    public CurrencyService() {
        this.currencyDao = new CurrencyDao(DatabaseConnection.getConnection());
    }

    public static String getJsonBody(HttpServletRequest request) throws IOException {
        StringBuilder jsonObject = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonObject.append(line);
            }
            log.info("Получен объект {}", jsonObject);
            return jsonObject.toString();
        }
    }

    public void getAllCurrencies(HttpServletResponse response) {
        log.info("Вызван метод получения всех валют");
        List<CurrencyDto> allCurrencies = currencyDao.getAllCurrencies();
        String allCurrenciesJson;
        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType("application/json; charset=UTF-8");
        try {
            allCurrenciesJson = objectMapper.writeValueAsString(allCurrencies);
            log.info("Сущность Currency успешно преобразована в json {}", allCurrenciesJson);
            if (!allCurrenciesJson.equals("[]")) {
                response.setStatus(200);
            } else {
                response.setStatus(404);
            }
            try {
                response.getWriter().write(allCurrenciesJson);
            } catch (IOException e) {
                log.error("Ошибка потока ввода/вывода при отправке данных в ответ", e);
                response.setStatus(500); // Internal Server Error
            }
        } catch (JsonProcessingException e) {
            log.error("Ошибка маппинга сущности Currency в json", e);
            throw new RuntimeException(e);
        }
    }


    private boolean isValid(String input) {
        String regex = "^[a-zA-Z]{3}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public void getCurrencyByCode(HttpServletResponse response, String currencyCode) throws IOException {
        log.info("Вызван метод получения валюты по коду");
        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType("application/json; charset=UTF-8");
        if (!isValid(currencyCode)) {
            log.warn("Введен неверный код валюты {}", currencyCode);
            response.setStatus(400);
            response.getWriter().write("Введен неверный код валюты " + currencyCode);
            return;
        }
        CurrencyDto currencyDto = currencyDao.getCurrencyByCode(currencyCode);
        log.info("Полученные данные {}", currencyDto);
        response.setContentType("application/json; charset=UTF-8");
        if (currencyDto != null) {
            response.setStatus(200);
            try {
                response.getWriter().write(objectMapper.writeValueAsString(currencyDto));
            } catch (JsonProcessingException e) {
                log.error("Ошибка при преобразовании данных в JSON", e);
                response.setStatus(500); // Internal Server Error
                response.getWriter().write("Ошибка при преобразовании данных в JSON");
            } catch (IOException e) {
                log.error("Ошибка при отправке данных в ответ", e);
                response.setStatus(500); // Internal Server Error
                response.getWriter().write("Ошибка при отправке данных в ответ");
            }
        } else {
            log.warn("Не найдена валюта по коду в базе данных {}", currencyCode);
            response.setStatus(404);
            response.getWriter().write("Не найдена валюта по коду в базе данных: " + currencyCode);
        }
    }


    public void createCurrency(HttpServletRequest request, HttpServletResponse response) {
        log.info("Вызван метод создания новой валюты");
        ObjectMapper objectMapper = new ObjectMapper();
        CurrencyDto currencyDto = null;
        response.setContentType("application/json; charset=UTF-8");
        try {
            currencyDto = objectMapper.readValue(getJsonBody(request), CurrencyDto.class);
            boolean isCurrency = currencyDao.createCurrency(currencyDto);
            if ((currencyDto.getCode() == null) || (currencyDto.getSign() == null) || (currencyDto.getFullName() == null)) {
                log.warn("Отсутствует поле нужной формы в полученном теле запроса");
                response.setStatus(400);
                response.getWriter().write("Отсутствует поле нужной формы в полученном теле запроса");
            } else if (!isValid(currencyDto.getCode())) {
                log.warn("Поле код у переданной сущности некорректно + {}", currencyDto.getCode());
                response.setStatus(400);
                response.getWriter().write("Поле код у переданной сущности некорректно");
            } else if (!isCurrency) {
                log.warn("Сущность уже существует в базе данных");
                response.setStatus(409);
                response.getWriter().write("Сущность уже существует в базе данных");
            } else {
                log.info("Сущность {} сохранена ", CurrencyConverter.dtoToEntity(currencyDto));
                response.setStatus(200);
                response.getWriter().write("Сущность сохранена успешно");
            }
        } catch (IOException e) {
            log.error("Ошибка потока ввода вывода", e);
            throw new RuntimeException(e);
        }
    }
}
