package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.ManagerSaveException;
import exception.NotFoundException;
import exception.ValidationException;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;

public class SubtaskHandler extends CommonHandler implements HttpHandler {


    public SubtaskHandler(TaskManager manager) {
        CommonHandler.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Endpoint endpoint = getEndpoint(exchange);
            switch (endpoint) {
                case GET_SUBTASKS:    // получаем все подзадачи
                    sendGetResponse(exchange, getGson().toJson(manager.getSubtaskList()), 200);
                    break;
                case GET_SUBTASK_BY_ID:    // получаем подзадачу по id
                    try {
                        int id = getIdURI(exchange);
                        sendGetResponse(exchange, getGson().toJson(manager.getSubtask(id)), 200);
                    } catch (NotFoundException e) {    // если обратились по id которого не существует
                        ErrorHandler.handle(exchange, e);
                    }
                    break;
                case POST:
                    try {
                        String body = getBodyRequest(exchange);
                        int TaskId = getTaskIdInBodyRequest(body);
                        Subtask subtask = getGson().fromJson(body, Subtask.class);
                        if (TaskId == 0) {    // если в теле запроса не найден id, то это создание подзадачи
                            manager.createSubtask(subtask);
                            sendEmptyResponse(exchange, 201);
                            break;
                        }
                        manager.updateSubtask(subtask);    // иначе обновляем подзадачу по найденному id
                        sendEmptyResponse(exchange, 200);
                    } catch (ValidationException e) {    // если пересекается время выполнения задач и т.п.
                        ErrorHandler.handle(exchange, e);
                    }
                    break;
                case DELETE_SUBTASK:
                    int deleteId = getIdURI(exchange);
                    try {
                        manager.removeSubtask(deleteId);
                        sendEmptyResponse(exchange, 200);
                    } catch (ManagerSaveException e) {    // ловим исключение когда по id не удалось найти задачу
                        ErrorHandler.handle(exchange, e);
                    }
                    break;
                default:
                    sendGetResponse(exchange, getGson().toJson("Страница по пути: " +
                            exchange.getRequestURI().toString() + " не найдена!"), 404);
            }    // при остальных исключениях проваливаемся в общий Exception класса ErrorHandler
        } catch (Exception e) {
            ErrorHandler.handle(exchange, e);
        } finally {
            exchange.close();
        }

    }
}
