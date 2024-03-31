package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {

    @Test
    @DisplayName("Задачи должны равняться если их id равны")
    void shouldEpicsAreEqualIfTheirIdAreEqual() {
        Epic epic = new Epic("Эпик1", "Описание1");
        Epic epicExpected = new Epic("Эпик2", "Описание2");
        epic.setId(999);
        epicExpected.setId(999);

        assertEquals(epicExpected, epic, "У эпиков разный ID");
    }

}