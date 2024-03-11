package service;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
@DisplayName("Память менеджера истории")
class InMemoryHistoryManagerTest {
    InMemoryHistoryManager historyManager;
    Task task;
    Subtask subtask;
    Epic epic;
    InMemoryTaskManager taskManager;


    @BeforeEach
    void beforeEach(){
        historyManager = new InMemoryHistoryManager();
        taskManager = new InMemoryTaskManager();
        task =  taskManager.createTask(new Task("Задача", "Описание задачи"));
        epic = taskManager.createEpic(new Epic("Эпик", "описание эпика"));
        subtask = taskManager.createSubtask(
                new Subtask("Подзадача", "описание подзадачи", epic.getId()));
    }

    @Test
    @DisplayName("Должна добавлять задачи без повторов")
    void shouldAddTasksWithoutRepeat() {
        assertNotNull(historyManager.getHistory(), "История не проинициализирована.");
        for (int i = 1; i < 5; i += 3) {
            historyManager.add(task);
            historyManager.add(subtask);
            historyManager.add(epic);
        }
        historyManager.add(subtask);
        assertEquals(3, historyManager.getHistory().size(),
                "Повторные задачи из истории не удаляются");
        historyManager.add(subtask);
        assertEquals(3, historyManager.getHistory().size(),
                "Повторные задачи из истории не удаляются");

        InMemoryHistoryManager historyManager2 = new InMemoryHistoryManager();
        historyManager2.add(task);
        historyManager2.add(task);
        historyManager2.add(task);
        assertEquals(1, historyManager2.getHistory().size(),
                "Повторные задачи из истории не удаляются (в истории одна запись)");
    }

    @Test
    @DisplayName("Должен удалять задачи из истории")
    void shouldRemoveTask(){
            historyManager.add(task);
            historyManager.add(subtask);
            historyManager.add(epic);

        historyManager.remove(subtask.getId());

        assertEquals(2, historyManager.getHistory().size(),
                "Из истории не удаляется запись");

        historyManager.remove(1001);

        assertEquals(2, historyManager.getHistory().size(),
                "Удаление задачи из истории работает некорректно с ошибочным id");


    }

}