package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.ManagerSaveException;
import exception.NotFoundException;
import exception.ParsingException;
import exception.ValidationException;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import model.Type;
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
                        Subtask subtask = getGson().fromJson(body, Subtask.class);
                        // если в теле запроса не найден id, то это создание подзадачи
                        if (checkBodyPOST_Request(body)) {    // + проверка при парсинге тела запроса ParsingException
                            subtask = (Subtask) checkJsonTask(subtask);
                            manager.createSubtask(subtask);
                            sendEmptyResponse(exchange, 201);
                            break;
                        }
                        subtask = (Subtask) checkJsonTask(subtask);
                        manager.updateSubtask(subtask);    // иначе обновляем подзадачу по найденному id
                        sendEmptyResponse(exchange, 200);
                    } catch (ValidationException e) {
                        // если пересекается время выполнения задач и т.п.
                        ErrorHandler.handle(exchange, e);
                    } catch (ManagerSaveException e) {
                        ErrorHandler.handle(exchange, e);
                    } catch (ParsingException e) {
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

    @Override
    protected Task checkJsonTask(Task task) {
        task.setTaskType(Type.SUBTASK);
        task.setEmptySubtasks();
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.NEW);
        }
        return task;
    }
}
