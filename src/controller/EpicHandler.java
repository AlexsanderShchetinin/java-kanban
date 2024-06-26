package controller;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.ManagerSaveException;
import exception.NotFoundException;
import exception.ParsingException;
import exception.ValidationException;
import model.Epic;
import model.Subtask;
import model.TaskStatus;
import model.Type;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends CommonHandler implements HttpHandler {


    public EpicHandler(TaskManager manager) {
        CommonHandler.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Endpoint endpoint = getEndpoint(exchange);
            switch (endpoint) {
                case GET_EPICS:    // получаем все эпики
                    sendGetResponse(exchange, getGson().toJson(manager.getEpicList()), 200);
                    break;
                case GET_EPIC_BY_ID:    // получаем эпик по id
                    try {
                        int id = getIdURI(exchange);
                        sendGetResponse(exchange, getGson().toJson(manager.getEpic(id)), 200);
                    } catch (NotFoundException e) {    // если обратились по id которого не существует
                        ErrorHandler.handle(exchange, e);
                    }
                    break;
                case POST:    // для эпиков реализовано только создание (обновление эпика только через подзадачи)
                    try {
                        String body = getBodyRequest(exchange);
                        checkBodyPOST_Request(body);    // проверка при парсинге тела запроса
                        Epic newEpic = getGson().fromJson(body, Epic.class);
                        newEpic.setTaskType(Type.EPIC);
                        newEpic.setEpicId(0);
                        newEpic.setEmptySubtasks();
                        if (newEpic.getStatus() == null) {
                            newEpic.setStatus(TaskStatus.NEW);
                        }
                        manager.createEpic(newEpic);
                        sendEmptyResponse(exchange, 201);
                    } catch (ValidationException e) {
                        ErrorHandler.handle(exchange, e);
                    } catch (ManagerSaveException e) {
                        ErrorHandler.handle(exchange, e);
                    } catch (ParsingException e) {
                        ErrorHandler.handle(exchange, e);
                    } catch (JsonSyntaxException e) {
                        ErrorHandler.handle(exchange, e);
                    }
                    break;
                case GET_EPIC_SUBTASKS:
                    try {
                        int id = getIdURI(exchange);
                        List<Subtask> listSubtasksFromEpic = manager.getListSubtasksFromEpic(id);
                        sendGetResponse(exchange, getGson().toJson(listSubtasksFromEpic), 200);
                    } catch (NotFoundException e) {    // если обратились по id которого не существует
                        ErrorHandler.handle(exchange, e);
                    }
                    break;
                case DELETE_EPIC:
                    int deleteId = getIdURI(exchange);
                    try {
                        manager.removeEpic(deleteId);
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
