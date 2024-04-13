package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.ManagerSaveException;
import exception.NotFoundException;
import exception.ValidationException;
import model.Task;
import service.TaskManager;

import java.io.IOException;

public class TaskHandler extends CommonHandler implements HttpHandler {


    public TaskHandler(TaskManager manager) {
        CommonHandler.manager = manager;
    }


    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            Endpoint endpoint = getEndpoint(httpExchange);
            switch (endpoint) {
                case GET_TASKS:    // получаем все задачи
                    sendGetResponse(httpExchange, getGson().toJson(manager.getTasksList()), 200);
                    break;
                case GET_TASK_BY_ID:    // получаем задачу по id
                    try {
                        int id = getIdURI(httpExchange);
                        sendGetResponse(httpExchange, getGson().toJson(manager.getTask(id)), 200);
                    } catch (NotFoundException e) {    // если обратились по id которого не существует
                        ErrorHandler.handle(httpExchange, e);
                    }
                    break;
                case POST:
                    try {
                        String body = getBodyRequest(httpExchange);
                        int TaskId = getTaskIdInBodyRequest(body);
                        Task task = getGson().fromJson(body, Task.class);
                        if (TaskId == 0) {    // если в теле запроса не найден id, то это создание задачи
                            manager.createTask(task);
                            sendEmptyResponse(httpExchange, 201);
                            break;
                        }
                        manager.updateTask(task);    // иначе обновляем задачу по найденному id
                        sendEmptyResponse(httpExchange, 200);
                    } catch (ValidationException e) {    // если пересекается время выполнения задач и т.п.
                        ErrorHandler.handle(httpExchange, e);
                    }
                    break;
                case DELETE_TASK:
                    int deleteId = getIdURI(httpExchange);
                    try {
                        manager.removeTask(deleteId);
                        sendEmptyResponse(httpExchange, 200);
                    } catch (ManagerSaveException e) {    // ловим исключение когда по id не удалось найти задачу
                        ErrorHandler.handle(httpExchange, e);
                    }
                    break;
                default:
                    sendGetResponse(httpExchange, getGson().toJson("Страница по пути: " +
                            httpExchange.getRequestURI().toString() + " не найдена!"), 404);
            }    // при остальных исключениях проваливаемся в общий Exception класса ErrorHandler
        } catch (Exception e) {
            ErrorHandler.handle(httpExchange, e);
        } finally {
            httpExchange.close();
        }
    }
}
