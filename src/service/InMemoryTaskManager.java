package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryTaskManager that = (InMemoryTaskManager) o;
        return Objects.equals(tasks, that.tasks) &&
                Objects.equals(subtasks, that.subtasks) &&
                Objects.equals(epics, that.epics) &&
                Objects.equals(historyManager, that.historyManager);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tasks, subtasks, epics, historyManager);
    }

    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Subtask> subtasks;
    protected final HashMap<Integer, Epic> epics;
    protected static int identifier;
    protected final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
        identifier = 0;
        this.historyManager = Managers.getDefaultHistory();
    }


    private int generateID() {
        return ++identifier;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Task createTask(Task task) {
        task.setId(generateID());
        tasks.put(task.getId(), task);
        historyManager.add(task);
        return task;
    }

    @Override
    public List<Task> getTasksList() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            return null;
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    @Override
    public void updateTask(Task newTask) {
        tasks.put(newTask.getId(), newTask);
    }

    @Override
    public void removeTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        // Проверка прикрепленного эпика (эпик не должен содержать ид подзадач)
        if (subtasks.containsKey(subtask.getEpicId())) return null;
        subtask.setId(generateID());
        subtasks.put(subtask.getId(), subtask);
        // Обновляем эпик
        Epic updatingEpic = epics.get(subtask.getEpicId());
        updateEpic(updatingEpic);
        historyManager.add(subtask);    // делаем запись в истории
        return subtask;
    }

    @Override
    public List<Subtask> getSubtaskList() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return null;
        }
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void clearSubtasks() {
        for (Epic epic : epics.values()) {    // удаляем подзадачи из эпиков
            ArrayList<Subtask> listSubtaskByEpic = epic.getSubtasks();
            listSubtaskByEpic.clear();
            epic.setSubtasks(listSubtaskByEpic);
            epics.remove(epic.getId());
            epics.put(epic.getId(), checkEpicStatus(epic));
        }
        subtasks.clear();    // удаляем подзадачи
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        // Проверка прикрепленного эпика (эпик не должен содержать ид подзадач)
        if (subtasks.containsKey(newSubtask.getEpicId())) return;
        Subtask oldSubtask = subtasks.get(newSubtask.getId());
        // проверяем совпадают ли ключи эпиков в старой и новой подзадаче
        if (oldSubtask.getEpicId() != newSubtask.getEpicId()) {
            // иначе удаляем старую подзадачу в старом эпике
            Epic oldEpic = epics.get(oldSubtask.getEpicId());
            ArrayList<Subtask> subtasksOldEpic = oldEpic.getSubtasks();
            subtasksOldEpic.remove(oldSubtask);
            oldEpic.setSubtasks(subtasksOldEpic);
            epics.put(oldEpic.getId(), oldEpic);
        }
        // обновляем новую подзадачу
        subtasks.put(newSubtask.getId(), newSubtask);
        updateEpic(epics.get(newSubtask.getEpicId()));
    }

    @Override
    public void removeSubtask(int id) {
        // удаляем подзадачу из эпика и обновляем эпик
        int epicId = subtasks.get(id).getEpicId();
        Epic epicAttached = epics.get(epicId);
        ArrayList<Subtask> listSubtaskByEpic = epicAttached.getSubtasks();
        listSubtaskByEpic.remove(subtasks.get(id));
        epicAttached.setSubtasks(listSubtaskByEpic);
        epics.put(epicAttached.getId(), checkEpicStatus(epicAttached));
        // удаляем подзадачу
        subtasks.remove(id);
        // удаляем из истории
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getListSubtasksFromEpic(Epic epic) {
        if (!subtasks.isEmpty()) {
            ArrayList<Subtask> subtasksFromEpic = new ArrayList<>();
            for (Subtask value : subtasks.values()) {
                if (value.getEpicId() == epic.getId()) {
                    subtasksFromEpic.add(value);
                }
            }
            return subtasksFromEpic;
        } else {
            return Collections.emptyList();
        }
    }

    /* допущение при создании эпика - эпик создается с пустым списком подзадач.
    на этапе создания эпика в нем еще нет подзадач, иначе произойдет зацикливание:
    если дать возможность во время создания эпика еще и создавать подзадачу,
    то во время создания эпика будет создаваться подзадача, которая будет ссылаться на еще не созданный эпик. */
    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(generateID());
        Epic newEpic = checkEpicStatus(epic);    // проверяем статус эпика и возвращаем корректный
        epics.put(newEpic.getId(), newEpic);
        historyManager.add(newEpic);    // добавляем историю
        return newEpic;
    }

    @Override
    public List<Epic> getEpicList() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public void clearEpics() {
        // если удаляются все эпики то соответственно и удаляются все подзадачи
        clearSubtasks();
        epics.clear();    // удаляем все эпики
    }

    @Override
    public void removeEpic(int id) {
        // при удалении эпика удаляем связанные с ним подзадачи
        Epic epic = epics.get(id);
        ArrayList<Subtask> subtasksFromEpic = epic.getSubtasks();
        for (Subtask subtask : subtasksFromEpic) {
            subtasks.remove(subtask.getId());
        }
        epics.remove(id);    // удаляем эпик
        historyManager.remove(id);    // удаляем из истории
    }

    @Override
    public void updateEpic(Epic newEpic) {
        // Проверка прикрепленых подзадач (id подзадач не должны содержать id эпиков)
        for (Subtask newSubtask : newEpic.getSubtasks()) {
            if (epics.containsKey(newSubtask.getId())) return;
        }
        Epic checkedEpic = checkEpicStatus(newEpic);    // проверяем корректность статуса эпика
        // обновляем список подзадач внутри эпика
        ArrayList<Subtask> newSubtasks = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (checkedEpic.getId() == subtask.getEpicId()) {
                newSubtasks.add(subtask);
            }
        }
        checkedEpic.setSubtasks(newSubtasks);
        // после этого обновляем сам эпик
        epics.put(checkedEpic.getId(), checkedEpic);
    }

    private Epic checkEpicStatus(Epic epic) {
        ArrayList<Subtask> subtaskFromEpic = epic.getSubtasks();
        if (subtaskFromEpic != null) {
            int subtasksNumber = epic.getSubtasks().size();
            if (subtasksNumber == 0) {
                epic.setStatus(TaskStatus.NEW);
                return epic;
            }
            for (Subtask subtask : epic.getSubtasks()) {
                if (subtask.getStatus() == TaskStatus.NEW && subtasksNumber == epic.getSubtasks().size()) {
                    epic.setStatus(TaskStatus.NEW);
                } else if (subtask.getStatus() == TaskStatus.DONE) {
                    --subtasksNumber;
                    if (subtasksNumber == 0) {
                        epic.setStatus(TaskStatus.DONE);
                        break;
                    } else {
                        epic.setStatus(TaskStatus.IN_PROGRESS);
                    }
                } else if (subtask.getStatus() == TaskStatus.IN_PROGRESS) {
                    epic.setStatus(TaskStatus.IN_PROGRESS);
                    break;
                }
            }
        } else {
            epic.setStatus(TaskStatus.NEW);
            return epic;
        }
        return epic;
    }
}
