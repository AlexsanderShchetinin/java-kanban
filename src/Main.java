import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.FileBackedTaskManager;
import service.Managers;
import service.TaskManager;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        // тестирование
        TaskManager manager = Managers.getDefault();
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager();
        System.out.println("создаем 2 задачи, эпик с двумя подзадачами и эпик с одной подзадачей:");
        // создаем 2 задачи, эпик с двумя подзадачами и эпик с одной подзадачей:
        Task task1 = manager.createTask(new Task("First Task", "Go to gym"));
        Task task2 = manager.createTask(new Task("Second Task", "Make kanban"));
        Epic epic1 = manager.createEpic(new Epic("First Epic", "Learn to drive a car"));
        System.out.println(epic1);
        Subtask subtask1 = manager.createSubtask(new Subtask("First Subtask", "Give a license", epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Second Subtask", "Buy a car", epic1.getId()));
        Epic epic2 = manager.createEpic(new Epic("Second Epic", "Finish school"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Third Subtask", "Finish eleven classes",
                epic2.getId()));
        System.out.println("_____");
        System.out.println("печатаем списки эпиков, задач и подзадач");
        // печатаем списки эпиков, задач и подзадач:
        System.out.println("_____");
        System.out.println("меняем статусы созданных задач");
        // меняем статусы созданных объектов
        task1.setStatus(TaskStatus.IN_PROGRESS);
        task2.setStatus(TaskStatus.DONE);
        manager.updateTask(task1);
        manager.updateTask(task2);
        System.out.println(manager.getTask(task1.getId()));
        System.out.println(manager.getTask(task2.getId()));
        System.out.println("пытаемся вручную изменить статус у эпиков");
        // пытаемся вручную изменить статус у эпиков
        epic1.setStatus(TaskStatus.IN_PROGRESS);
        epic2.setStatus(TaskStatus.DONE);
        manager.updateEpic(epic1);
        manager.updateEpic(epic2);
        System.out.println("___");
        System.out.println("проверяем что статусы не изменились");
        System.out.println(manager.getEpic(epic1.getId()));    // проверяем что статусы не изменились
        System.out.println(manager.getEpic(epic2.getId()));
        System.out.println("___");
        // меняем статусы у подзадач
        System.out.println("меняем статусы у подзадач");
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        subtask2.setStatus(TaskStatus.DONE);
        subtask3.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);
        manager.updateSubtask(subtask3);
        System.out.println(manager.getSubtask(subtask1.getId()));
        System.out.println(manager.getSubtask(subtask2.getId()));
        System.out.println(manager.getSubtask(subtask3.getId()));
        System.out.println("прверяем что после этого статусы эпиков поменялись");
        manager.getEpicList();    // прверяем что после этого статусы эпиков поменялись
        System.out.println("__________");
        System.out.println("удаляем эпик, задачу и одну подзадачу, смотрим что осталось ");
        // удаляем эпик, задачу и одну подзадачу
        manager.removeEpic(epic2.getId());
        manager.removeTask(task1.getId());
        manager.removeSubtask(subtask2.getId());
        System.out.println("____");
        // полностью удаляем подзадачи и смотрим что осталось
        System.out.println("полностью удаляем подзадачи и смотрим что осталось");
        manager.clearSubtasks();

        // тестирование FileBackedTaskManager
        fileBackedTaskManager.createTask(new Task("First Task", "Go to gym"));
        fileBackedTaskManager.createTask(new Task("Second Task", "Make kanban"));
        Epic epic3 = fileBackedTaskManager.createEpic(new Epic("First Epic", "Learn to drive a car"));
        fileBackedTaskManager.createSubtask(
                new Subtask("First Subtask", "Give a license", epic3.getId()));
        fileBackedTaskManager.createSubtask(new Subtask("Second Subtask", "Buy a car", epic3.getId()));
        Epic updateEpic = fileBackedTaskManager.getEpic(10);
        updateEpic.setName("Update first Epic");
        fileBackedTaskManager.updateEpic(updateEpic);
        // Читаем записанный файл
        File load = new File("tasksAndHistoryFile.csv");
        FileBackedTaskManager newFileBackedTaskManager = FileBackedTaskManager.loadFromFile(load);
        // Используем newFileBackedTaskManager и проверяем что файл перезаписвается без изменений
        newFileBackedTaskManager.save();
    }
}