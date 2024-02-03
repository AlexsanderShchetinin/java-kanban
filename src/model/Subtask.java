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
        if(epic.getId() == this.id) return;    // id эпика и подзадачи должны различаться
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
