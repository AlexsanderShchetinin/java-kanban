package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void shouldSubtasksAreEqualIfTheirIdAreEqual() {
        Epic epic = new Epic("Эпик", "Описание Эпика");
        Subtask subtask = new Subtask("Подзадача1", "Описание1", epic.getId());
        Subtask subtaskExpected = new Subtask("Подзадача2", "Описание2", epic.getId());
        subtask.setId(999);
        subtaskExpected.setId(999);
        System.out.println("Тест toString "+ subtask);

        assertEquals(subtaskExpected, subtask, "У подзадач задач разный ID");
    }
}