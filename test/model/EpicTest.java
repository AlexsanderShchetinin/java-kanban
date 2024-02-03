package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void shouldEpicsAreEqualIfTheirIdAreEqual() {
        Epic epic = new Epic("Эпик1", "Описание1");
        Epic epicExpected = new Epic("Эпик2", "Описание2");
        epic.setId(999);
        epicExpected.setId(999);

        assertEquals(epicExpected, epic, "У эпиков разный ID");
    }

}