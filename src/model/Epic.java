package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class Epic extends Task {


    public Epic(String name, String description) {
        super(name, description);
        this.subtasks = new ArrayList<>();
        this.taskType = Type.EPIC;
    }


    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        for (Subtask subtask : subtasks) {
            if (subtask.getId() == this.id) return;    // id эпика и подзадачи должны различаться
        }
        this.subtasks = subtasks;
    }

    private void calculateTimesEpic() {
        final LocalDateTime[] endTime = new LocalDateTime[1];
        Optional<LocalDateTime> minStartTime = subtasks.stream()
                .map(subtask -> subtask.startTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder());
        Optional<LocalDateTime> maxEndTime = subtasks.stream()
                .map(subtask -> subtask.startTime.plus(subtask.duration))
                .max(Comparator.naturalOrder());
        minStartTime.ifPresent(localDateTime -> startTime = localDateTime);

        maxEndTime.ifPresent(localDateTime -> endTime[0] = localDateTime);
        duration = Duration.between(startTime, endTime[0]);
    }

    @Override
    public LocalDateTime getEndTime() {
        calculateTimesEpic();
        return startTime.plus(duration);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + name + '\''
                + ", description='" + description + '\''
                + ", status=" + status + '\''
                + ", id=" + id + '\''
                + ", subtasks.size=" + subtasks.size() + '}';
    }
}
