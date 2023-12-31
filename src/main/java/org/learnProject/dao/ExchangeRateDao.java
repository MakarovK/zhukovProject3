package org.learnProject.dao;

import lombok.extern.slf4j.Slf4j;
import org.learnProject.converter.CurrencyConverter;
import org.learnProject.converter.ExchangeRateConverter;
import org.learnProject.dto.CurrencyDto;
import org.learnProject.dto.ExchangeRateDto;
import org.learnProject.pojo.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExchangeRateDao {

    private Connection connection;

    private CurrencyDao currencyDao;

    public ExchangeRateDao(Connection connection) {
        this.connection = connection;
        this.currencyDao = new CurrencyDao(connection);
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

    private Currency getCurrencyById(int currencyId) {
        String query = "select * from currencies where id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, currencyId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Currency currency = mapCurrency(resultSet);
                    return currency;
                } else {
                    log.warn("Валюта с кодом {} не найдена", currencyId);
                    return null;
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка SQL запроса {}", e);
            throw new RuntimeException(e);
        }
    }

    private Currency getCurrencyByField(Currency currency) {
        String query = "select * from currencies where id = ? or fullname = ? or code = ? or sign = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, currency.getId());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getCode());
            preparedStatement.setString(4, currency.getSign());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapCurrency(resultSet);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка SQL запроса {}", e);
            throw new RuntimeException(e);
        }
    }

    private ExchangeRate mapExchangeRate(ResultSet resultSet) {
        try {
            return new ExchangeRate().setId(resultSet.getInt("id"))
                    .setBaseCurrency(getCurrencyById(resultSet.getInt("basecurrencyid")))
                    .setTargetCurrency(getCurrencyById(resultSet.getInt("targetcurrencyid")))
                    .setRate(resultSet.getBigDecimal("rate").doubleValue());
        } catch (SQLException e) {
            log.error("Ошибка SQL запроса {}", e);
            throw new RuntimeException(e);
        }
    }

    public List<ExchangeRateDto> getAllExchangeRate() {
        log.info("Заходим в метод получения всех обменных курсов в Data Access Object");
        try (Statement statement = connection.createStatement()) {
            String query = "select * from exchangerates";
            try (ResultSet resultSet = statement.executeQuery(query)) {
                log.info("Составлен запрос {}", query);
                List<ExchangeRateDto> exchangeRateDtoList = new ArrayList<>();
                while (resultSet.next()) {
                    log.info("Получение объектов из базы данных");
                    exchangeRateDtoList.add(ExchangeRateConverter.entityToDto(mapExchangeRate(resultSet)));
                }
                log.info("Получен response для пользователя {}", exchangeRateDtoList);
                return exchangeRateDtoList;
            }
        } catch (SQLException e) {
            log.error("Ошибка SQL запроса {}", e);
            throw new RuntimeException(e);
        }
    }


    public ExchangeRateDto getExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) {
        log.info("Заходим в метод получения обменного курса по кодам в Data Access Object");
        String query = "select * from exchangerates where basecurrencyid = ? and targetcurrencyid = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            Currency baseCurrency = currencyDao.getCurrencyByCode(baseCurrencyCode) != null ? CurrencyConverter.dtoToEntity(currencyDao.getCurrencyByCode(baseCurrencyCode)) : null;
            Currency targetCurrency = currencyDao.getCurrencyByCode(targetCurrencyCode) != null ? CurrencyConverter.dtoToEntity(currencyDao.getCurrencyByCode(targetCurrencyCode)) : null;
            if (baseCurrency == null || targetCurrency == null) {
                return null;
            }
            preparedStatement.setInt(1, baseCurrency.getId());
            preparedStatement.setInt(2, targetCurrency.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    ExchangeRateDto exchangeRateDto = ExchangeRateConverter.entityToDto(mapExchangeRate(resultSet));
                    log.info("Получен response для пользователя {}", exchangeRateDto);
                    return exchangeRateDto;
                } else {
                    log.warn("Курс валют {}, {} не найден", baseCurrencyCode, targetCurrencyCode);
                    return null;
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка SQL запроса {}", e);
            throw new RuntimeException(e);
        }
    }


    private boolean isExchangeRateInDatabase(ExchangeRate exchangeRate) {
        String query = "select * from exchangerates where basecurrencyid = ? and targetcurrencyid = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, exchangeRate.getBaseCurrency().getId());
            preparedStatement.setInt(2, exchangeRate.getTargetCurrency().getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            log.error("Ошибка SQL запроса", e);
            throw new RuntimeException(e);
        }
    }

    public boolean createExchange(ExchangeRateDto exchangeRateDto) {
        log.info("Вызван метод создания нового курса валют");
        ExchangeRate exchangeRate = new ExchangeRate();
        Currency baseCurrency = getCurrencyByField(CurrencyConverter.dtoToEntity(exchangeRateDto.getBaseCurrency()));
        Currency targetCurrency = getCurrencyByField(CurrencyConverter.dtoToEntity(exchangeRateDto.getTargetCurrency()));
        Double rate = exchangeRateDto.getRate();
        exchangeRate.setBaseCurrency(baseCurrency)
                .setTargetCurrency(targetCurrency)
                .setRate(rate);
        String query = "insert into exchangerates(baseCurrencyId, targetCurrencyId, rate) values (?, ?, ?)";
        if (isExchangeRateInDatabase(exchangeRate)) {
            log.info("Курсы валют с кодами {}, {} уже существует в бд", exchangeRate.getBaseCurrency(), exchangeRate.getTargetCurrency());
            return false;
        } else {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, exchangeRate.getBaseCurrency().getId());
                preparedStatement.setInt(2, exchangeRate.getTargetCurrency().getId());
                preparedStatement.setBigDecimal(3, BigDecimal.valueOf(exchangeRate.getRate()));
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException e) {
                log.error("Ошибка SQL запроса", e);
                throw new RuntimeException(e);
            }
        }
    }

    public boolean updateExchange(String baseCurrencyCode, String targetCurrencyCode, Double rate) {
        log.info("Вызван метод изменения курса валют");
        try {
            ExchangeRate exchangeRate = ExchangeRateConverter.dtoToEntity(getExchangeRateByCode(baseCurrencyCode, targetCurrencyCode));
            boolean isExchangeRate = isExchangeRateInDatabase(exchangeRate);
            if (!isExchangeRate) {
                return false;
            } else {
                String query = "update exchangerates set rate = ? where basecurrencyid = ? and targetcurrencyid = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setBigDecimal(1, BigDecimal.valueOf(rate));
                    preparedStatement.setInt(2, exchangeRate.getBaseCurrency().getId());
                    preparedStatement.setInt(3, exchangeRate.getTargetCurrency().getId());
                    preparedStatement.executeUpdate();
                    return true;
                } catch (SQLException e) {
                    log.error("Ошибка SQL запроса", e);
                    throw new RuntimeException(e);
                }
            }
        } catch (NullPointerException e) {
            log.info("Сущность в БД не найдена, вылетел NullPointer");
            return false;
        }
    }

    public ExchangeRateDto currencyCalculation(String baseCurrencyCode, String targetCurrencyCode) {
        log.info("Вызван метод расчёта относительно курса валют");
        try {
            ExchangeRate exchangeRate = ExchangeRateConverter.dtoToEntity(getExchangeRateByCode(baseCurrencyCode, targetCurrencyCode));
            return ExchangeRateConverter.entityToDto(exchangeRate);
        } catch (NullPointerException e) {
            try {
                ExchangeRate exchangeRate = ExchangeRateConverter.dtoToEntity(getExchangeRateByCode(targetCurrencyCode, baseCurrencyCode));
                ExchangeRate exchangeRateResult = new ExchangeRate();
                exchangeRateResult.setId(0)
                        .setBaseCurrency(exchangeRate.getTargetCurrency())
                        .setTargetCurrency(exchangeRate.getBaseCurrency())
                        .setRate(Math.pow(exchangeRate.getRate(), -1));
                return ExchangeRateConverter.entityToDto(exchangeRateResult);
            } catch (NullPointerException ex) {
                try {
                    ExchangeRate exchangeRate1 = ExchangeRateConverter.dtoToEntity(getExchangeRateByCode("USD", baseCurrencyCode));
                    ExchangeRate exchangeRate2 = ExchangeRateConverter.dtoToEntity(getExchangeRateByCode("USD", targetCurrencyCode));
                    ExchangeRate exchangeRateResult = new ExchangeRate();
                    exchangeRateResult.setId(0)
                            .setBaseCurrency(exchangeRate1.getTargetCurrency())
                            .setTargetCurrency(exchangeRate2.getTargetCurrency())
                            .setRate(exchangeRate2.getRate() / exchangeRate1.getRate());
                    return ExchangeRateConverter.entityToDto(exchangeRateResult);
                } catch (NullPointerException exc) {
                    log.error("Невозможно найти подходящий курс валюты");
                    return null;
                }
            }
        }
    }
}
