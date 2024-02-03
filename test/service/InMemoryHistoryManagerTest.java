package service;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    InMemoryHistoryManager historyManager;
    Task task;
    Subtask subtask;
    Epic epic;


    @BeforeEach
    void beforeEach(){
        historyManager = new InMemoryHistoryManager();
        task = new Task("Задача", "Описание задачи");
        epic = new Epic("Эпик", "описание эпика");
        subtask = new Subtask("Подзадача", "описание подзадачи", epic);
    }

    @Test
    void add() {
        assertNotNull(historyManager.getHistory(), "История не проинициализирована.");
        final List<Task> history = historyManager.getHistory();
        for (int i = 1; i < 8; i += 3) {
            historyManager.add(task);
            assertEquals(i, history.size(), "Задача не добавляется в историю.");
            historyManager.add(subtask);
            assertEquals(i+1, history.size(), "Подзадача не добавляется в историю.");
            historyManager.add(epic);
            assertEquals(i+2, history.size(), "Эпик не добавляется в историю.");
        }

        historyManager.add(task);
        assertEquals(10, history.size(), "Задача не добавляется в историю.");
        historyManager.add(subtask);
        assertEquals(subtask, history.get(0), "Первый объект истории не удаляется");
        assertEquals(10, history.size(), "В менеджере истории больше 10 записей.");

    }

}