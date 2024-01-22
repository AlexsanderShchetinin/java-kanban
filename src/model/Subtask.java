package model;

import java.util.Objects;

public class Subtask extends Task {

    private Epic epic;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return Objects.equals(epic, subtask.epic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epic);
    }

    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    @Override
    public String toString() {
        String result = "Subtask{" +
                "name='" + name + '\'';
        if(super.getDescription() != null){
            result = result + ", description.length='" + description.length() + '\'';
        }else{
            result = result + ", description = null'" + '\'';
        }
        result = result + ", status=" + status + ", id=" + id + ", epic=" + epic.getName() +'}';
        return result;
    }
}
