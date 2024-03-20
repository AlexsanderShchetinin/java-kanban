package model;

public class Subtask extends Task {

    private Epic epic;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
    }


    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        if (epic.getId() == this.id) return;    // id эпика и подзадачи должны различаться
        this.epic = epic;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + name + '\''
                + ", description='" + description + '\''
                + ", status=" + status + '\''
                + ", id=" + id + '}' + '\''
                + ", epic=" + epic.getName() + '}';

    }
}
