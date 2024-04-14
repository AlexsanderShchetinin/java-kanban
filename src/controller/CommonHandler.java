package controller;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import converter.DurationAdapter;
import converter.TimeAdapter;
import exception.ParsingException;
import model.Task;
import model.TaskStatus;
import model.Type;
import service.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

// Абстрактный класс Handler для HttpTaskServer
// Содержит в себе общие методы и переменные для унаследованных Handlers
// также определяет endpoints для дальнейшей маршрутизации и обработки запроса
public abstract class CommonHandler {

    protected static TaskManager manager;

    // отправка ответа на запрос (ответ, содержащий тело ответа)
    protected static void sendGetResponse(HttpExchange httpExchange, String jsonText, int code) throws IOException {
        byte[] response = jsonText.getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = httpExchange.getResponseBody()) {
            httpExchange.getResponseHeaders().add("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(code, response.length);
            os.write(response);
        }
    }

    // отправка пустого ответа на запрос (без тела ответа)
    protected static void sendEmptyResponse(HttpExchange httpExchange, int code) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(code, 0);
    }

    // создания объекта gson для парсинга из Json и обратно
    protected static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.serializeNulls();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new TimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        return gsonBuilder.create();
    }

    // получение id из URI
    protected static int getIdURI(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Integer.parseInt(pathParts[2]);
        } catch (NumberFormatException exception) {
            ErrorHandler.handle(exchange, exception);
        }
        return -1;
    }

    // получение id задачи из тела запроса (если id не задано или нет тела запроса возвращается -1
    protected int getTaskIdInBodyRequest(String body) {
        Optional<String> optBody = Optional.of(body);
        if (optBody.get().isEmpty()) return 0;
        return getGson().fromJson(optBody.get(), Task.class).getId();
    }

    // получение тела запроса в формате строки
    protected final String getBodyRequest(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isEmpty() || body.isBlank()) {
            throw new ParsingException("В POST запросе отсутствует тело запроса!");
        }
        return body;
    }

    protected void checkBodyPOST_Request(String jsonBody) throws IOException {
        JsonElement jsonElement = JsonParser.parseString(jsonBody);
        if (!jsonElement.isJsonObject()) {
            throw new ParsingException("В тело запроса передан не JSON объект!");
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        try {
            if (jsonObject.get("id").isJsonNull()) {
                throw new ParsingException("при POST запросе поле id должно быть заполнено как null");
            }
            jsonObject.get("duration").getAsInt();
            jsonObject.get("startTime").getAsString();
            String taskType = jsonObject.get("taskType").getAsString().toUpperCase();
            String status = jsonObject.get("status").getAsString().toUpperCase();
            try {
                Type.valueOf(taskType);
            } catch (IllegalArgumentException exception) {
                throw new ParsingException("в теле запроса передан неверный тип задачи в поле taskType: " + taskType);
            }
            try {
                TaskStatus.valueOf(status);
            } catch (IllegalArgumentException exception) {
                throw new ParsingException("в теле запроса передан неверный статус задачи в поле status: " + status);
            }
            if ((taskType.equals("EPIC") || taskType.equals("TASK")) && (!jsonObject.get("epicId").isJsonNull())) {
                if (jsonObject.get("epicId").getAsInt() != 0) {
                    throw new ParsingException("в теле запроса передан epicId. " +
                            "У задач и эпиков этот параметр должен быть пустой!");
                }
            }
            if (taskType.equals("SUBTASK") && jsonObject.get("epicId").isJsonNull()) {
                throw new ParsingException("У подзадач поле epicId не может быть пустым!");
            }
            if ((taskType.equals("SUBTASK") || taskType.equals("TASK")) && (!jsonObject.get("subtasks").isJsonNull())) {
                throw new ParsingException("У задач и подзадач поле subtasks должно быть пустым");
            }
        } catch (NullPointerException exception) {
            throw new ParsingException("в теле запроса как минимум одно поле имеет неверное название!");
        }
    }

    // определение эндпоинтов
    protected Endpoint getEndpoint(HttpExchange exchange) throws IOException {
        try {
            String mode = exchange.getRequestMethod();
            String[] paths = exchange.getRequestURI().getPath().split("/");

            switch (mode) {
                case "GET":
                    if (paths[1].equals("tasks") && paths.length == 2) {    // путь заканчивается на tasks
                        return Endpoint.GET_TASKS;    // получение всех задач
                    }
                    if (paths[1].equals("tasks") && paths.length == 3) {    // путь заканчивается id задачи
                        return Endpoint.GET_TASK_BY_ID;    // получение одной задачи
                    }
                    if (paths[1].equals("subtasks") && paths.length == 2) {    // путь заканчивается на subtasks
                        return Endpoint.GET_SUBTASKS;     // получение всех подзадач
                    }
                    if (paths[1].equals("subtasks") && paths.length == 3) {    // путь заканчивается id подзадачи
                        return Endpoint.GET_SUBTASK_BY_ID;    // получение одной подзадач
                    }
                    if (paths[1].equals("epics") && paths.length == 2) {    // путь заканчивается на epics
                        return Endpoint.GET_EPICS;    // получение всех эпиков
                    }
                    if (paths[1].equals("epics") && paths.length == 3) {    // путь заканчивается id эпика
                        return Endpoint.GET_EPIC_BY_ID;    // получение одного эпика
                    }
                    if (paths[1].equals("epics") && paths.length == 4 && paths[3].equals("subtasks")) {
                        return Endpoint.GET_EPIC_SUBTASKS;    // путь указывает на получение подзадач к определенному эпику
                    }
                    if (paths[1].equals("history") && paths.length == 2) {    // путь заканчивается на history
                        return Endpoint.GET_HISTORY;    // получение истории
                    }
                    if (paths[1].equals("prioritized") && paths.length == 2) {    // путь заканчивается на prioritized
                        return Endpoint.GET_PRIORITIZED_TASKS;    // получение задач по временному приоритету
                    }
                    return Endpoint.UNKNOWN;    // если запрос GET но ни один из вариантов не подошел
                case "POST":                // Для запросов типа POST дополнительного разграничения не требуется
                    return Endpoint.POST;   // определение пути при создании createContext в классе HttpTaskServer
                case "DELETE":
                    if (paths[1].equals("tasks") && paths.length == 3) {    // путь заканчивается id задачи
                        return Endpoint.DELETE_TASK;    // удаление задачи
                    }
                    if (paths[1].equals("subtasks") && paths.length == 3) {    // путь заканчивается id подзадачи
                        return Endpoint.DELETE_SUBTASK;    // удаление подзадачи
                    }
                    if (paths[1].equals("epics") && paths.length == 3) {    // путь заканчивается id эпика
                        return Endpoint.DELETE_EPIC;    // удаление эпика
                    }
                    return Endpoint.UNKNOWN;    // если запрос DELETE ни к одному из вариантов не подошел
                default:
                    return Endpoint.UNKNOWN;
            }
        } catch (Exception e) {
            ErrorHandler.handle(exchange, e);
        }
        return Endpoint.UNKNOWN;
    }

    // перечисление всех возможных эндпоинтов
    protected enum Endpoint {
        GET_TASKS,
        GET_TASK_BY_ID,
        POST,
        DELETE_TASK,
        GET_SUBTASKS,
        GET_SUBTASK_BY_ID,
        DELETE_SUBTASK,
        GET_EPICS,
        GET_EPIC_BY_ID,
        GET_EPIC_SUBTASKS,
        DELETE_EPIC,
        GET_HISTORY,
        GET_PRIORITIZED_TASKS,
        UNKNOWN
    }
}
