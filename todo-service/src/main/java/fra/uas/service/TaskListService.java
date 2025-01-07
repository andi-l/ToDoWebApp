package fra.uas.service;

import fra.uas.model.Task;
import fra.uas.model.TaskList;
import fra.uas.repository.TaskListRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskListService {

    private final TaskListRepository taskListRepository;

    public TaskListService(TaskListRepository taskListRepository) {
        this.taskListRepository = taskListRepository;
    }

    // Creates a new TaskList with  tasks and saves it in the database
    public TaskList createTaskListWithTasks(String username, String title, List<Task> tasks) {
        TaskList taskList = new TaskList(username, LocalDate.now(), title, tasks);
        return taskListRepository.save(taskList);
    }

    // Creates a new TaskList without any tasks and saves it in the database
    public TaskList createTaskList(String username, String title) {
        TaskList taskList = new TaskList(username, LocalDate.now(), title, List.of());
        return taskListRepository.save(taskList);
    }
}
