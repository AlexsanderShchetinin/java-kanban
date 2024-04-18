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
class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    File file = new File("tasksAndHistoryFile.csv");
    File emptyFile = File.createTempFile("Empty file", ".csv");

    FileBackedTaskManagerTest() throws IOException {
    }

    @Override
    @Test
    @DisplayName("не должна создавать повторно эпики и задачи")
    void shouldNotCreateRepeatedTasks() {
        super.shouldNotCreateRepeatedTasks();
    }

    @BeforeEach
    void beforeEach() {
        taskManager = new FileBackedTaskManager(file);
    }

    @Test
    @DisplayName("Должна загружать резервную копию менеджера задач из пустого файла")
    void shouldLoadTaskManagerFromEmptyFile() {
        FileBackedTaskManager emptyManager = FileBackedTaskManager.loadFromFile(emptyFile);
        assertEquals(emptyManager, new FileBackedTaskManager(emptyFile),
                "Загрузка из пустого файла отличается от пустого менеджера FileBackedTaskManager");
    }

    @Test
    @DisplayName("Должна сохранять пустую резервную копию менеджера задач в файл")
    void shouldSaveEmptyTaskManagerToFile() {
        taskManager.clearTasks();
        FileBackedTaskManager emptyManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(emptyManager, new FileBackedTaskManager(emptyFile),
                "Сохранение и восстановление пустого менеджера FileBackedTaskManager не работает");
    }

    @Test
    @DisplayName("Должна сохранять резервную копию менеджера задач в файл и загружать её из файла")
    void shouldSaveAndLoadTaskManagerToFile() {
        // сохранение будет происходить каждый раз при вызове методов create
        taskManager.createTask(new Task("First Task", "Go to gym",
                "01.01.1970 00:01", 240));
        taskManager.createTask(new Task("Second Task", "Make kanban",
                "10.01.1970 10:01", 100000));
        Epic epic1 = taskManager.createEpic(new Epic("First Epic", "Learn to drive a car"));
        taskManager.createEpic(new Epic("Empty Epic", "Empty description"));
        taskManager.createSubtask(
                new Subtask("First Subtask", "Give a license", epic1.getId(),
                        "01.01.2001 00:00", 8505));
        taskManager.createSubtask(new Subtask("Second Subtask", "Buy a car", epic1.getId(),
                "01.01.2000 00:00", 8505));

        FileBackedTaskManager copy1 = FileBackedTaskManager.loadFromFile(file);

        assertEquals(copy1.historyManager, taskManager.historyManager,
                "Сохранение и восстановление менеджера FileBackedTaskManager после создания задач не работает");

        taskManager.getTask(1);
        taskManager.getSubtask(6);
        taskManager.getEpic(3);

        FileBackedTaskManager copy2 = FileBackedTaskManager.loadFromFile(file);

        assertEquals(copy2.historyManager, taskManager.historyManager,
                "Сохранение и восстановление менеджера FileBackedTaskManager при получении задач не работает");

        taskManager.removeTask(2);
        taskManager.removeEpic(4);
        taskManager.removeSubtask(5);

        FileBackedTaskManager copy3 = FileBackedTaskManager.loadFromFile(file);
        taskManager.updateEpic(epic1);

        assertEquals(copy3.historyManager, taskManager.historyManager,
                "Сохранение и восстановление менеджера FileBackedTaskManager после удаления задач не работает");
    }

    @Override
    @Test
    @DisplayName("не должна добавлять в эпик список из своего же эпика")
    void shouldNotEpicAddedToItself() {
        super.shouldNotEpicAddedToItself();
    }

    @Override
    @Test
    @DisplayName("не должна прикреплять подзадачу к подзадаче в привязанный эпик")
    void shouldNotSubtaskAddedToItsEpic() {
        super.shouldNotSubtaskAddedToItsEpic();
    }

    @Override
    @Test
    @DisplayName("должна корректно работать с задачами")
    void shouldCreateGetUpdateAndRemoveTasks() {
        super.shouldCreateGetUpdateAndRemoveTasks();
    }

    @Override
    @Test
    @DisplayName("должна корректно работать с эпиками и подзадачами")
    void shouldCreateGetAndRemoveSubtaskEndEpic() {
        super.shouldCreateGetAndRemoveSubtaskEndEpic();
    }

    @Override
    @Test
    @DisplayName("должна корректно удалять подзадачи")
    void shouldSubtaskRemove() {
        super.shouldSubtaskRemove();
    }

    @Override
    @Test
    @DisplayName("должна рассчитывать статусы и время выполнения у эпиков")
    void shouldCalculateEpicStatusAnd() {
        super.shouldCalculateEpicStatusAnd();
    }

    @Override
    @Test
    @DisplayName("Должна проверять пересечение интервалов приоритизированных задач")
    void shouldCheckTaskTimeInterval() {
        super.shouldCheckTaskTimeInterval();
    }
}