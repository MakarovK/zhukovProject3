package org.learnProject.dao;

import lombok.extern.slf4j.Slf4j;
import org.learnProject.converter.CurrencyConverter;
import org.learnProject.databaseConnection.DatabaseConnection;
import org.learnProject.dto.CurrencyDto;
import org.learnProject.pojo.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CurrencyDao {

    private Connection connection;

    public CurrencyDao(Connection connection) {
        this.connection = connection;
        log.info("Установлено соединение с базой данных");
    }

    private Currency mapCurrency(ResultSet resultSet) {
        try {
            return new Currency().setId(resultSet.getInt("id"))
                    .setCode(resultSet.getString("code"))
                    .setFullName(resultSet.getString("fullName"))
                    .setSign(resultSet.getString("sign"));
        } catch (SQLException e) {
            log.error("Ошибка SQL запроса {}", e);
            throw new RuntimeException(e);
        }
    }

    public List<CurrencyDto> getAllCurrencies() {
        log.info("Заходим в метод получения всех валют в Data Access Object");
        try (Statement statement = connection.createStatement()) {
            String query = "select * from currencies";
            try (ResultSet resultSet = statement.executeQuery(query)) {
                log.info("Составлен запрос: {}", query);
                List<CurrencyDto> currencyDtoList = new ArrayList<>();
                while (resultSet.next()) {
                    log.info("Получение объектов из базы данных");
                    currencyDtoList.add(CurrencyConverter.entityToDto(mapCurrency(resultSet)));
                }
                log.info("Получен response для пользователя {}", currencyDtoList);
                return currencyDtoList;
            }
        } catch (SQLException e) {
            log.error("Ошибка SQL запроса {}", e);
            throw new RuntimeException(e);
        }
    }

    public CurrencyDto getCurrencyByCode(String currencyCode) {
        log.info("Вызван метод получения валюты по коду в Data Access Object");
        String query = "select * from currencies where code = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, currencyCode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    CurrencyDto currencyDtoResult = CurrencyConverter.entityToDto(mapCurrency(resultSet));
                    log.info("Получен response для пользователя {}", currencyDtoResult);
                    return currencyDtoResult;
                } else {
                    log.warn("Валюта с кодом {} не найдена", currencyCode);
                    return null;
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка SQL запроса", e);
            throw new RuntimeException("Ошибка SQL запроса", e);
        }
    }

    public boolean createCurrency(CurrencyDto currencyDto) {
        log.info("Вызван метод создания новой валюты");
        Currency currency = CurrencyConverter.dtoToEntity(currencyDto);
        String query = "insert into currencies(fullName, code, sign) values (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, currency.getFullName());
            preparedStatement.setString(2, currency.getCode());
            preparedStatement.setString(3, currency.getSign());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                log.warn("Такая сущность уже существует в базе данных");
                return false;
            } else {
                log.error("Ошибка SQL запроса", e);
                throw new RuntimeException(e);
            }
        }
    }
}
