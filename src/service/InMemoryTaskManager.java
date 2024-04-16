package service;

import exception.ManagerSaveException;
import exception.NotFoundException;
import exception.ValidationException;
import model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Subtask> subtasks;
    protected final HashMap<Integer, Epic> epics;
    protected static int identifier;
    protected final HistoryManager historyManager;
    protected final TreeSet<Task> prioritizedTasks;
    protected final Map<Integer, Task> tasksCollect;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
        identifier = 0;
        this.historyManager = Managers.getDefaultHistory();
        Comparator<Task> startTimeComparator = (o1, o2) -> {
            Optional<LocalDateTime> time1 = Optional.ofNullable(o1.getStartTime());
            Optional<LocalDateTime> time2 = Optional.ofNullable(o2.getStartTime());
            if (time1.isPresent() && time2.isPresent()) {
                if (time1.get().isAfter(time2.get())) return 1;
                else return -1;
            }
            return 0;
        };
        this.prioritizedTasks = new TreeSet<>(startTimeComparator);
        // мапа для загрузки данных из файлового хранилища во время запука FileBackedTaskManager
        tasksCollect = new HashMap<>();
    }


    private int generateID() {
        return ++identifier;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public Task createTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            throw new ValidationException("Задача c id=" + task.getId() + " уже создана!");
        }
        task.setId(generateID());
        addTaskByPriority(task);    // добавляем в приоритетные задачи
        tasks.put(task.getId(), task);    // вносим задачу в память менеджера
        historyManager.add(task);    // добавляем в историю
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
            throw new NotFoundException("Задача не найдена по id = " + id);
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public void clearTasks() {
        // очищаем из приоритетного множества задачи
        for (Task task : tasks.values()) {
            prioritizedTasks.remove(task);
        }
        tasks.clear();    // очищаем сами задачи
    }

    @Override
    public void updateTask(Task newTask) {
        if (tasks.containsKey(newTask.getId())) {    // проверяем что задача уже есть в мапе
            // заменяем задачу в приоритетном множестве на случай, если в newTask изменился приоритет
            addTaskByPriority(newTask);
            tasks.put(newTask.getId(), newTask);    // обновляем
            historyManager.add(newTask);
        }
    }

    @Override
    public void removeTask(int id) {
        if (tasks.containsKey(id)) {
            // удаляем задачу из всех таблиц и списков
            prioritizedTasks.remove(tasks.get(id));
            tasks.remove(id);
            historyManager.remove(id);
        } else {
            throw new ManagerSaveException("Задачи с id = " + id + " для удаления не существует!");
        }

    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            throw new ValidationException("Подзадача c id=" + subtask.getId() + " уже создана!");
        }
        // Проверка прикрепленного эпика
        if (subtasks.containsKey(subtask.getEpicId()) || !epics.containsKey(subtask.getEpicId())) {
            throw new ManagerSaveException("В подзадаче неверно указан epicId=" + subtask.getEpicId() +
                    " Эпика с таким id не существует!");
        }
        subtask.setId(generateID());
        addTaskByPriority(subtask);    // добавляем в множество подзадачу
        // добавляем задачу в память
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
            throw new NotFoundException("Подзадача не найдена по id = " + id);
        }
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void clearSubtasks() {
        for (Epic epic : epics.values()) {    // удаляем подзадачи из эпиков
            ArrayList<Subtask> listSubtaskByEpic = epic.getSubtasks();
            listSubtaskByEpic.clear();
        }
        // очищаем из приоритетного множества подзадачи
        for (Task subtask : subtasks.values()) {
            prioritizedTasks.remove(subtask);
        }
        subtasks.clear();    // удаляем подзадачи
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        // Проверка прикрепленного эпика (эпик не должен содержать ид подзадач)
        if (subtasks.containsKey(newSubtask.getEpicId())) return;
        Subtask oldSubtask = subtasks.get(newSubtask.getId());
        // проверяем совпадают ли ключи эпиков в старой и новой подзадаче
        if (!oldSubtask.getEpicId().equals(newSubtask.getEpicId())) {
            // иначе удаляем старую подзадачу в старом эпике
            Epic oldEpic = epics.get(oldSubtask.getEpicId());
            ArrayList<Subtask> subtasksOldEpic = oldEpic.getSubtasks();
            subtasksOldEpic.remove(oldSubtask);
            oldEpic.setSubtasks(subtasksOldEpic);
            epics.put(oldEpic.getId(), oldEpic);
        }
        // заменяем задачу в приоритетном множестве на случай, если в newSubtask изменился приоритет
        addTaskByPriority(newSubtask);
        // обновляем новую подзадачу
        subtasks.put(newSubtask.getId(), newSubtask);
        updateEpic(epics.get(newSubtask.getEpicId()));
        historyManager.add(newSubtask);
    }

    @Override
    public void removeSubtask(int id) {
        if (subtasks.containsKey(id)) {
            // удаляем подзадачу из эпика и обновляем эпик
            int epicId = subtasks.get(id).getEpicId();
            Epic epicAttached = epics.get(epicId);
            ArrayList<Subtask> listSubtaskByEpic = epicAttached.getSubtasks();
            listSubtaskByEpic.remove(subtasks.get(id));
            epicAttached.setSubtasks(listSubtaskByEpic);
            epics.put(epicAttached.getId(), checkEpicStatus(epicAttached));
            // удаляем подзадачу из всех таблиц и списков
            prioritizedTasks.remove(subtasks.get(id));
            subtasks.remove(id);
            historyManager.remove(id);
        } else {
            throw new ManagerSaveException("Подзадачи с id = " + id + " для удаления не существует!");
        }
    }

    @Override
    public List<Subtask> getListSubtasksFromEpic(int epicID) {
        return getSubtaskList().stream()
                .filter(subtask -> subtask.getEpicId() == epicID)
                .collect(Collectors.toList());

    }

    /* допущение при создании эпика - эпик создается с пустым списком подзадач.
    на этапе создания эпика в нем еще нет подзадач, иначе произойдет зацикливание:
    если дать возможность во время создания эпика еще и создавать подзадачу,
    то во время создания эпика будет создаваться подзадача, которая будет ссылаться на еще не созданный эпик. */
    @Override
    public Epic createEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            throw new ValidationException("Эпик с id=" + epic.getId() + " уже создан!, " +
                    "для обновления Эпика необходимо обновлять привязанные к нему подзадачи");
        }
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
            throw new NotFoundException("Эпик с id = " + id + " не найден");
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
        if (epics.containsKey(id)) {
            // при удалении эпика удаляем связанные с ним подзадачи
            Epic epic = epics.get(id);
            ArrayList<Subtask> subtasksFromEpic = epic.getSubtasks();
            for (Subtask subtask : subtasksFromEpic) {
                subtasks.remove(subtask.getId());
            }
            epics.remove(id);    // удаляем эпик
            historyManager.remove(id);    // удаляем из истории
        } else {
            throw new ManagerSaveException("Подзадачи с id = " + id + " для удаления не существует!");
        }
    }

    @Override
    public void updateEpic(Epic newEpic) {
        // Проверка прикрепленых подзадач (id подзадач не должны содержать id эпиков)
        for (Subtask newSubtask : newEpic.getSubtasks()) {
            if (epics.containsKey(newSubtask.getId())) {
                throw new ValidationException("Обновляемый эпик равен одной из существующих подзадач!");
            }
        }
        Epic checkedEpic = checkEpicStatus(newEpic);    // проверяем корректность статуса эпика
        checkedEpic.calculateTimesEpic();    // обновляем время выполнения эпика
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
        //historyManager.add(checkedEpic);
    }

    @Override
    public Epic checkEpicStatus(Epic epic) {
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

    @Override
    public List<Task> getPrioritizedTasks() {
        if (prioritizedTasks.isEmpty()) {    // во время первого запуска программы, нужно обновить лист prioritizedTasks
            List<Task> collect = tasksCollect.values().stream()
                    .filter(task -> task.getStartTime() != null)
                    // не учитываем эпики - они могут помешать добавлению новых задач и подзадач
                    // (из-за своего периода = периоду от первой до последней подзадачи)
                    // по факту этот период не равен времени выполнения всего эпика
                    .filter(task -> task.getTaskType() != Type.EPIC)
                    .collect(Collectors.toList());
            prioritizedTasks.addAll(collect);
        }
        return new ArrayList<>(prioritizedTasks);
    }


    private void addTaskByPriority(Task task) {
        if (task.getStartTime() != null && task.getTaskType() != Type.EPIC) {
            // в стриме проходимся по всем уже записанным задачам, если метод isCrossedTasks() вернет true
            // то задача попадет в итоговый emptyList. Далее если emptyList не пустой, то есть пересечение по времени
            // соответственно такую задачу нельзя добавлять в приоритезированные
            List<Task> emptyList = getPrioritizedTasks().stream()
                    .filter(task1 -> task1.getStartTime() != null)
                    .filter(checkedTask -> isCrossedTasks(checkedTask, task))
                    .filter(checkedTask -> checkedTask.getId() != task.getId())    // исключаем одинаковые задачи
                    .collect(Collectors.toList());
            if (emptyList.isEmpty()) prioritizedTasks.add(task);
            else {
                identifier--;    // если создание задачи отменяется, то возвращать идентификатор в прежнее состояние
                throw new ValidationException("Имеется пересечение по времени выполнения с другими задачами!");
            }
        }
    }

    // если две задачи пересекаются по времени выполнения возвращаем true
    private boolean isCrossedTasks(Task task1, Task task2) {
        LocalDateTime t1 = task1.getStartTime();
        LocalDateTime t2 = t1.plusMinutes(task1.getDurationToMinutes());
        LocalDateTime t3 = task2.getStartTime();
        LocalDateTime t4 = t3.plusMinutes(task2.getDurationToMinutes());
        if (t1.equals(t3) || t2.equals(t4)) return true;
        if (t1.isAfter(t3) && t1.isBefore(t4)) return true;
        return t1.isBefore(t3) && t2.isAfter(t3);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryTaskManager that = (InMemoryTaskManager) o;
        return Objects.equals(tasks, that.tasks) &&
                Objects.equals(subtasks, that.subtasks) &&
                Objects.equals(epics, that.epics) &&
                Objects.equals(historyManager, that.historyManager) &&
                Objects.equals(tasksCollect, that.tasksCollect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tasks, subtasks, epics, historyManager, tasksCollect);
    }
}
