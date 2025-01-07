package fra.uas.controller;

import fra.uas.model.TaskList;
import fra.uas.service.TaskListService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

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

}
