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
        if(pathInfo == null || pathInfo.equals("/")) {
            exchangeRateService.getAllExchangeRate(response);
            log.info("Информация по запросу {}", pathInfo);
            log.info("Запрос обработан");
        } else {
            String exchangeRateCodes = pathInfo.replace("/", "").toUpperCase();
            exchangeRateService.getExchangeRateByCode(response, exchangeRateCodes);
            log.info("Запрос обработан");
        }
    }
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("----------------------------------------------------------------------------------------");
        log.info("Произошло обращение к сервлету doPost по URI {}", request.getRequestURI());
        //exchangeRateService.createExchange(request, response);
        log.info("Запрос обработан");
    }
}
