package model;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {

    private ArrayList<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
        this.subtasks = new ArrayList<>();
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtasks, epic.subtasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasks);
    }

    @Override
    public String toString() {
        String result = "Epic{" +
                "name='" + name + '\'';;
        if(description != null){
            result = result + ", description.length = '" + description.length() + '\'';
        }else{
            result = result + ", description = null'" + '\'';
        }
        result = result + ", status=" + status + ", id=" + id + ", ";
        if (subtasks != null){
            result = result + "subtasks.size = " + subtasks.size() +'}';
        }else{
            result = result + "subtasks.size = null " +'}';
        }
        return result;
    }
}
