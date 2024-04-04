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

    public void calculateTimesEpic() {
        LocalDateTime[] endTime = new LocalDateTime[1];
        Optional<LocalDateTime> minStartTime = subtasks.stream()
                .map(subtask -> subtask.startTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder());
        Optional<LocalDateTime> maxEndTime = subtasks.stream()
                .map(subtask -> subtask.startTime.plus(subtask.duration))
                .max(Comparator.naturalOrder());
        startTime = minStartTime.orElse(null);
        endTime[0] = maxEndTime.orElse(null);
        if (startTime != null) {
            duration = Duration.between(startTime, endTime[0]);
        }
    }

    @Override
    public LocalDateTime getEndTime() {
        calculateTimesEpic();
        return super.getEndTime();
    }

    @Override
    public long getDurationToMinutes() {
        return super.getDurationToMinutes();
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
