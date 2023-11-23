package org.learnProject.servlets;

import lombok.extern.slf4j.Slf4j;
import org.learnProject.service.CurrencyService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class CurrencyServlet extends HttpServlet {

    private CurrencyService currencyService = new CurrencyService();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("----------------------------------------------------------------------------------------");
        log.info("Произошло обращение к сервлету doGet по URI {}", request.getRequestURI());
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            currencyService.getAllCurrencies(response);
            log.info("Информация по запросу {}", pathInfo);
            log.info("Запрос обработан");
        } else {
            String currencyCode = pathInfo.replace("/", "").toUpperCase();
            currencyService.getCurrencyByCode(response, currencyCode);
            log.info("Информация по запросу {}", pathInfo);
            log.info("Запрос обработан");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("----------------------------------------------------------------------------------------");
        log.info("Произошло обращение к сервлету doPost по URI {}", request.getRequestURI());
        currencyService.createCurrency(request, response);
        log.info("Запрос обработан");
    }
}
