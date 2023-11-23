package org.learnProject.servlets;

import lombok.extern.slf4j.Slf4j;
import org.learnProject.service.ExchangeRateService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class ExchangeRateServlet extends HttpServlet {

    public ExchangeRateService exchangeRateService = new ExchangeRateService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("----------------------------------------------------------------------------------------");
        log.info("Произошло обращение к сервлету doGet по URI {}", request.getRequestURI());
        String pathInfo = request.getPathInfo();
        String fromCurrencyCode = request.getParameter("from");
        String toCurrencyCode = request.getParameter("to");
        String amountStr = request.getParameter("amount");
        try {
            Double amount = Double.parseDouble(amountStr);
            if (request.getRequestURI().equals("/exchange")) {
                log.info("Информация по запросу {}", pathInfo);
                log.info("From {}", fromCurrencyCode);
                log.info("To {}", toCurrencyCode);
                log.info("Amount {}", amount);
                exchangeRateService.currencyCalculation(fromCurrencyCode, toCurrencyCode, amount, response);
                log.info("Запрос обработан");
            } else if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                exchangeRateService.getAllExchangeRate(response);
                log.info("Информация по запросу {}", pathInfo);
                log.info("Запрос обработан");
            } else {
                String exchangeRateCodes = pathInfo.replace("/", "").toUpperCase();
                exchangeRateService.getExchangeRateByCode(response, exchangeRateCodes);
                log.info("Запрос обработан");
            }
        } catch (NumberFormatException e) {
            log.error("Ошибка при преобразовании параметра amount", e);
            response.setStatus(400);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("Неверное значение параметра amount");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("----------------------------------------------------------------------------------------");
        log.info("Произошло обращение к сервлету doPost по URI {}", request.getRequestURI());
        exchangeRateService.createExchange(request, response);
        log.info("Запрос обработан");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("----------------------------------------------------------------------------------------");
        log.info("Произошло обращение к сервлету doPost по URI {}", request.getRequestURI());
        String pathInfo = request.getPathInfo();
        String exchangeRateCodes = pathInfo.replace("/", "").toUpperCase();
        exchangeRateService.updateExchange(request, response, exchangeRateCodes);
        log.info("Запрос обработан");
    }
}
