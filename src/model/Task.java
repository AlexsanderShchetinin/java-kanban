package model;

import java.util.Objects;

public class Task {

    private String name;
    private String description;
    private TaskStatus status;
    private int id;


    public Task(String name, String description, TaskStatus status){ // конструктор для задач и подзадач
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task(Task task){
        this.id = task.id;
        this.status = task.status;
        this.name = task.name;
        this.description = task.description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id &&
                Objects.equals(name, task.name) &&
                Objects.equals(description, task.description) &&
                status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, status, id);
    }

    @Override
    public String toString() {
        String result = "Task{" +
                "name='" + name + '\'';
        if(description != null){
            result = result + ", description.length='" + description.length() + '\'';
        }else{
            result = result + ", description = null'" + '\'';
        }
        result = result + ", status=" + status + ", id=" + id +'}';
        return result;
    }


}
