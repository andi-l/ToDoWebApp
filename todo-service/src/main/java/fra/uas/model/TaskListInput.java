package fra.uas.model;

import java.util.List;

public class TaskListInput {

    private Long id;
    private String title;
    private String username;
    private List<TaskInput> tasks;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<TaskInput> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskInput> tasks) {
        this.tasks = tasks;
    }
}
