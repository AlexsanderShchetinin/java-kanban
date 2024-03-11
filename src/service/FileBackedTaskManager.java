package service;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FileBackedTaskManager extends InMemoryTaskManager {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileBackedTaskManager that = (FileBackedTaskManager) o;
        return Objects.equals(tasksCollect, that.tasksCollect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tasksCollect);
    }

    static Path tasksFilePath = Paths.get("tasksAndHistoryFile.csv");

    protected final Map<Integer, Task> tasksCollect;

    public FileBackedTaskManager() {
        super();
        tasksCollect = new HashMap<>();
    }


    // загрузка задач и истории из файла во время запуска программы
    public static FileBackedTaskManager loadFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8))) {
            FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager();
            int maxId = 0;
            while (reader.ready()) {
                String line = reader.readLine();
                if (!line.isEmpty() && !line.isBlank()) {
                    String[] parts = line.split(",");
                    // по первому элементу массива определяем данные строки
                    switch (parts[0]) {
                        case "id":
                            break;    // ничего не делаем
                        case "HISTORY":
                            // так как строка с историей идет последней при парсинге, то перед созданием истории
                            // нужно добавить список с subtasks в каждый epic - используем метод обновления эпика.
                            for (Epic epic : fileBackedTaskManager.epics.values()) {
                                fileBackedTaskManager.updateEpic(epic);
                            }
                            List<Integer> historyNumbers = historyFromString(line);
                            for (Integer numId : historyNumbers) {
                                try {
                                    Task task = fileBackedTaskManager.tasksCollect.get(numId);
                                    fileBackedTaskManager.historyManager.add(task);
                                } catch (NullPointerException e) {
                                    throw new RuntimeException("Считать историю по id: " + numId + " не удалось. ", e);
                                }
                            }
                            break;
                        // список с задачами не относится ни к одному типу, поэтому его обработку пишем в default.
                        default:
                            try {
                                int id = Integer.parseInt(parts[0]);
                                if (id > maxId) maxId = id;
                                Type type = Type.valueOf(parts[1]);
                                switch (type) {
                                    case TASK:
                                        Task receivedTask = taskFromString(line, type);
                                        fileBackedTaskManager.tasksCollect.put(id, receivedTask);
                                        fileBackedTaskManager.tasks.put(id, receivedTask);
                                        break;
                                    case SUBTASK:
                                        Subtask receivedSubtask = (Subtask) taskFromString(line, type);
                                        fileBackedTaskManager.tasksCollect.put(id, receivedSubtask);
                                        fileBackedTaskManager.subtasks.put(id, receivedSubtask);
                                        break;
                                    case EPIC:
                                        Epic receivedEpic = (Epic) taskFromString(line, type);
                                        fileBackedTaskManager.tasksCollect.put(id, receivedEpic);
                                        fileBackedTaskManager.epics.put(id, receivedEpic);
                                        break;
                                    default:
                                        String message = "Тип задачи не поддерживается при парсинге файла: " +
                                                tasksFilePath.getFileName() + " Строка: " + line;
                                        System.out.println(message);
                                        break;
                                }
                            } catch (Exception e) {
                                throw new RuntimeException("Неизвестный тип данных при чтении: " +
                                        tasksFilePath.getFileName() + " Строка: " + line, e);
                            }
                    }
                }
            }
            identifier = maxId;
            return fileBackedTaskManager;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла: " + tasksFilePath.getFileName(), e);
        }
    }

    // Получение задачи (подзадачи, эпика) из строки
    private static Task taskFromString(String value, Type type) {
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

    // Получение обратной последовательности вызова истории
    private static List<Integer> historyFromString(String value) {
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

    // Сохранение задач и истории в файл
    public void save() {
        try (final BufferedWriter writer = new BufferedWriter(
                new FileWriter(tasksFilePath.getFileName().toString(), StandardCharsets.UTF_8))) {
            ArrayList<Task> history = (ArrayList<Task>) getHistory();
            // сохраняем задачи (подзадачи и эпики)
            String headline = "id,type,name,status,description,epic";
            writer.write(headline);
            writer.newLine();
            for (Task task : history) {
                writer.write(taskToString(task));
                writer.newLine();
            }
            // сохраняем историю отдельной строкой вконце файла
            writer.newLine();
            writer.write(historyToString(historyManager));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи файла: " + tasksFilePath.getFileName(), e);
        }
    }

    private static String historyToString(HistoryManager manager) {
        ArrayList<Task> history = (ArrayList<Task>) manager.getHistory();
        StringBuilder sb = new StringBuilder();
        sb.append(Type.HISTORY).append(",");
        for (Task task : history) {
            sb.append(task.getId()).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }


    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    // Сохранение задачи в строку
    private String taskToString(Task task) {
        return task.getId() + "," + task.getTaskType() + "," + task.getName() + ","
                + task.getStatus() + "," + task.getDescription() + "," + task.getEpicId();
    }


    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void updateTask(Task newTask) {
        super.updateTask(newTask);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        super.updateSubtask(newSubtask);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void updateEpic(Epic newEpic) {
        super.updateEpic(newEpic);
        save();
    }

}
