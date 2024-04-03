package service;

import exception.ManagerSaveException;
import exception.ValidationException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;


    @Test
    @DisplayName("не должна добавлять в эпик список из своего же эпика")
    void shouldNotEpicAddedToItself() {
        // Создаем эпик с пустым списком подзадач
        final Epic epic1 = taskManager.createEpic(new Epic("Эпик", "Описание эпика"));
        // Создаем отдельную подзадачу и меняем в ней id на id эпика
        final Subtask errorSubtask = new Subtask("Подзадача", "с id равным id эпика", epic1.getId());
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
                taskManager.createEpic(new Epic("Эпик", "Описание Эпика")).getId(),
                "10.03.2024 09:10", 650);
        Subtask returnedSubtask = taskManager.createSubtask(subtask);
        // Создаем новый эпик и устанавливаем в нем id равное id подзадачи
        Epic errorEpic = new Epic("Эпик", "Описание Эпика");
        errorEpic.setId(2);
        // меняем id связанного эпика на id подзадачи
        returnedSubtask.setId(2);

        try {
            // пытаемся создать подзадачу с привязанным эпиком у которго id равно id подзадачи
            Subtask subtask1 = taskManager.createSubtask(new Subtask("Подзадача2",
                    "Описание подзадачи 2", errorEpic.getId()));
            // пытаемся обновить подзадачу taskManager в эпике которого привязан id == id подзадачи
            Subtask expectedSubtask = taskManager.getSubtask(2);    // подзадача до обновления
            taskManager.updateSubtask(returnedSubtask);
            Subtask subtaskAfterUpdate = taskManager.getSubtask(2);    // подзадача после обновления

            // Проверяем что подзадача с некорректным эпиком не создается.
            assertNull(subtask1, "подзадача создана с некорректным эпиком");
            // Проверяем что обновление не проходит
            assertEquals(subtaskAfterUpdate, expectedSubtask, "Подзадача обновлена с некорректным эпиком");
        } catch (ManagerSaveException e) {
            assertEquals("Подзадача связана с некорректным эпиком", e.getMessage(),
                    "Не удалось отловить исключение при смене id подзадачи");
        }
    }

    @Test
    @DisplayName("не должна создавать повторно эпики и задачи")
    void shouldNotCreateRepeatedTasks() {
        Task task = taskManager.createTask(new Task("Test addNewTask", "Test addNewTask description",
                "01.04.2024 12:00", 300));
        Epic epic = taskManager.createEpic(new Epic("Epic", "Test epic description"));
        final Subtask subtask = taskManager.createSubtask(
                new Subtask("Test addNewSubtask", "Test subtask description", epic.getId(),
                        "01.04.2024 19:05", 55));

        assertThrows(ValidationException.class, () -> {
            taskManager.createTask(task);
        }, "Удалось повторно создать одну и ту же задачу");

        assertThrows(ValidationException.class, () -> {
            taskManager.createEpic(epic);
        }, "Удалось повторно создать один и тот же эпик");

        assertThrows(ValidationException.class, () -> {
            taskManager.createSubtask(subtask);
        }, "Удалось повторно создать одну и ту же подзадачу");
    }

    @Test
    @DisplayName("должна корректно работать с задачами")
    void shouldCreateGetUpdateAndRemoveTasks() {
        Task task = new Task("Test addNewTask", "Test addNewTask description",
                "01.04.2024 12:00", 300);
        final Task returned = taskManager.createTask(task);

        final Task savedTask = taskManager.getTask(returned.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasksList();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач (должно быть 1).");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");

        task.setName("OtherNameTask");

        final Task returned2 = taskManager.createTask(new Task("New_Task", "Random3517"));
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
        Epic epic = taskManager.createEpic(new Epic("Epic", "Test epic description"));
        final Subtask returnedSubtask = taskManager.createSubtask(
                new Subtask("Test addNewSubtask", "Test subtask description", epic.getId(),
                        "01.04.2024 19:05", 55));
        final Subtask savedSubtask = taskManager.getSubtask(returnedSubtask.getId());

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(returnedSubtask, savedSubtask, "Подзадачи не совпадают.");

        final List<Subtask> subtasks = taskManager.getSubtaskList();
        assertNotNull(subtasks, "Подзадачи не возвращаются в методе getSubtaskList().");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(returnedSubtask, subtasks.get(0), "Подзадачи не совпадают.");

        final Epic savedEpic = taskManager.getEpic(epic.getId());

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getEpicList();
        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");

        epic.setName("NewNameEpic");
        taskManager.updateEpic(epic);
        assertEquals("NewNameEpic", taskManager.getEpic(epic.getId()).getName(),
                "При обновлении эпика имена не равны");

        returnedSubtask.setName("NewNameSubtask");
        taskManager.updateSubtask(returnedSubtask);
        assertEquals("NewNameSubtask", taskManager.getSubtask(returnedSubtask.getId()).getName(),
                "При обновлении подзадачи имена не равны");

        taskManager.removeSubtask(returnedSubtask.getId());
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
            taskManager.createSubtask(new Subtask("Подзадача " + i, "Описание " + i, epic.getId(),
                    "02.04.2024 1" + i + ":00", 33));
        }
        final Epic epic2 = taskManager.createEpic(new Epic("Имя эпика", "Описание Эпика"));
        for (int i = 1; i < 6; i++) {
            taskManager.createSubtask(new Subtask("Подзадача " + i, "Описание " + i, epic2.getId(),
                    "03.04.2024 1" + i + ":00", 23));
        }
        Subtask subtask1 = taskManager.getListSubtasksFromEpic(epic.getId()).get(0);

        taskManager.removeSubtask(subtask1.getId());

        assertEquals(4, epic.getSubtasks().size(), "Подзадача не удаляется.");
        assertNotEquals("Подзадача 1", taskManager.getListSubtasksFromEpic(epic.getId()).get(0).getName(),
                "Удаляется неправильная подзадача");
        assertEquals("Подзадача 2", taskManager.getListSubtasksFromEpic(epic.getId()).get(0).getName(),
                "Удаляется неправильная подзадача");

        taskManager.clearSubtasks();

        assertEquals(0, epic.getSubtasks().size(), "Все подзадачи не удаляются.");
    }

    @Test
    @DisplayName("должна рассчитывать статусы у эпиков")
    void shouldCalculateEpicStatus() {
        // предварительное создание эпиков и подзадач с временем начала (с приоритетом)
        // id эпиков - 1, 11, 21, 31, 41 и 51
        // id подзадач в промежутках
        for (int i = 1; i < 6; i++) {
            taskManager.createEpic(new Epic("Epic_" + i, "Описание эпика №" + i));
            for (int j = 1; j < 10; j++) {
                taskManager.createSubtask(new Subtask("Subtask_" + i + "." + j, "Description" + "." + j,
                        taskManager.getEpic(10 * (i - 1) + 1).getId(),
                        "0" + i + ".01.2024 1" + j + ":01", 58));
            }
        }
        Epic lastEpic = taskManager.createEpic(new Epic("Крайний эпик", "Описание крайнего эпика"));
        for (int j = 1; j < 6; j++) {
            taskManager.createSubtask(new Subtask("CheckSubtask" + j, "CheckDescription" + j,
                    lastEpic.getId(), "09.01.2024 1" + j + ":01", 29));
        }

        // расчет статуса Epic
        // 1. Все подзадачи со статусом NEW
        Epic calculateEpic1 = taskManager.checkEpicStatus(lastEpic);
        assertEquals(TaskStatus.NEW, calculateEpic1.getStatus(), "ошибка при расчете статуса Эпика NEW.");

        // 2. Все подзадачи со статусом DONE
        taskManager.getListSubtasksFromEpic(1)
                .forEach(subtask -> subtask.setStatus(TaskStatus.DONE));
        Epic calculateEpic2 = taskManager.checkEpicStatus(taskManager.getEpic(1));
        assertEquals(TaskStatus.DONE, calculateEpic2.getStatus(), "ошибка при расчете статуса Эпика DONE.");

        // 3. Подзадачи со статусами NEW и DONE
        taskManager.getListSubtasksFromEpic(11).stream()
                .filter(subtask -> subtask.getId() > 15)    // оставляем 4 подзадачи в статусе NEW, остальные меняем
                .forEach(subtask -> subtask.setStatus(TaskStatus.DONE));
        Epic calculateEpic3 = taskManager.checkEpicStatus(taskManager.getEpic(11));
        assertEquals(TaskStatus.IN_PROGRESS, calculateEpic3.getStatus(),
                "ошибка при первом расчете статуса Эпика IN_PROGRESS.");

        // 4. Подзадачи со статусом IN_PROGRESS
        taskManager.getListSubtasksFromEpic(21)
                .forEach(subtask -> subtask.setStatus(TaskStatus.IN_PROGRESS));
        Epic calculateEpic4 = taskManager.checkEpicStatus(taskManager.getEpic(21));
        assertEquals(TaskStatus.IN_PROGRESS, calculateEpic4.getStatus(),
                "ошибка при втором расчете статуса Эпика IN_PROGRESS.");
    }

    @Test
    @DisplayName("Должна проверять пересечение интервалов задач с приоритетом")
    void shouldCheckTaskTimeInterval() {
        // Создание задач с временем начала (с приоритетом) и без приоритета
        // id задач с 1 по 10
        // нечетные - приоритезированные, четные - без начального времени
        // при создании приоритетных задач уже проверяется что интервалы не пересекаются
        for (int i = 1; i < 11; i += 2) {
            taskManager.createTask(new Task("Task_" + i, "Descript_" + i,
                    (i + 10) + ".01.2024 " + (i + 10) + ":01", 35));
            taskManager.createTask(new Task("Task_" + (i + 1), "Descript_" + (i + 1)));
        }

        // Добавим новые интервалы в задачи без начального времени
        // таким образом чтобы они приводили к исключениям по всем граничным и промежуточным условиям
        // 1. Время начала одной задачи равно времени начала другой
        LocalDateTime dateTime1 = LocalDateTime.of(2024, 1, 11, 11, 1);
        taskManager.getTask(2).setStartTime(dateTime1);
        assertThrows(ValidationException.class, () -> {
            taskManager.updateTask(taskManager.getTask(2));
        }, "Расчет пересечения интервалов не сработал при одинаковом времени начала задач");

        // 2. Время окончания у двух задач одинаковое
        LocalDateTime dateTime2 = LocalDateTime.of(2024, 1, 13, 13, 0);
        taskManager.getTask(4).setStartTime(dateTime2);
        taskManager.getTask(4).setDurationOfMinutes(36);
        assertThrows(ValidationException.class, () -> {
            taskManager.updateTask(taskManager.getTask(4));
        }, "Расчет пересечения интервалов не сработал при одинаковом времени окончания задач");

        // 3. Интервал одной задачи полностью лежит внутри интервала другой задачи
        LocalDateTime dateTime3 = LocalDateTime.of(2024, 1, 15, 14, 0);
        taskManager.getTask(6).setStartTime(dateTime3);
        taskManager.getTask(6).setDurationOfMinutes(360);
        assertThrows(ValidationException.class, () -> {
            taskManager.updateTask(taskManager.getTask(6));
        }, "Расчет пересечения интервалов не сработал при полном пересечении интервалов");

        // 3. Интервал одной задачи частично перекрывает интервала другой задачи
        LocalDateTime dateTime4 = LocalDateTime.of(2024, 1, 17, 17, 10);
        taskManager.getTask(8).setStartTime(dateTime4);
        taskManager.getTask(8).setDurationOfMinutes(360);
        assertThrows(ValidationException.class, () -> {
            taskManager.updateTask(taskManager.getTask(8));
        }, "Расчет пересечения интервалов не сработал при частичном пересечении интервалов");
    }

}