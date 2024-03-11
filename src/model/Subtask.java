package model;

public class Subtask extends Task {

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
        this.taskType = Type.SUBTASK;
    }

    @Override
    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (epicId == this.id) return;    // id эпика и подзадачи должны различаться
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + name + '\''
                + ", description='" + description + '\''
                + ", status=" + status + '\''
                + ", id=" + id + '}' + '\''
                + ", epic=" + epicId + '}';

    }
}
