package service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Память менеджера задач")
class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {


    @Override
    @Test
    @DisplayName("не должна создавать повторно эпики и задачи")
    void shouldNotCreateRepeatedTasks() {
        super.shouldNotCreateRepeatedTasks();
    }

    @BeforeEach
    void beforeEach() {
        taskManager = new InMemoryTaskManager();
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


