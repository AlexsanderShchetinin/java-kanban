package service;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Резервная копия менеджера задач")
class FileBackedTaskManagerTest {

    FileBackedTaskManager fileBackedTaskManager;
    File file = new File("tasksAndHistoryFile.csv");
    File emptyFile = File.createTempFile("Empty file", ".csv");

    FileBackedTaskManagerTest() throws IOException {
    }

    @BeforeEach
    void beforeEach() {
        fileBackedTaskManager = new FileBackedTaskManager();
    }

    @Test
    @DisplayName("Должна загружать резервную копию менеджера задач из пустого файла")
    void shouldLoadTaskManagerFromEmptyFile() {
        FileBackedTaskManager emptyManager = FileBackedTaskManager.loadFromFile(emptyFile);
        assertEquals(emptyManager, new FileBackedTaskManager(),
                "Загрузка из пустого файла отличается от пустого менеджера FileBackedTaskManager");
    }

    @Test
    @DisplayName("Должна сохранять пустую резервную копию менеджера задач в файл")
    void shouldSaveEmptyTaskManagerToFile() {
        fileBackedTaskManager.save();
        FileBackedTaskManager emptyManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(emptyManager, new FileBackedTaskManager(),
                "Сохранение и восстановление пустого менеджера FileBackedTaskManager не работает");
    }

    @Test
    @DisplayName("Должна сохранять резервную копию менеджера задач в файл и загружать её из файла")
    void shouldSaveAndLoadTaskManagerToFile() {
        // сохранение будет происходить каждый раз при вызове методов create
        fileBackedTaskManager.createTask(new Task("First Task", "Go to gym"));
        fileBackedTaskManager.createTask(new Task("Second Task", "Make kanban"));
        Epic epic1 = fileBackedTaskManager.createEpic(new Epic("First Epic", "Learn to drive a car"));
        Epic epic2 = fileBackedTaskManager.createEpic(new Epic("Empty Epic", "Empty description"));
        fileBackedTaskManager.createSubtask(
                new Subtask("First Subtask", "Give a license", epic1.getId()));
        fileBackedTaskManager.createSubtask(new Subtask("Second Subtask", "Buy a car", epic1.getId()));

        FileBackedTaskManager copy1 = FileBackedTaskManager.loadFromFile(file);

        assertEquals(copy1.historyManager, fileBackedTaskManager.historyManager,
                "Сохранение и восстановление менеджера FileBackedTaskManager после создания задач не работает");

        fileBackedTaskManager.getTask(1);
        fileBackedTaskManager.getSubtask(6);
        fileBackedTaskManager.getEpic(3);

        FileBackedTaskManager copy2 = FileBackedTaskManager.loadFromFile(file);

        assertEquals(copy2.historyManager, fileBackedTaskManager.historyManager,
                "Сохранение и восстановление менеджера FileBackedTaskManager при получении задач не работает");

        fileBackedTaskManager.removeTask(2);
        fileBackedTaskManager.removeEpic(4);
        fileBackedTaskManager.removeSubtask(5);

        FileBackedTaskManager copy3 = FileBackedTaskManager.loadFromFile(file);
        fileBackedTaskManager.save();

        assertEquals(copy3.historyManager, fileBackedTaskManager.historyManager,
                "Сохранение и восстановление менеджера FileBackedTaskManager после удаления задач не работает");

    }
}