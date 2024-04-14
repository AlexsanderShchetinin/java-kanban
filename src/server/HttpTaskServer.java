package server;

import com.sun.net.httpserver.HttpServer;
import controller.*;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;


public class HttpTaskServer {

    private HttpServer server;
    private final TaskManager manager;
    public static final int PORT = 8080;


    // конструктор для тестов
    public HttpTaskServer(TaskManager manager) {
        this.manager = manager;
        createServ();
    }

    // продуктивный конструктор
    public HttpTaskServer() {
        // перед созданием и запуском сервера загружаем данные в память из файла - создаем экземпляр всего TaskManager
        this.manager = Managers.getBackedManager();
        createServ();
    }


    public static void main(String[] args) {
        HttpTaskServer httpTaskServer = new HttpTaskServer();
        httpTaskServer.start(); // запускаем сервер
    }

    public void createServ() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // прописываем все возможные пути по которым будет запускаться обработка запросов
        server.createContext("/tasks", new TaskHandler(manager));
        server.createContext("/subtasks", new SubtaskHandler(manager));
        server.createContext("/epics", new EpicHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));
    }

    public void start() {
        server.start();
        System.out.println("Сервер трекера задач запущен на порту " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер трекера задач остановлен на порту " + PORT);
    }
}
