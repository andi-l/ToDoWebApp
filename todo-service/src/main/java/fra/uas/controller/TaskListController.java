package fra.uas.controller;

import fra.uas.model.Task;
import fra.uas.model.TaskInput;
import fra.uas.model.TaskList;
import fra.uas.service.TaskListService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TaskListController {
    private final TaskListService taskListService;

    public TaskListController(TaskListService taskListService) {
        this.taskListService = taskListService;
    }

    // Retrieves all TaskLists
    @SchemaMapping(typeName = "Query", field = "getAllTaskLists")
    public List<TaskList> getAllTaskLists() {
        return taskListService.getAllTaskLists();
    }

    // Retrieves a specific TaskList by ID
    @SchemaMapping(typeName = "Query", field = "getTaskListById")
    public TaskList getTaskListById(@Argument Long id) {
        return taskListService.getTaskListById(id);
    }

    // Creates a new TaskList with no tasks
    @SchemaMapping(typeName = "Mutation", field = "createTaskList")
    public TaskList createTaskList(@Argument String username, @Argument String title) {
        return taskListService.createTaskList(username, title);
    }

    // Creates a new TaskList with  tasks
    @SchemaMapping(typeName = "Mutation", field = "createTaskListWithTasks")
    public TaskList createTaskListWithTasks(@Argument String username, @Argument String title, @Argument List<TaskInput> tasks) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("TaskList username cannot be null or empty");
        }
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("Tasks cannot be null or empty");
        }
        tasks.forEach(task -> {
            if (task.getUsername() == null || task.getUsername().isEmpty()) {
                throw new IllegalArgumentException("Task username cannot be null or empty: " + task);
            }
            if (task.getTitle() == null || task.getTitle().isEmpty()) {
                throw new IllegalArgumentException("Task title cannot be null or empty: " + task);
            }
        });

        TaskList taskList = new TaskList(username, LocalDate.now(), title, List.of());
        List<Task> taskEntities = tasks.stream()
                .map(input -> new Task(
                        input.getUsername(),
                        input.getTitle(),
                        input.getTaskDescription(),
                        input.isCompleted(),
                        LocalDate.now(),
                        input.getDueDate()
                ))
                .collect(Collectors.toList());

        taskList.setTasks(taskEntities);

        return taskListService.createTaskListWithTasks(username, title, taskEntities);
    }
}
