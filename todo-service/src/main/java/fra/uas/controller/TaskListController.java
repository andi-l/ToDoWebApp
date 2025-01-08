package fra.uas.controller;

import fra.uas.model.Task;
import fra.uas.model.TaskInput;
import fra.uas.model.TaskList;
import fra.uas.service.TaskListService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // Adds tasks to an existing TaskList
    @SchemaMapping(typeName = "Mutation", field = "addTasksToTaskList")
    public TaskList addTasksToTaskList(@Argument Long taskListId, @Argument List<TaskInput> tasks) {
        TaskList taskList = taskListService.getTaskListById(taskListId);
        List<Task> taskEntities = tasks.stream()
                .map(input -> new Task(
                        input.getUsername(),
                        input.getTitle(),
                        input.getTaskDescription(),
                        input.isCompleted(),
                        LocalDate.now(),
                        input.getDueDate()))
                .collect(Collectors.toList());
        return taskListService.addTasksToTaskList(taskListId, taskEntities);
    }

    // Updates the details of a TaskList, including tasks
    @SchemaMapping(typeName = "Mutation", field = "updateTaskList")
    public TaskList updateTaskList(
            @Argument Long taskListId,
            @Argument String username,
            @Argument String creationDate,
            @Argument String title,
            @Argument List<TaskInput> tasks
    ) {
        List<Task> updatedTasks = tasks.stream().map(taskInput -> {
            Task task = new Task();
            task.setId(taskInput.getId());
            task.setUsername(taskInput.getUsername());
            task.setTitle(taskInput.getTitle());
            task.setTaskDescription(taskInput.getTaskDescription());
            task.setCompleted(taskInput.isCompleted());
            task.setDueDate(taskInput.getDueDate());
            task.setCompletionDate(taskInput.isCompleted() ? LocalDate.now() : null);
            return task;
        }).collect(Collectors.toList());

        return taskListService.updateTaskList(taskListId, username, creationDate, updatedTasks, title);
    }

    // Retrieves tasks within a TaskList
    @SchemaMapping(typeName = "TaskList", field = "tasks")
    public List<Task> tasks(TaskList taskList) {
        return taskList.getTasks();
    }

    // Deletes a specific TaskList
    @SchemaMapping(typeName = "Mutation", field = "deleteTaskList")
    public ResponseEntity<String> deleteTaskList(@Argument Long taskListId) {
        boolean isDeleted = taskListService.deleteTaskList(taskListId);

        if (!isDeleted) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Task list cannot be deleted");
        }

        return ResponseEntity.ok("Task list deleted successfully");
    }

    // Deletes specific tasks from a TaskList
    @SchemaMapping(typeName = "Mutation", field = "deleteTasksFromTaskList")
    public TaskList deleteTasksFromTaskList(@Argument Long taskListId, @Argument List<Long> taskIds) {
        return taskListService.deleteTasksFromTaskList(taskListId, taskIds);
    }

    // Retrieves TaskLists sorted by creation date in ascending order
    @SchemaMapping(typeName = "Query", field = "getTaskListsSortedByCreationDateAsc")
    public List<TaskList> getTaskListsSortedByCreationDateAsc(@Argument String username) {
        return taskListService.getTaskListsSortedByCreationDateAsc(username);
    }

    // Retrieves TaskLists sorted by creation date in descending order
    @SchemaMapping(typeName = "Query", field = "getTaskListsSortedByCreationDateDesc")
    public List<TaskList> getTaskListsSortedByCreationDateDesc(@Argument String username) {
        return taskListService.getTaskListsSortedByCreationDateDesc(username);
    }

    // Retrieves TaskLists sorted by task count in ascending order
    @SchemaMapping(typeName = "Query", field = "getTaskListsSortedByTaskCountAsc")
    public List<TaskList> getTaskListsSortedByTaskCountAsc(@Argument String username) {
        return taskListService.getTaskListsSortedByTaskCountAsc(username);
    }

    // Retrieves TaskLists sorted by task count in descending order
    @SchemaMapping(typeName = "Query", field = "getTaskListsSortedByTaskCountDesc")
    public List<TaskList> getTaskListsSortedByTaskCountDesc(@Argument String username) {
        return taskListService.getTaskListsSortedByTaskCountDesc(username);
    }

    // Toggles the completion status of a task and updates its completion date
    @SchemaMapping(typeName = "Mutation", field = "toggleTaskCompletion")
    public Task toggleTaskCompletion(
            @Argument Long taskListId,
            @Argument Long taskId,
            @Argument boolean isCompleted) {
        return taskListService.toggleTaskCompletion(taskListId, taskId, isCompleted);
    }

    // Updates the details of a task, such as title, description, and due date
    @SchemaMapping(typeName = "Mutation", field = "updateTaskDetails")
    public Task updateTaskDetails(
            @Argument Long taskListId,
            @Argument Long taskId,
            @Argument String title,
            @Argument String description,
            @Argument String dueDate) {
        LocalDate parsedDueDate = dueDate != null ? LocalDate.parse(dueDate) : null;
        return taskListService.updateTaskDetails(taskListId, taskId, title, description, parsedDueDate);
    }

    // Updates the title of a TaskList
    @SchemaMapping(typeName = "Mutation", field = "updateTaskListTitle")
    public TaskList updateTaskListTitle(
            @Argument Long taskListId,
            @Argument String title) {
        return taskListService.updateTaskListTitle(taskListId, title);
    }

    // Retrieves TaskLists for a specific username
    @SchemaMapping(typeName = "Query", field = "getTaskListsByUsername")
    public List<TaskList> getTaskListsByUsername(@Argument String username) {
        return taskListService.getTaskListsByUsername(username);
    }
}
