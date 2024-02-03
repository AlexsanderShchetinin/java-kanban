package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryHistoryManager;
import service.InMemoryTaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void shouldTasksAreEqualIfTheirIdAreEqual() {
        Task task = new Task("Задача1", "Описание1");
        Task taskExpected = new Task("Задача2", "Описание2");
        task.setId(999);
        taskExpected.setId(999);

        assertEquals(taskExpected, task, "У задач разный ID");
    }



}