package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {
    Task createTask(Task task);

    List<Task> getTasksList();

    Task getTask(int id);

    void clearTasks();

    void updateTask(Task newTask);

    void removeTask(int id);

    Subtask createSubtask(Subtask subtask);

    List<Subtask> getSubtaskList();

    Subtask getSubtask(int id);

    void clearSubtasks();

    void updateSubtask(Subtask newSubtask);

    void removeSubtask(int id);

    List<Subtask> getListSubtasksFromEpic(int epicId);

    Epic createEpic(Epic epic);

    List<Epic> getEpicList();

    Epic getEpic(int id);

    void clearEpics();

    void removeEpic(int id);

    void updateEpic(Epic newEpic);

    Epic checkEpicStatus(Epic epic);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

}
