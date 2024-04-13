package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import converter.CSVFormat;
import service.TaskManager;

import java.io.IOException;
import java.util.Objects;

public class HistoryHandler extends CommonHandler implements HttpHandler {
    public HistoryHandler(TaskManager manager) {
        CommonHandler.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Endpoint endpoint = getEndpoint(exchange);
            if (Objects.requireNonNull(endpoint) == Endpoint.GET_HISTORY) {
                // история в формате строки (так удобнее)
                sendGetResponse(exchange,
                        getGson().toJson(CSVFormat.historyToString(manager.getHistoryManager())), 200);
            } else {
                sendGetResponse(exchange, getGson().toJson("Страница по пути: " +
                        exchange.getRequestURI().toString() + " не найдена!"), 404);
            }
        } catch (Exception e) {
            ErrorHandler.handle(exchange, e);
        } finally {
            exchange.close();
        }
    }
}
