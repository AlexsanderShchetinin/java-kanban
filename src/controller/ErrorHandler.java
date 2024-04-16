package controller;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exception.ManagerSaveException;
import exception.NotFoundException;
import exception.ParsingException;
import exception.ValidationException;

import java.io.IOException;

public class ErrorHandler extends CommonHandler {


    public static void handle(HttpExchange exchange, NotFoundException e) throws IOException {
        e.printStackTrace();
        sendGetResponse(exchange, getGson().toJson(e.getMessage()), 404);
    }

    public static void handle(HttpExchange exchange, NumberFormatException e) throws IOException {
        e.printStackTrace();
        sendGetResponse(exchange, getGson().toJson("По данному id ресурс не найден"), 404);
    }

    public static void handle(HttpExchange exchange, JsonSyntaxException e) throws IOException {
        e.printStackTrace();
        sendGetResponse(exchange, getGson().toJson(e.getMessage()), 400);
    }


    public static void handle(HttpExchange exchange, ValidationException e) throws IOException {
        e.printStackTrace();
        sendGetResponse(exchange, getGson().toJson(e.getMessage()), 406);
    }

    public static void handle(HttpExchange exchange, ManagerSaveException e) throws IOException {
        e.printStackTrace();
        sendGetResponse(exchange, getGson().toJson(e.getMessage()), 400);
    }

    public static void handle(HttpExchange exchange, ParsingException e) throws IOException {
        e.printStackTrace();
        sendGetResponse(exchange, getGson().toJson(e.getMessage()), 400);
    }


    public static void handle(HttpExchange exchange, Exception e) throws IOException {
        e.printStackTrace();
        sendGetResponse(exchange, getGson().toJson(e), 500);
    }

}
