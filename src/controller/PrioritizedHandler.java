package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.TaskManager;

import java.io.IOException;
import java.util.Objects;

public class PrioritizedHandler extends CommonHandler implements HttpHandler {
    public PrioritizedHandler(TaskManager manager) {
        CommonHandler.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Endpoint endpoint = getEndpoint(exchange);
            if (Objects.requireNonNull(endpoint) == Endpoint.GET_PRIORITIZED_TASKS) {
                sendGetResponse(exchange, getGson().toJson(manager.getPrioritizedTasks()), 200);
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
