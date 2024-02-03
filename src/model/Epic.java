package model;

import java.util.ArrayList;

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
        for (Subtask subtask : subtasks) {
            if(subtask.getId() == this.id) return;    // id эпика и подзадачи должны различаться
        }
        this.subtasks = subtasks;
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
