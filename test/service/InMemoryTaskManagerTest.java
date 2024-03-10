package service;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Память менеджера задач")
class InMemoryTaskManagerTest {

    private static InMemoryTaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    @Test
    @DisplayName("не должна добавлять в эпик список из своего же эпика")
    void shouldNotEpicAddedToItself() {
        // Создаем эпик с пустым списком подзадач
        final Epic epic1 = taskManager.createEpic(new Epic("Эпик", "Описание эпика"));
        // Создаем отдельную подзадачу и меняем в ней id на id эпика
        final Subtask errorSubtask = new Subtask("Подзадача", "с id равным id эпика",
                taskManager.getEpic(epic1.getId()));
        errorSubtask.setId(epic1.getId());
        // и пробуем прикрепить ошибочную подзадачу к эпику
        ArrayList<Subtask> errorListSubtasks = new ArrayList<>();
        errorListSubtasks.add(errorSubtask);
        taskManager.getEpic(epic1.getId()).setSubtasks(errorListSubtasks);

        // Проверяем что ошибочная подзадача не прикрепилась к эпику
        assertTrue(taskManager.getEpic(epic1.getId()).getSubtasks().isEmpty(),
                "В эпике установлена подзадача с некорректным id");

        // Создаем ошибочный эпик, где id прикрепленной подзадачи такой же как и в эпике
        Epic epic = new Epic("Эпик для обновления", "Описание эпика для обновления");
        epic.setSubtasks(errorListSubtasks);
        epic.setId(1);
        // Пытаемся обновить такой эпик
        taskManager.updateEpic(epic1);
        // Проверяем что ошибочная подзадача не прикрепилась к эпику при обновлении
        assertTrue(taskManager.getEpic(1).getSubtasks().isEmpty(),
                "Удалось прикрепить подзадачу с некорректным id при обновлении эпика");
    }

    @Test
    @DisplayName("не должна прикреплять подзадачу к подзадаче в привязанный эпик")
    void shouldNotSubtaskAddedToItsEpic() {
        // Создаем эпик и подзадачу в taskManager
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи",
                taskManager.createEpic(new Epic("Эпик", "Описание Эпика")));
        Subtask returnedSubtask = taskManager.createSubtask(subtask);
        // Создаем новый эпик и устанавливаем в нем id равное id подзадачи
        Epic errorEpic = new Epic("Эпик", "Описание Эпика");
        errorEpic.setId(2);
        // меняем id связанного эпика на id подзадачи
        returnedSubtask.getEpic().setId(2);

        // пытаемся создать подзадачу с привязанным эпиком у которго id равно id подзадачи
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Подзадача2",
                "Описание подзадачи 2", errorEpic));
        // пытаемся обновить подзадачу taskManager в эпике которого привязан id == id подзадачи
        Subtask expectedSubtask = taskManager.getSubtask(2);    // подзадача до обновления
        taskManager.updateSubtask(returnedSubtask);
        Subtask subtaskAfterUpdate = taskManager.getSubtask(2);    // подзадача после обновления

        // Проверяем что подзадача с некорректным эпиком не создается.
        assertNull(subtask1, "подзадача создана с некорректным эпиком");
        // Проверяем что обновление не проходит
        assertEquals(subtaskAfterUpdate, expectedSubtask, "Подзадача обновлена с некорректным эпиком");
    }

    @Test
    @DisplayName("должна корректно работать с задачами")
    void shouldCreateGetUpdateAndRemoveTasks() {
        Task task = new Task("Test addNewTask", "Test addNewTask description");
        final Task returned = taskManager.createTask(task);

        final Task savedTask = taskManager.getTask(returned.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasksList();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач (должно быть 1).");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");

        task.setName("OtherNameTask");
        taskManager.updateTask(task);

        final Task returned2 = taskManager.createTask(task);
        final List<Task> tasks2 = taskManager.getTasksList();
        assertEquals(2, tasks2.size(), "Неверное количество задач (должно быть 2).");

        taskManager.removeTask(returned2.getId());
        final List<Task> tasks3 = taskManager.getTasksList();
        assertEquals(1, tasks3.size(), "Неверное количество задач (должно быть 1).");

        taskManager.clearTasks();
        final List<Task> tasks4 = taskManager.getTasksList();
        assertEquals(0, tasks4.size(), "Неверное количество задач (должно быть 0).");
        assertNotNull(tasks4, "Задачи не возвращаются.");
    }

    @Test
    @DisplayName("должна корректно работать с эпиками и подзадачами")
    void shouldCreateGetAndRemoveSubtaskEndEpic() {
        Epic epic = new Epic("Epic", "Test epic description");
        Subtask subtask = new Subtask("Test addNewSubtask", "Test subtask description", epic);
        final Subtask returnedSubtask = taskManager.createSubtask(subtask);
        final Subtask savedSubtask = taskManager.getSubtask(returnedSubtask.getId());

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");

        final List<Subtask> subtasks = taskManager.getSubtaskList();
        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.get(0), "Подзадачи не совпадают.");

        final Epic returnedEpic = taskManager.createEpic(epic);
        final Epic savedEpic = taskManager.getEpic(returnedEpic.getId());

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getEpicList();
        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");

        epic.setName("NewNameEpic");
        taskManager.updateEpic(epic);
        assertEquals("NewNameEpic", taskManager.getEpic(epic.getId()).getName(),
                "При обновлении эпика имена не равны");

        subtask.setName("NewNameSubtask");
        taskManager.updateSubtask(subtask);
        assertEquals("NewNameSubtask", taskManager.getSubtask(subtask.getId()).getName(),
                "При обновлении подзадачи имена не равны");

        taskManager.removeSubtask(subtask.getId());
        final List<Subtask> subtasks1 = taskManager.getSubtaskList();
        assertNotNull(subtasks1, "Подзадачи не возвращаются.");
        assertEquals(0, subtasks1.size(), "Неверное количество подзадач.");

        taskManager.removeEpic(epic.getId());
        final List<Epic> epics1 = taskManager.getEpicList();
        assertNotNull(epics1, "Эпики не возвращаются.");
        assertEquals(0, epics1.size(), "Неверное количество эпиков.");
    }

    @Test
    @DisplayName("должна корректно удалять подзадачи")
    void shouldSubtaskRemove() {
        final Epic epic = taskManager.createEpic(new Epic("Имя эпика", "Описание Эпика"));
        for (int i = 1; i < 6; i++) {
            taskManager.createSubtask(new Subtask("Подзадача " + i, "Описание " + i, epic));
        }
        Subtask subtask1 = taskManager.getListSubtasksFromEpic(epic).get(0);

        taskManager.removeSubtask(subtask1.getId());

        assertEquals(4, epic.getSubtasks().size(), "Подзадача не удаляется.");
        assertNotEquals("Подзадача 1", taskManager.getListSubtasksFromEpic(epic).get(0).getName(),
                "Удаляется неправильная подзадача");
        assertEquals("Подзадача 2", taskManager.getListSubtasksFromEpic(epic).get(0).getName(),
                "Удаляется неправильная подзадача");

        taskManager.clearSubtasks();

        assertEquals(0, epic.getSubtasks().size(), "Все подзадачи не удаляются.");
    }
}


