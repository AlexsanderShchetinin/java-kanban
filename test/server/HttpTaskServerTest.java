package server;

import com.google.gson.*;
import converter.CSVFormat;
import converter.DurationAdapter;
import converter.TimeAdapter;
import exception.NotFoundException;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Http сервер трекера задач")
class HttpTaskServerTest {

    static HttpTaskServer httpTaskServer;
    static TaskManager manager;
    HttpClient client;

    // Вспомагательные методы для загрузки данных
    // testInitialization должен создавать TaskManager с 32 сущностями:
    // 12 задачами, 6 из которых приоритетные
    // 17 подзадачами, 12 из которых приоритетные
    // 3 эпиками, 1 из которых без подзадач
    TaskManager testInitialization(TaskManager manager) {
        final Epic epic = manager.createEpic(new Epic("First эпик", "Описание Эпика111"));
        for (int i = 1; i < 9; i++) {
            manager.createSubtask(new Subtask("Подзадача " + i, "Описание " + i, epic.getId(),
                    "02.04.2024 1" + i + ":00", 33));
        }
        final Epic epic2 = manager.createEpic(new Epic("Second эпик", "Описание Эпика222"));
        for (int i = 1; i < 9; i += 2) {
            manager.createSubtask(new Subtask("Подзадача " + (i + 100), "Описание " + (i + 100),
                    epic2.getId(), "13.04.2024 1" + i + ":00", 23));
            manager.createSubtask(new Subtask("Подзадача " + (i + 100), "Описание " + (i + 100),
                    epic2.getId()));
        }
        // Создание задач с временем начала (с приоритетом) и без приоритета
        // id задач с 1 по 10
        // нечетные - приоритезированные, четные - без начального времени
        // при создании приоритетных задач уже проверяется что интервалы не пересекаются
        for (int i = 1; i < 11; i += 2) {
            manager.createTask(new Task("Task_" + i, "Descript_" + i,
                    (i + 10) + ".01.2024 " + (i + 10) + ":01", 35));
            manager.createTask(new Task("Task_" + (i + 1), "Descript_" + (i + 1)));
        }
        // создадим еще пару вариантов
        manager.createSubtask(new Subtask("Подзадача J", "Описание: без времени ", epic2.getId()));
        manager.createTask(new Task("Task", "Task without time"));
        manager.createTask(new Task("Task", "Task with time",
                "03.04.2024 19:00", 45));
        manager.createEpic(new Epic("Эпик999", "Эпик без подзадач"));
        return manager;
    }

    protected static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.serializeNulls();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new TimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        return gsonBuilder.create();
    }

    URI createURI(String path) {
        return URI.create("http://localhost:" + HttpTaskServer.PORT + path);
    }

    // отправка запроса на получения GET
    HttpRequest buildGET_Request(URI uri) {
        // создаём объект, описывающий HTTP-запрос
        return HttpRequest.newBuilder() // получаем экземпляр билдера
                .GET()
                .uri(uri) // указываем адрес ресурса
                .version(HttpClient.Version.HTTP_1_1) // указываем версию протокола
                .header("Accept", "text/html; charset=UTF-8")
                .build();
    }

    // отправка запроса на добавление POST с телом
    HttpRequest buildPOST_Request(URI uri, String bodyRequest) {
        return HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(bodyRequest))
                .uri(uri) // адрес ресурса
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "text/html; charset=UTF-8")
                .build();
    }

    // отправка запроса на удаление DELETE
    HttpRequest buildDELETE_Request(URI uri) {
        return HttpRequest.newBuilder()
                .DELETE()
                .uri(uri) // адрес ресурса
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "text/html; charset=UTF-8")
                .build();
    }

    @BeforeEach
    void setUp() {
        // перед запуском каждого теста очищаем память от внесенных изменений
        // и заново запускаем тестовые данные
        manager = Managers.getDefault();
        manager = testInitialization(manager);
        // Запускаем сервер
        httpTaskServer = new HttpTaskServer(manager);    // используем конструктор с добавленным менеджером
        httpTaskServer.start();
    }

    @AfterEach
    void tearDown() {
        // останавливаем сервер
        httpTaskServer.stop();
    }

    @Test
    @DisplayName("Должен отдавать в ответ список всех задач")
    void ShouldResponseGET_Tasks() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        HttpRequest request = buildGET_Request(createURI("/tasks"));
        // отправляем запрос и получаем ответ от сервера
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "статус ответа != 200");   // тест статуса ответа

        JsonElement jsonElement = JsonParser.parseString(response.body());

        // преобразуем в JSON-массив
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(manager.getTasksList().size(), jsonArray.size(),    // тест размера возвращаемого массива
                "Кол-во возвращаемых в ответе задач не равно числу инициализированных!");

        for (JsonElement element : jsonArray) {    // тест сопоставления каждого Json объекта задаче из памяти
            JsonObject taskJson = element.getAsJsonObject();
            int id = taskJson.get("id").getAsInt();
            Task task = manager.getTask(id);
        }
    }

    @Test
    @DisplayName("Должен отдавать в ответ задачу по id URI")
    void ShouldResponseGET_TaskById() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        int sumTasks = manager.getTasksList().size() + manager.getSubtaskList().size() + manager.getEpicList().size();
        for (int i = 1; i <= sumTasks; i++) {
            HttpRequest TaskRequest = buildGET_Request(createURI("/tasks/" + i));
            // отправляем запрос и получаем ответ от сервера
            HttpResponse<String> response = client.send(TaskRequest, HttpResponse.BodyHandlers.ofString());
            try {
                Task task = manager.getTask(i);
                JsonElement jsonElement = JsonParser.parseString(response.body());
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                assertEquals(task.getId(), jsonObject.get("id").getAsInt(),
                        "Тело ответа содержит некорректную задачу " +
                                "(id полученного объекта != id задаче из памяти");
            } catch (NotFoundException except) {
                assertEquals(404, response.statusCode(),
                        "Неверный код ответа, когда задача не найдена");
            }
        }
    }

    @Test
    @DisplayName("Должен записывать в память  новую задачу из запроса")
    void ShouldPOST_InRequest_TaskCreating() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        Task newTask = new Task("Задача без времени", "ShouldPOST_InRequest_TaskCreating");
        Task newTaskWithTime = new Task("Задача с временем_1", "ShouldPOST_InRequest_TaskCreating",
                "01.01.2000 10:00", 1441);
        // задача которая будет пересекаться по времени с предыдущей
        Task newTaskWithTime2 = new Task("Задача с временем_2", "ShouldPOST_InRequest_TaskCreating",
                "02.01.2000 10:00", 0);

        String jsonString1 = getGson().toJson(newTask);
        String jsonString2 = getGson().toJson(newTaskWithTime);
        String jsonString3 = getGson().toJson(newTaskWithTime2);

        HttpRequest TaskRequest1 = buildPOST_Request(createURI("/tasks"), jsonString1);
        HttpResponse<String> response1 = client.send(TaskRequest1, HttpResponse.BodyHandlers.ofString());
        // тест статуса ответа при добавлении неприоритизированной задачи
        assertEquals(201, response1.statusCode(), "статус ответа != 201");

        HttpRequest TaskRequest2 = buildPOST_Request(createURI("/tasks"), jsonString2);
        HttpResponse<String> response2 = client.send(TaskRequest2, HttpResponse.BodyHandlers.ofString());
        // тест статуса ответа при добавлении приоритетной задачи,
        // когда оне не пересекается по времени с другими задачами
        assertEquals(201, response2.statusCode(), "статус ответа != 201");

        HttpRequest TaskRequest3 = buildPOST_Request(createURI("/tasks"), jsonString3);
        HttpResponse<String> response3 = client.send(TaskRequest3, HttpResponse.BodyHandlers.ofString());
        // тест статуса ответа при добавлении приоритетной задачи,
        // когда оне пересекается по времени с другими задачами
        assertEquals(406, response3.statusCode(), "статус ответа != 406");
    }

    @Test
    @DisplayName("Должен обновлять в памяти задачу из запроса (если в запросе передан id)")
    void ShouldPOST_InRequest_TaskUpdating() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        Task newTask = new Task("Задача без времени", "ShouldPOST_InRequest_TaskUpdating");
        Task newTaskWithTime = new Task("Задача с временем_1", "ShouldPOST_InRequest_TaskUpdating",
                "01.01.2000 10:00", 1441);
        // задача которая будет пересекаться по времени с предыдущей
        Task newTaskWithTime2 = new Task("Задача с временем_2", "ShouldPOST_InRequest_TaskUpdating",
                "02.01.2000 10:00", 0);

        // устанавливаем id задачам, чтобы имитировать обновление
        newTask.setId(manager.getTasksList().get(1).getId());
        newTaskWithTime.setId(manager.getTasksList().get(2).getId());
        newTaskWithTime2.setId(manager.getTasksList().get(3).getId());

        String jsonString1 = getGson().toJson(newTask);
        String jsonString2 = getGson().toJson(newTaskWithTime);
        String jsonString3 = getGson().toJson(newTaskWithTime2);

        HttpRequest TaskRequest1 = buildPOST_Request(createURI("/tasks"), jsonString1);
        HttpResponse<String> response1 = client.send(TaskRequest1, HttpResponse.BodyHandlers.ofString());
        // тест статуса ответа при добавлении неприоритизированной задачи
        assertEquals(200, response1.statusCode(), "статус ответа != 200");

        HttpRequest TaskRequest2 = buildPOST_Request(createURI("/tasks"), jsonString2);
        HttpResponse<String> response2 = client.send(TaskRequest2, HttpResponse.BodyHandlers.ofString());
        // тест статуса ответа при добавлении приоритетной задачи,
        // когда оне не пересекается по времени с другими задачами
        assertEquals(200, response2.statusCode(), "статус ответа != 200");

        HttpRequest TaskRequest3 = buildPOST_Request(createURI("/tasks"), jsonString3);
        HttpResponse<String> response3 = client.send(TaskRequest3, HttpResponse.BodyHandlers.ofString());
        // тест статуса ответа при добавлении приоритетной задачи,
        // когда оне пересекается по времени с другими задачами
        assertEquals(406, response3.statusCode(), "статус ответа != 406");
    }

    @Test
    @DisplayName("Должен удалять из памяти задачу по id URI")
    void ShouldDELETE_Task_InRequestURIById() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        for (Task task : manager.getTasksList()) {
            int id = task.getId();
            HttpRequest TaskRequest = buildDELETE_Request(createURI("/tasks/" + id));
            // отправляем запрос и получаем ответ от сервера
            HttpResponse<String> response = client.send(TaskRequest, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Неверный код ответа, когда задача удалена");
            try {
                int errorId = manager.getTask(id).getId();
                assertEquals(999, errorId, "После удаления задача осталась в памяти менеджера");
            } catch (NotFoundException except) {
                assertEquals("Задача не найдена по id = " + id, except.getMessage(),
                        "После удаления задача осталась в памяти менеджера");
            }
        }
        // тест удаления задачи с неправильным id в URI запросе
        HttpRequest TaskErrorRequest = buildDELETE_Request(createURI("/tasks/" + 999));
        // отправляем запрос и получаем ответ от сервера
        HttpResponse<String> response = client.send(TaskErrorRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "Неверный код ответа, когда задача не найдена для удаления");
    }

    @Test
    @DisplayName("Должен отдавать в ответ список всех подзадач")
    void ShouldResponseGET_Subtasks() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        HttpRequest request = buildGET_Request(createURI("/subtasks"));
        // отправляем запрос и получаем ответ от сервера
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "статус ответа != 200");   // тест статуса ответа

        JsonElement jsonElement = JsonParser.parseString(response.body());

        // преобразуем в JSON-массив
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(manager.getSubtaskList().size(), jsonArray.size(),    // тест размера возвращаемого массива
                "Кол-во возвращаемых в ответе задач не равно числу инициализированных!");

        for (JsonElement element : jsonArray) {    // тест сопоставления каждого Json объекта подзадаче из памяти
            JsonObject taskJson = element.getAsJsonObject();
            int id = taskJson.get("id").getAsInt();
            Task task = manager.getSubtask(id);
        }
    }

    @Test
    @DisplayName("Должен отдавать в ответ подзадачу по id URI")
    void ShouldResponseGET_SubtaskById() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        int sumTasks = manager.getTasksList().size() + manager.getSubtaskList().size() + manager.getEpicList().size();
        for (int i = 1; i <= sumTasks; i++) {
            HttpRequest Subtaskrequest = buildGET_Request(createURI("/subtasks/" + i));
            // отправляем запрос и получаем ответ от сервера
            HttpResponse<String> response = client.send(Subtaskrequest, HttpResponse.BodyHandlers.ofString());
            try {
                Subtask subtask = manager.getSubtask(i);
                JsonElement jsonElement = JsonParser.parseString(response.body());
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                assertEquals(subtask.getId(), jsonObject.get("id").getAsInt(),
                        "Тело ответа содержит некорректную подзадачу " +
                                "(id полученного объекта != id подзадаче из памяти");
            } catch (NotFoundException except) {
                assertEquals(404, response.statusCode(),
                        "Неверный код ответа, когда задача не найдена");
            }
        }
    }

    @Test
    @DisplayName("Должен записывать в память  новую подзадачу из запроса")
    void ShouldPOST_InRequest_SubtaskCreating() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        Subtask newTask = new Subtask("Подзадача без времени",
                "ShouldPOST_InRequest_SubtaskCreating", 1);
        Subtask newTaskWithTime = new Subtask("Подзадача с временем_1",
                "ShouldPOST_InRequest_SubtaskCreating", 1, "01.01.2000 10:00", 1441);
        // задача которая будет пересекаться по времени с предыдущей
        Subtask newTaskWithTime2 = new Subtask("Подзадача с временем_2",
                "ShouldPOST_InRequest_SubtaskCreating", 1, "02.01.2000 10:00", 0);

        String jsonString1 = getGson().toJson(newTask);
        String jsonString2 = getGson().toJson(newTaskWithTime);
        String jsonString3 = getGson().toJson(newTaskWithTime2);

        HttpRequest TaskRequest1 = buildPOST_Request(createURI("/subtasks"), jsonString1);
        HttpResponse<String> response1 = client.send(TaskRequest1, HttpResponse.BodyHandlers.ofString());
        // тест статуса ответа при добавлении неприоритизированной задачи
        assertEquals(201, response1.statusCode(), "статус ответа != 201");

        HttpRequest TaskRequest2 = buildPOST_Request(createURI("/subtasks"), jsonString2);
        HttpResponse<String> response2 = client.send(TaskRequest2, HttpResponse.BodyHandlers.ofString());
        // тест статуса ответа при добавлении приоритетной задачи,
        // когда оне не пересекается по времени с другими задачами
        assertEquals(201, response2.statusCode(), "статус ответа != 201");

        HttpRequest TaskRequest3 = buildPOST_Request(createURI("/subtasks"), jsonString3);
        HttpResponse<String> response3 = client.send(TaskRequest3, HttpResponse.BodyHandlers.ofString());
        // тест статуса ответа при добавлении приоритетной задачи,
        // когда оне пересекается по времени с другими задачами
        assertEquals(406, response3.statusCode(), "статус ответа != 406");
    }

    @Test
    @DisplayName("Должен обновлять в памяти подзадачу из запроса (если в запросе передан id)")
    void ShouldPOST_InRequest_SubtaskUpdating() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        Subtask newTask = new Subtask("Подзадача без времени",
                "ShouldPOST_InRequest_SubtaskCreating", 1);
        Subtask newTaskWithTime = new Subtask("Подзадача с временем_1",
                "ShouldPOST_InRequest_SubtaskCreating", 1, "01.01.2000 10:00", 1441);
        // задача которая будет пересекаться по времени с предыдущей
        Subtask newTaskWithTime2 = new Subtask("Подзадача с временем_2",
                "ShouldPOST_InRequest_SubtaskCreating", 1, "02.01.2000 10:00", 0);
        // устанавливаем id подзадачам, чтобы имитировать обновление
        newTask.setId(manager.getSubtaskList().get(1).getId());
        newTaskWithTime.setId(manager.getSubtaskList().get(2).getId());
        newTaskWithTime2.setId(manager.getSubtaskList().get(3).getId());

        String jsonString1 = getGson().toJson(newTask);
        String jsonString2 = getGson().toJson(newTaskWithTime);
        String jsonString3 = getGson().toJson(newTaskWithTime2);

        HttpRequest TaskRequest1 = buildPOST_Request(createURI("/subtasks"), jsonString1);
        HttpResponse<String> response1 = client.send(TaskRequest1, HttpResponse.BodyHandlers.ofString());
        // тест статуса ответа при добавлении неприоритизированной задачи
        assertEquals(200, response1.statusCode(), "статус ответа != 200");

        HttpRequest TaskRequest2 = buildPOST_Request(createURI("/subtasks"), jsonString2);
        HttpResponse<String> response2 = client.send(TaskRequest2, HttpResponse.BodyHandlers.ofString());
        // тест статуса ответа при добавлении приоритетной задачи,
        // когда оне не пересекается по времени с другими задачами
        assertEquals(200, response2.statusCode(), "статус ответа != 200");

        HttpRequest TaskRequest3 = buildPOST_Request(createURI("/subtasks"), jsonString3);
        HttpResponse<String> response3 = client.send(TaskRequest3, HttpResponse.BodyHandlers.ofString());
        // тест статуса ответа при добавлении приоритетной задачи,
        // когда оне пересекается по времени с другими задачами
        assertEquals(406, response3.statusCode(), "статус ответа != 406");
    }

    @Test
    @DisplayName("Должен удалять из памяти подзадачу по id URI")
    void ShouldDELETE_Subtask_InRequestURIById() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        for (Subtask subtask : manager.getSubtaskList()) {
            int id = subtask.getId();
            HttpRequest TaskRequest = buildDELETE_Request(createURI("/subtasks/" + id));
            // отправляем запрос и получаем ответ от сервера
            HttpResponse<String> response = client.send(TaskRequest, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Неверный код ответа, когда подзадача удалена");
            try {
                int errorId = manager.getSubtask(id).getId();
                assertEquals(999, errorId, "После удаления подзадача осталась в памяти менеджера");
            } catch (NotFoundException except) {
                assertEquals("Подзадача не найдена по id = " + id, except.getMessage(),
                        "После удаления подзадача осталась в памяти менеджера");
            }
        }
        // тест удаления подзадачи с неправильным id в URI запросе
        HttpRequest TaskErrorRequest = buildDELETE_Request(createURI("/subtasks/" + 999));
        // отправляем запрос и получаем ответ от сервера
        HttpResponse<String> response = client.send(TaskErrorRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "Неверный код ответа, когда подзадача не найдена для удаления");

    }

    @Test
    @DisplayName("Должен отдавать в ответ список всех эпиков")
    void ShouldResponseGET_Epics() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        HttpRequest request = buildGET_Request(createURI("/epics"));
        // отправляем запрос и получаем ответ от сервера
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "статус ответа != 200");   // тест статуса ответа

        JsonElement jsonElement = JsonParser.parseString(response.body());

        // преобразуем в JSON-массив
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(manager.getEpicList().size(), jsonArray.size(),    // тест размера возвращаемого массива
                "Кол-во возвращаемых в ответе задач не равно числу инициализированных!");

        for (JsonElement element : jsonArray) {    // тест сопоставления каждого Json объекта подзадаче из памяти
            JsonObject taskJson = element.getAsJsonObject();
            int id = taskJson.get("id").getAsInt();
            Task task = manager.getEpic(id);
        }
    }

    @Test
    @DisplayName("Должен отдавать в ответ Эпик по id URI")
    void ShouldResponseGET_EpicById() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        int sumTasks = manager.getTasksList().size() + manager.getSubtaskList().size() + manager.getEpicList().size();
        for (int i = 1; i <= sumTasks; i++) {
            HttpRequest Epickrequest = buildGET_Request(createURI("/epics/" + i));
            // отправляем запрос и получаем ответ от сервера
            HttpResponse<String> response = client.send(Epickrequest, HttpResponse.BodyHandlers.ofString());
            try {
                Epic epic = manager.getEpic(i);
                JsonElement jsonElement = JsonParser.parseString(response.body());
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                assertEquals(epic.getId(), jsonObject.get("id").getAsInt(),
                        "Тело ответа содержит некорректный эпик " +
                                "(id полученного объекта != id эпика из памяти");
            } catch (NotFoundException except) {
                assertEquals(404, response.statusCode(),
                        "Неверный код ответа, когда эпик не найден");
            }
        }
    }

    @Test
    @DisplayName("Должен записывать в память  новый эпик из запроса")
    void ShouldPOST_InRequest_EpicCreating() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();

        int beginSizeEpics = manager.getEpicList().size();
        Epic newEpic = new Epic("ЭпикТест1", "ShouldPOST_InRequest_EpicCreating");
        String jsonString = getGson().toJson(newEpic);
        HttpRequest epicRequest = buildPOST_Request(createURI("/epics"), jsonString);
        HttpResponse<String> response = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        int lastSizeEpics = manager.getEpicList().size();

        // тест статуса ответа при добавлении эпика
        assertEquals(201, response.statusCode(), "статус ответа != 201");
        // тест добавления эпика в память менеджера
        assertEquals(beginSizeEpics + 1, lastSizeEpics, "Эпик не добавился в память менеджера!");
    }


    @Test
    @DisplayName("Должен удалять из памяти эпик по id URI")
    void ShouldDELETE_Epic_InRequestURIById() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        for (Epic epic : manager.getEpicList()) {
            int id = epic.getId();
            HttpRequest TaskRequest = buildDELETE_Request(createURI("/epics/" + id));
            // отправляем запрос и получаем ответ от сервера
            HttpResponse<String> response = client.send(TaskRequest, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Неверный код ответа, когда эпик удален");
            try {
                int errorId = manager.getEpic(id).getId();
                assertEquals(999, errorId, "После удаления эпик остался в памяти менеджера");
            } catch (NotFoundException except) {
                assertEquals("Эпик с id = " + id + " не найден", except.getMessage(),
                        "После удаления эпик остался в памяти менеджера");
            }
        }
        // тест удаления эпика с неправильным id в URI запросе
        HttpRequest epicErrorRequest = buildDELETE_Request(createURI("/epics/" + 999));
        // отправляем запрос и получаем ответ от сервера
        HttpResponse<String> response = client.send(epicErrorRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "Неверный код ответа, когда эпик не найден для удаления");


    }

    @Test
    @DisplayName("Должен отдавать в ответ подзадачи, прикрепленные к одному эпику по id URI эпика")
    void ShouldResponseGET_EpicsSubtasksByIdEpic() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        int sumTasks = manager.getTasksList().size() + manager.getSubtaskList().size() + manager.getEpicList().size();
        for (int id = 1; id <= sumTasks; id++) {
            HttpRequest request = buildGET_Request(createURI("/epics/" + id + "/subtasks"));
            // отправляем запрос и получаем ответ от сервера
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            try {    // тест статуса ответа
                assertEquals(200, response.statusCode(), "статус ответа != 200");

                JsonElement jsonElement = JsonParser.parseString(response.body());
                // преобразуем в JSON-массив
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                // тест размера возвращаемого массива
                assertEquals(manager.getListSubtasksFromEpic(id).size(), jsonArray.size(),
                        "Кол-во возвращаемых в ответе подзадач не равно числу инициализированных в эпике!");
                // тест сопоставления каждого Json объекта подзадаче из памяти
                for (JsonElement element : jsonArray) {
                    JsonObject taskJson = element.getAsJsonObject();
                    int idJson = taskJson.get("id").getAsInt();
                    manager.getSubtask(idJson);
                }
            } catch (NotFoundException except) {
                assertEquals(404, response.statusCode(),
                        "Неверный код ответа, когда эпик не найден");
            }
        }
    }

    @Test
    @DisplayName("Должен отдавать в ответ список истории")
    void ShouldResponseGET_History() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        HttpRequest request = buildGET_Request(createURI("/history"));
        // отправляем запрос и получаем ответ от сервера
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "статус ответа != 200");   // тест статуса ответа

        JsonElement jsonElement = JsonParser.parseString(response.body());
        String historyFromJson = jsonElement.getAsString();
        assertEquals(CSVFormat.historyToString(manager.getHistoryManager()), historyFromJson,
                "История из ответа отличается от истории из памяти менеджера!");
    }

    @Test
    @DisplayName("Должен отдавать в ответ список приоритетных задач, фильтруя по времени начала")
    void ShouldResponseGET_PrioritizedTasks() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        HttpRequest request = buildGET_Request(createURI("/prioritized"));
        // отправляем запрос и получаем ответ от сервера
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "статус ответа != 200");   // тест статуса ответа

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        assertEquals(manager.getPrioritizedTasks().size(), jsonArray.size(),
                "Размер возвращаемых приоритетных задач отличается " +
                        "от размера приоритетных задач в памяти менеджера!");

        // Проверяем каждый элемент приоритетного списка задач:
        // всегда из ответа должен находиться один объект с id == id приоритетной задачи в памяти
        for (JsonElement jsonTask : jsonArray) {
            List<Task> priorityTask = manager.getPrioritizedTasks().stream()
                    .filter(task -> task.getId() == jsonTask.getAsJsonObject().get("id").getAsInt())
                    .collect(Collectors.toList());
            assertEquals(1, priorityTask.size());
        }
    }

    @Test
    @DisplayName("Должен выдавать 404 ошибку если ресурс не найден")
    void shouldResponseUnknownResource() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        HttpRequest request1 = buildGET_Request(createURI("/prioritized/unknown"));
        HttpRequest request2 = buildGET_Request(createURI("/tasks/unknown"));
        HttpRequest request3 = buildGET_Request(createURI("/subtasks/unknown"));
        HttpRequest request4 = buildGET_Request(createURI("/epics/unknown"));
        HttpRequest request5 = buildGET_Request(createURI("/history/unknown"));
        HttpRequest request6 = buildGET_Request(createURI("/unknown"));

        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode(), "статус ответа != 404");

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response2.statusCode(), "статус ответа != 404");

        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response3.statusCode(), "статус ответа != 404");

        HttpResponse<String> response4 = client.send(request4, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response4.statusCode(), "статус ответа != 404");

        HttpResponse<String> response5 = client.send(request5, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response5.statusCode(), "статус ответа != 404");

        HttpResponse<String> response6 = client.send(request6, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response6.statusCode(), "статус ответа != 404");


    }

}