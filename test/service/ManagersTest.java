package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void checkInitialManagers(){
        TaskManager aDefault = Managers.getDefault();
        HistoryManager defaultHistory = Managers.getDefaultHistory();

        assertNotNull(defaultHistory.getHistory(), "История менеджера пустая");
        assertNotNull(aDefault.getHistory(), "В менеждере задач история пустая ");
        assertNotNull(aDefault.getTasksList(), "Список задач возвращает null ");
        assertNotNull(aDefault.getSubtaskList(), "Список подзадач возвращает null ");
        assertNotNull(aDefault.getEpicList(), "Список эпиков возвращает null ");
    }

}