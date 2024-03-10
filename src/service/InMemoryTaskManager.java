package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Subtask> subtasks;
    private final HashMap<Integer, Epic> epics;
    private static int identifier;
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
        identifier = 0;
        this.historyManager = historyManager;
    }


    private int generateID() {
        return ++identifier;
    }

    @Override
    public List<Task> getHistory() {
        return  historyManager.getHistory();
    }

    @Override
    public Task createTask(Task task) {
        task.setId(generateID());
        tasks.put(task.getId(), task);
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
        tasks.remove(newTask.getId());
        tasks.put(newTask.getId(), newTask);
    }

    @Override
    public void removeTask(int id) {
        tasks.remove(id);
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        // Проверка прикрепленного эпика (эпик не должен содержать ид подзадач)
        if(subtasks.containsKey(subtask.getEpic().getId())) return null;
        subtask.setId(generateID());
        Epic epicBySubtask = subtask.getEpic();
        if (epicBySubtask.getSubtasks() != null) {
            if (!(epicBySubtask.getSubtasks().contains(subtask))) {
                // если в эпике нет подзадачи - добавляем
                ArrayList<Subtask> listSubtask = epicBySubtask.getSubtasks();
                listSubtask.add(subtask);
                epicBySubtask.setSubtasks(listSubtask);
            }
        } else {
            // если нет ни одной подзадачи в эпике - тоже добавляем
            ArrayList<Subtask> listSubtask = new ArrayList<>();
            listSubtask.add(subtask);
            epicBySubtask.setSubtasks(listSubtask);
        }
        Epic updateEpic = checkEpicStatus(epicBySubtask);    // проверяем статус эпика
        // Проверяем был ли создан эпик в менеджере, если нет - то не добавляем
        if(!(updateEpic.getId() == 0)){
            epics.remove(updateEpic.getId());
            epics.put(updateEpic.getId(), updateEpic);
        }
        subtasks.put(subtask.getId(), subtask);
        return subtask;
    }

    @Override
    public List<Subtask> getSubtaskList() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if(subtask == null){
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
        if(subtasks.containsKey(newSubtask.getEpic().getId())) return;
        // если в новой подзадаче в эпике не добавлена подзадача - добавляем
        if (!(newSubtask.getEpic().getSubtasks().contains(newSubtask))) {
            ArrayList<Subtask> subtasksNewEpic = newSubtask.getEpic().getSubtasks();
            subtasksNewEpic.add(newSubtask);
            newSubtask.getEpic().setSubtasks(subtasksNewEpic);
        }
        Subtask oldSubtask = subtasks.get(newSubtask.getId());
        // проверяем совпадают ли ключи эпиков в старой и новой подзадаче
        if (oldSubtask.getEpic().getId() == newSubtask.getEpic().getId()) {
            // если эпики не меняли - просто обновляем подзадачу и связанный эпик
            subtasks.remove(newSubtask.getId());
            subtasks.put(newSubtask.getId(), newSubtask);
            Epic epic = checkEpicStatus(newSubtask.getEpic());    // обновляем статус эпика
            epics.remove(epic.getId());
            epics.put(epic.getId(), epic);
        } else {    // иначе удаляем старую подзадачу в старом эпике
            Epic oldEpic = epics.get(oldSubtask.getEpic().getId());
            ArrayList<Subtask> subtasksOldEpic = oldEpic.getSubtasks();
            subtasksOldEpic.remove(oldSubtask);
            oldEpic.setSubtasks(subtasksOldEpic);
            epics.remove(oldEpic.getId());
            epics.put(oldEpic.getId(), oldEpic);
            // и повторяем логику обновления новой подзадачи
            subtasks.remove(newSubtask.getId());
            subtasks.put(newSubtask.getId(), newSubtask);
            Epic epic = checkEpicStatus(newSubtask.getEpic());
            epics.remove(epic.getId());
            epics.put(epic.getId(), epic);
        }
    }

    @Override
    public void removeSubtask(int id) {
        // удаляем подзадачу из эпика и обновляем эпик
        Epic epicAttached = subtasks.get(id).getEpic();
        ArrayList<Subtask> listSubtaskByEpic = epicAttached.getSubtasks();
        listSubtaskByEpic.remove(subtasks.get(id));
        epicAttached.setSubtasks(listSubtaskByEpic);
        epics.put(epicAttached.getId(), checkEpicStatus(epicAttached));
        // удаляем подзадачу
        subtasks.remove(id);
    }

    @Override
    public List<Subtask> getListSubtasksFromEpic(Epic epic) {
        return epics.get(epic.getId()).getSubtasks();
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
        return newEpic;
    }

    @Override
    public List<Epic> getEpicList() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if(epic == null){
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
    }

    @Override
    public void updateEpic(Epic newEpic) {
        // Проверка прикрепленых подзадач (id подзадач не должны содержать id эпиков)
        for (Subtask newSubtask : newEpic.getSubtasks()) {
            if(epics.containsKey(newSubtask.getId())) return;
        }
        Epic checkedEpic = checkEpicStatus(newEpic);    // проверяем корректность статуса эпика
        /* при обновлении эпика всегда список подзадач будет одинаковый в старом и новом эпике
        так как при объявлении конструктора эпика нет возможности вручную изменять список подзадач.
        Это специально сделано чтобы пользователь не смог скорректировать в эпике список подзадач.*/

        // при обновлении эпика обновляем все подзадачи связанные с ним - меняем в них эпик.
        ArrayList<Subtask> attachedSubtasks = epics.get(checkedEpic.getId()).getSubtasks();
        for (Subtask subtask : attachedSubtasks) {
            subtask.setEpic(checkedEpic);
            subtasks.remove(subtask.getId());
            subtasks.put(subtask.getId(), subtask);
        }
        // после этого обновляем сам эпик
        epics.remove(checkedEpic.getId());
        epics.put(checkedEpic.getId(), checkedEpic);
    }

    private Epic checkEpicStatus(Epic epic) {
        ArrayList<Subtask> subtaskFromEpic = epic.getSubtasks();
        if (subtaskFromEpic != null) {
            int SubtasksNumber = epic.getSubtasks().size();
            if (SubtasksNumber == 0) {
                epic.setStatus(TaskStatus.NEW);
                return epic;
            }
            for (Subtask subtask : epic.getSubtasks()) {
                if (subtask.getStatus() == TaskStatus.NEW && SubtasksNumber == epic.getSubtasks().size()) {
                    epic.setStatus(TaskStatus.NEW);
                } else if (subtask.getStatus() == TaskStatus.DONE) {
                    --SubtasksNumber;
                    if (SubtasksNumber == 0) {
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
