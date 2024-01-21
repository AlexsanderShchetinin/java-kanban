import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.Manager;

public class Main {

    public static void main(String[] args) {
        // тестирование
        Manager manager = new Manager();

        // создаем 2 задачи, эпик с двумя подзадачами и эпик с одной подзадачей:
        Task task1 = manager.createTask(new Task("First Task", "Go to gym", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task("Second Task", "Make kanban",
                TaskStatus.IN_PROGRESS));
        Epic epic1 = manager.createEpic(new Epic("First Epic", "Learn to drive a car",
                TaskStatus.IN_PROGRESS));    // специально указал некоррекный статус
        System.out.println(manager.getEpic(epic1.getId()));   // проверяем как сменился статус эпика
        Subtask subtask1 = manager.createSubtask(new Subtask("First Subtask", "Give a license",
                TaskStatus.NEW, epic1));
        Subtask subtask2 = manager.createSubtask(new Subtask("Second Subtask", "Buy a car",
                TaskStatus.NEW, epic1));
        Epic epic2 = manager.createEpic(new Epic("Second Epic", "Finish school", TaskStatus.NEW));
        Subtask subtask3 = manager.createSubtask(new Subtask("Third Subtask", "Finish eleven classes",
                TaskStatus.IN_PROGRESS, epic2));
        System.out.println("_____");
        // печатаем списки эпиков, задач и подзадач:
        manager.getTasksList();
        manager.getEpicList();
        manager.getSubtaskList();
        System.out.println("_____");
        // меняем статусы созданных объектов
        task1.setStatus(TaskStatus.IN_PROGRESS);
        task2.setStatus(TaskStatus.DONE);
        manager.updateTask(task1);
        manager.updateTask(task2);
        System.out.println(manager.getTask(task1.getId()));
        System.out.println(manager.getTask(task2.getId()));

        // пытаемся вручную изменить статус у эпиков
        epic1.setStatus(TaskStatus.IN_PROGRESS);
        epic2.setStatus(TaskStatus.DONE);
        manager.updateEpic(epic1);
        manager.updateEpic(epic2);
        System.out.println("___");
        System.out.println(manager.getEpic(epic1.getId()));    // проверяем что статусы не изменились
        System.out.println(manager.getEpic(epic2.getId()));
        System.out.println("___");
        // меняем статусы у подзадач
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        subtask2.setStatus(TaskStatus.DONE);
        subtask3.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);
        manager.updateSubtask(subtask3);
        System.out.println(manager.getSubtask(subtask1.getId()));
        System.out.println(manager.getSubtask(subtask2.getId()));
        System.out.println(manager.getSubtask(subtask3.getId()));
        manager.getEpicList();    // прверяем что после этого статусы эпиков поменялись
        System.out.println("__________");
        // удаляем эпик, задачу и одну подзадачу
        manager.removeEpic(epic2.getId());
        manager.removeTask(task1.getId());
        manager.removeSubtask(subtask2.getId());
        // смотрим что осталось
        manager.getTasksList();
        manager.getEpicList();
        manager.getSubtaskList();
        System.out.println("____");
        // полностью удаляем подзадачи и смотрим что осталось
        manager.clearSubtasks();
        manager.getTasksList();
        manager.getEpicList();
        manager.getSubtaskList();
    }
}