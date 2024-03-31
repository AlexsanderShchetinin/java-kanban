package service;

import model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CSVFormat {

    // Получение задачи (подзадачи, эпика) из строки
    protected static Task taskFromString(String value, Type type) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        switch (type) {
            case TASK:
                Task task = new Task(parts[2], parts[4]);
                task.setId(id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(parts[2], parts[4]);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                Subtask subtask = new Subtask(parts[2], parts[4], epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                return null;
        }
    }

    // Сохранение задачи в строку
    protected static String taskToString(Task task) {
        return task.getId() + "," + task.getTaskType() + "," + task.getName() + ","
                + task.getStatus() + "," + task.getDescription() + "," + task.getEpicId();
    }

    // Получение обратной последовательности вызова истории
    protected static List<Integer> historyFromString(String value) {
        LinkedList<Integer> numbersHistory = new LinkedList<>();
        String[] parts = value.split(",");
        for (int i = 1; i < parts.length; i++) {    // начинаем с единицы, чтобы в выборку не попала "ИСТОРИЯ"
            try {
                int receivedId = Integer.parseInt(parts[i]);
                numbersHistory.addFirst(receivedId);    // добавляем в лист вызов истории в обратном порядке
            } catch (Exception e) {
                System.out.println(e.getCause() + e.getMessage());
                break;
            }
        }
        return numbersHistory;
    }

    // Сохранение менеджера истории в строку, состоящую из последовательности id задач
    protected static String historyToString(HistoryManager manager) {
        ArrayList<Task> history = (ArrayList<Task>) manager.getHistory();
        StringBuilder sb = new StringBuilder();
        sb.append(Type.HISTORY).append(",");
        for (Task task : history) {
            sb.append(task.getId()).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }
}
