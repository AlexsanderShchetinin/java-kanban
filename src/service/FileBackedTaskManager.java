package service;

import converter.CSVFormat;
import model.Epic;
import model.Subtask;
import model.Task;
import model.Type;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private static File backedFile;


    public FileBackedTaskManager(File file) {
        super();
        backedFile = file;
    }


    // загрузка задач и истории из файла во время запуска программы
    public static FileBackedTaskManager loadFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8))) {
            FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
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
                            List<Integer> historyNumbers = CSVFormat.historyFromString(line);
                            for (Integer numId : historyNumbers) {
                                try {
                                    Task task = fileBackedTaskManager.tasksCollect.get(numId);
                                    fileBackedTaskManager.historyManager.add(task);
                                } catch (NullPointerException e) {
                                    throw new RuntimeException("Считать историю по id: " + numId + " не удалось. ", e);
                                }
                            }
                            // нужно добавить список с subtasks в каждый epic - используем метод обновления эпика.
                            for (Epic epic : fileBackedTaskManager.epics.values()) {
                                fileBackedTaskManager.updateEpic(epic);
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
                                        Task receivedTask = CSVFormat.taskFromString(line, type);
                                        fileBackedTaskManager.tasksCollect.put(id, receivedTask);
                                        fileBackedTaskManager.tasks.put(id, receivedTask);
                                        break;
                                    case SUBTASK:
                                        Subtask receivedSubtask = (Subtask) CSVFormat.taskFromString(line, type);
                                        fileBackedTaskManager.tasksCollect.put(id, receivedSubtask);
                                        fileBackedTaskManager.subtasks.put(id, receivedSubtask);
                                        break;
                                    case EPIC:
                                        Epic receivedEpic = (Epic) CSVFormat.taskFromString(line, type);
                                        fileBackedTaskManager.tasksCollect.put(id, receivedEpic);
                                        fileBackedTaskManager.epics.put(id, receivedEpic);
                                        break;
                                    default:
                                        String message = "Тип задачи не поддерживается при парсинге файла: " +
                                                backedFile.getName() + " Строка: " + line;
                                        System.out.println(message);
                                        break;
                                }
                            } catch (Exception e) {
                                throw new RuntimeException("Неизвестный тип данных при чтении: " +
                                        backedFile.getName() + " Строка: " + line, e);
                            }
                    }
                }
            }
            identifier = maxId;
            return fileBackedTaskManager;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла: " + file.getName(), e);
        }
    }

    // Сохранение задач и истории в файл
    private void save() {
        try (final BufferedWriter writer = new BufferedWriter(
                new FileWriter(backedFile, StandardCharsets.UTF_8))) {
            ArrayList<Task> history = (ArrayList<Task>) getHistory();
            // сохраняем задачи (подзадачи и эпики)
            String headline = "id,type,name,status,description,epic,startTime,duration";
            writer.write(headline);
            writer.newLine();
            for (Task task : history) {
                writer.write(CSVFormat.taskToString(task));
                writer.newLine();
            }
            // сохраняем историю отдельной строкой вконце файла
            writer.newLine();
            writer.write(CSVFormat.historyToString(historyManager));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи файла: " + backedFile.getName(), e);
        }
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
