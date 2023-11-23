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
        try {
            allCurrenciesJson = objectMapper.writeValueAsString(allCurrencies);
            log.info("Сущность Currency успешно преобразована в json {}", allCurrenciesJson);
        } catch (JsonProcessingException e) {
            log.error("Ошибка маппинга сущности Currency в json {}", e);
            throw new RuntimeException(e);
        }
        response.setContentType("application/json; charset=UTF-8");
        try {
            response.getWriter().write(allCurrenciesJson);
            if (!allCurrenciesJson.equals("[]")) {
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
        String regex = "^[a-zA-Z]{3}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public void getCurrencyByCode(HttpServletResponse response, String currencyCode) {
        log.info("Вызван метод получения валюты по коду");
        String currencyByCode;
        ObjectMapper objectMapper = new ObjectMapper();
        if (!isValid(currencyCode)) {
            log.warn("Введен неверный код валюты {}", currencyCode);
            response.setStatus(400);
            return;
        }
        CurrencyDto currencyDto = currencyDao.getCurrencyByCode(currencyCode);
        log.info("Полученные данные {}", currencyDto);
        if (currencyDto != null) {
            response.setStatus(200);
            try {
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(currencyDto));
            } catch (IOException e) {
                log.error("Ошибка при отправке данных в ответ", e);
                response.setStatus(500);
            }
        } else if (currencyDto == null) {
            log.warn("Не найдена валюта по коду в базе данных {}", currencyCode);
            response.setStatus(404);
        }
    }

    public void createCurrency(HttpServletRequest request, HttpServletResponse response) {
        log.info("Вызван метод создания новой валюты");
        ObjectMapper objectMapper = new ObjectMapper();
        CurrencyDto currencyDto = null;
        try {
            currencyDto = objectMapper.readValue(getJsonBody(request), CurrencyDto.class);
            boolean isCurrency = currencyDao.createCurrency(currencyDto);
            if ((currencyDto.getCode() == null) || (currencyDto.getSign() == null) || (currencyDto.getFullName() == null)) {
                log.warn("Отсутствует поле нужной формы в полученном теле запроса");
                response.setStatus(400);
            } else if (!isCurrency) {
                log.warn("Сущность уже существует в базе данных");
                response.setStatus(409);
            } else {
                log.info("Сущность {} сохранена ", CurrencyConverter.dtoToEntity(currencyDto));
                response.setStatus(200);
            }
        } catch (IOException e) {
            log.error("Ошибка потока ввода вывода", e);
            throw new RuntimeException(e);
        }
    }
}
