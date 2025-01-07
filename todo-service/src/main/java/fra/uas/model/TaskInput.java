package fra.uas.model;

import java.time.LocalDate;

public class TaskInput {

    private Long id;
    private String username;
    private String title;
    private String taskDescription;
    private boolean completed;
    private LocalDate dueDate;
    private LocalDate completionDate;

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return "TaskInput{" +
                "username='" + username + '\'' +
                ", title='" + title + '\'' +
                ", taskDescription='" + taskDescription + '\'' +
                ", completed=" + completed +
                ", dueDate='" + dueDate + '\'' +
                '}';
    }
}
