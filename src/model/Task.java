package model;

import converter.TimeAdapter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

public class Task {

    protected String name;
    protected String description;
    protected TaskStatus status;
    protected int id;
    protected Integer epicId;
    protected ArrayList<Subtask> subtasks;
    protected Type taskType;
    protected Duration duration;
    protected LocalDateTime startTime;


    public Task(String name, String description, String startTime, long duration) {
        this.id = 0;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.taskType = Type.TASK;
        this.duration = Duration.ofMinutes(duration);
        this.startTime = LocalDateTime.parse(startTime, TimeAdapter.DATE_TIME_FORMAT_1);
    }

    public Task(String name, String description) {
        this.id = 0;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.taskType = Type.TASK;
        this.duration = Duration.ZERO;
        this.startTime = null;
    }

    public void setTaskType(Type type) {
        this.taskType = type;
    }

    public void setEmptySubtasks() {
        this.subtasks = null;
    }

    public String getDescription() {
        return description;
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

    public Integer getEpicId() {
        if (epicId == null) {
            return null;
        }
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public long getDurationToMinutes() {
        if (duration != null) {
            return duration.toMinutes();
        }
        return 0;
    }

    public void setDurationOfMinutes(long duration) {
        this.duration = Duration.ofMinutes(duration);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }


    public LocalDateTime getEndTime() {
        return Optional.of(startTime.plus(duration)).orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\''
                + ", description='" + description + '\''
                + ", status=" + status + '\''
                + ", id=" + id + '}';
    }

    public Type getTaskType() {
        return taskType;
    }

}
