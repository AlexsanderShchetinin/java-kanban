package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
public class Manager {

    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Subtask> subtasks;
    private final HashMap<Integer, Epic> epics;
    private static int identifier;

    public Manager() {
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
        identifier = 0;
    }


    private int generateID(){
        return ++identifier;
    }

    public Task createTask(Task task) {
        task.setId(generateID());
        tasks.put(task.getId(), task);
        return task;
    }

    public void getTasksList() {
        for (Task task : tasks.values()) {
            System.out.println(task);
        }
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void updateTask(Task newTask) {
        tasks.remove(newTask.getId());
        tasks.put(newTask.getId(), newTask);
    }

    public void removeTask(int id) {
        tasks.remove(id);
    }

    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(generateID());
        Epic epicBySubtask = subtask.getEpic();
        if(epicBySubtask.getSubtasks() != null){
            if (!(epicBySubtask.getSubtasks().contains(subtask))){
                // если в эпике нет подзадачи - добавляем
                ArrayList<Subtask> listSubtask = epicBySubtask.getSubtasks();
                listSubtask.add(subtask);
                epicBySubtask.setSubtasks(listSubtask);
            }
        }else{
            // если нет ни одной подзадачи в эпике - тоже добавляем
            ArrayList<Subtask> listSubtask = new ArrayList<>();
            listSubtask.add(subtask);
            epicBySubtask.setSubtasks(listSubtask);
        }
        Epic updateEpic = checkEpicStatus(epicBySubtask);    // проверяем статус эпика
        epics.remove(updateEpic.getId());
        epics.put(updateEpic.getId(), updateEpic);
        subtasks.put(subtask.getId(), subtask);
        return subtask;
    }

    public void getSubtaskList(){
        for (Subtask subtask : subtasks.values()) {
            System.out.println(subtask);
        }
    }

    public Subtask getSubtask(int id){
        return subtasks.get(id);
    }

    public void clearSubtasks(){
        for (Epic epic : epics.values()) {    // удаляем подзадачи из эпиков
            ArrayList<Subtask> listSubtaskByEpic = epic.getSubtasks();
            listSubtaskByEpic.clear();
            epic.setSubtasks(listSubtaskByEpic);
            epics.remove(epic.getId());
            epics.put(epic.getId(), checkEpicStatus(epic));
        }
        subtasks.clear();    // удаляем подзадачи
    }

    public void updateSubtask (Subtask newSubtask){
        // если в новой подзадаче в эпике не добавлена подзадача - добавляем
        if(!(newSubtask.getEpic().getSubtasks().contains(newSubtask))){
            ArrayList<Subtask> subtasksNewEpic = newSubtask.getEpic().getSubtasks();
            subtasksNewEpic.add(newSubtask);
            newSubtask.getEpic().setSubtasks(subtasksNewEpic);
        }
        Subtask oldSubtask = subtasks.get(newSubtask.getId());
        // проверяем совпадают ли ключи эпиков в старой и новой подзадаче
        if(oldSubtask.getEpic().getId() == newSubtask.getEpic().getId()){
            // если эпики не меняли - просто обновляем подзадачу и связанный эпик
            subtasks.remove(newSubtask.getId());
            subtasks.put(newSubtask.getId(), newSubtask);
            Epic epic = checkEpicStatus(newSubtask.getEpic());    // обновляем статус эпика
            epics.remove(epic.getId());
            epics.put(epic.getId(), epic);
        }else{    // иначе удаляем старую подзадачу в старом эпике
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

    public void removeSubtask(int id) {
        // удаляем подзадачу из эпика и обновляем эпик
        Epic epicAttached = subtasks.get(id).getEpic();
        ArrayList<Subtask> listSubtaskByEpic = epicAttached.getSubtasks();
        listSubtaskByEpic.remove(subtasks.get(id));
        epicAttached.setSubtasks(listSubtaskByEpic);
        epics.remove(epicAttached.getId());
        epics.put(epicAttached.getId(), checkEpicStatus(epicAttached));
        // удаляем подзадачу
        subtasks.remove(id);
    }

    public void getListSubtasksFromEpic (Epic epic){
        ArrayList<Subtask> attachedSubtasks = epics.get(epic.getId()).getSubtasks();
        System.out.println("для " + epic + ":");
        for (Subtask subtask : attachedSubtasks) {
            System.out.println(subtask);
        }
    }


    /* допущение при создании эпика - эпик создается с пустым списком подзадач.
    на этапе создания эпика в нем еще нет подзадач, иначе произойдет зацикливание:
    если дать возможность во время создания эпика еще и создавать подзадачу,
    то во время создания эпика будет создаваться подзадача, которая будет ссылаться на еще не созданный эпик. */
    public Epic createEpic(Epic epic) {
        epic.setId(generateID());
        Epic newEpic = checkEpicStatus(epic);    // проверяем статус эпика и возвращаем корректный
        epics.put(newEpic.getId(), newEpic);
        return newEpic;
    }

    public void getEpicList(){
        for (Epic epic : epics.values()) {
            System.out.println(epic);
        }
    }

    public Epic getEpic(int id){
        return epics.get(id);
    }

    public void clearEpics(){
        // если удаляются все эпики то соответственно и удаляются все подзадачи
        clearSubtasks();
        epics.clear();    // удаляем все эпики
    }

    public void removeEpic(int id){
        // при удалении эпика удаляем связанные с ним подзадачи
        Epic epic = epics.get(id);
        ArrayList<Subtask> subtasksFromEpic = epic.getSubtasks();
        for (Subtask subtask : subtasksFromEpic) {
            subtasks.remove(subtask.getId());
        }
        epics.remove(id);    // удаляем эпик
    }

    public void updateEpic(Epic newEpic){
        Epic checkedEpic = checkEpicStatus(newEpic);    // проверяем корректность статуса эпика
        /* при обновлении эпика всегда список подзадач будет одинаковый в старом и новом эпике
        так как при объявлении конструктора эпика нет возможности вручную изменять список подзадач.
        Это специально сделано чтобы пользователь не смог скорректировать в эпике список подзадач.*/

        // при обновлении эпика обновляем все подзадачи связанные с ним - меняем в них эпик.
        ArrayList<Subtask> attachedSubtasks = epics.get(newEpic.getId()).getSubtasks();
        for (Subtask subtask : attachedSubtasks) {
            subtask.setEpic(newEpic);
            subtasks.remove(subtask.getId());
            subtasks.put(subtask.getId(), subtask);
        }
        // после этого обновляем сам эпик
        epics.remove(checkedEpic.getId());
        epics.put(checkedEpic.getId(), checkedEpic);
    }

    private Epic checkEpicStatus(Epic epic){
        ArrayList<Subtask> subtaskFromEpic = epic.getSubtasks();
        if(subtaskFromEpic != null){
            int SubtasksNumber = epic.getSubtasks().size();
            if(SubtasksNumber == 0){
                epic.setStatus(TaskStatus.NEW);
                return epic;
            }
            for (Subtask subtask : epic.getSubtasks()) {
                if (subtask.getStatus() == TaskStatus.NEW && SubtasksNumber == epic.getSubtasks().size()) {
                    epic.setStatus(TaskStatus.NEW);
                }
                else if (subtask.getStatus() == TaskStatus.DONE) {
                    --SubtasksNumber;
                    if (SubtasksNumber == 0) {
                        epic.setStatus(TaskStatus.DONE);
                        break;
                    }else{
                        epic.setStatus(TaskStatus.IN_PROGRESS);
                    }
                }
                else if (subtask.getStatus() == TaskStatus.IN_PROGRESS) {
                    epic.setStatus(TaskStatus.IN_PROGRESS);
                    break;
                }
            }
        }else{
            epic.setStatus(TaskStatus.NEW);
            return epic;
        }
        return epic;
    }
}
