package fra.uas.service;

import fra.uas.model.Task;
import fra.uas.model.TaskList;
import fra.uas.repository.TaskListRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    // Adds a list of tasks to an existing TaskList and saves the updated TaskList
    public TaskList addTasksToTaskList(Long taskListId, List<Task> tasks) {
        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new IllegalArgumentException("TaskList with ID " + taskListId + " not found."));
        taskList.getTasks().addAll(tasks);
        return taskListRepository.save(taskList);
    }

    // Retrieves all TaskLists from the database
    public List<TaskList> getAllTaskLists() {
        return taskListRepository.findAll();
    }

    // Retrieves a specific TaskList by its ID
    public TaskList getTaskListById(Long id) {
        return taskListRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TaskList with ID " + id + " not found."));
    }

    // Retrieves all TaskLists for a specific username
    public List<TaskList> getTaskListsByUsername(String username) {
        return taskListRepository.findByUsername(username);
    }

    // Updates the details of a TaskList, including tasks, and saves the updated TaskList
    public TaskList updateTaskList(Long taskListId, String username, String creationDate, List<Task> updatedTasks, String title) {
        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new IllegalArgumentException("TaskList with ID " + taskListId + " not found."));

        taskList.setUsername(username);
        taskList.setCreationDate(LocalDate.parse(creationDate));
        taskList.setTitle(title);
        taskList.getTasks().clear();
        taskList.getTasks().addAll(updatedTasks);
        return taskListRepository.save(taskList);
    }

    // Deletes a TaskList by its ID
    public boolean deleteTaskList(Long taskListId) {
        TaskList taskList = taskListRepository.findById(taskListId).orElse(
                null
        );
        if (taskList == null) {
            return false;
        }

        taskListRepository.delete(taskList);
        return true;
    }

    // Removes specific tasks from a TaskList and saves the updated TaskList
    public TaskList deleteTasksFromTaskList(Long taskListId, List<Long> taskIds) {
        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new IllegalArgumentException("TaskList with ID " + taskListId + " not found."));
        List<Task> tasksToRemove = taskList.getTasks().stream()
                .filter(task -> taskIds.contains(task.getId()))
                .toList();
        taskList.getTasks().removeAll(tasksToRemove);
        return taskListRepository.save(taskList);
    }

    // Retrieves TaskLists for a username, sorted by creation date in ascending order
    public List<TaskList> getTaskListsSortedByCreationDateAsc(String username) {
        return taskListRepository.findByUsername(username).stream()
                .sorted(Comparator.comparing(TaskList::getCreationDate))
                .collect(Collectors.toList());
    }

    // Retrieves TaskLists for a username, sorted by creation date in descending order
    public List<TaskList> getTaskListsSortedByCreationDateDesc(String username) {
        return taskListRepository.findByUsername(username).stream()
                .sorted(Comparator.comparing(TaskList::getCreationDate).reversed())
                .collect(Collectors.toList());
    }

    // Retrieves TaskLists for a username, sorted by the number of tasks in ascending order
    public List<TaskList> getTaskListsSortedByTaskCountAsc(String username) {
        return taskListRepository.findByUsername(username).stream()
                .sorted(Comparator.comparing(taskList -> taskList.getTasks().size()))
                .collect(Collectors.toList());
    }

    // Retrieves TaskLists for a username, sorted by the number of tasks in descending order
    public List<TaskList> getTaskListsSortedByTaskCountDesc(String username) {
        return taskListRepository.findByUsername(username).stream()
                .sorted(Comparator.comparing((TaskList taskList) -> taskList.getTasks().size()).reversed())
                .collect(Collectors.toList());
    }

    // Toggles the completion status of a task and updates its completion date accordingly
    public Task toggleTaskCompletion(Long taskListId, Long taskId, boolean isCompleted) {
        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new IllegalArgumentException("TaskList with ID " + taskListId + " not found."));
        Task task = taskList.getTasks().stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task with ID " + taskId + " not found in TaskList " + taskListId));
        task.setCompleted(isCompleted);
        task.setCompletionDate(isCompleted ? LocalDate.now() : null);
        taskListRepository.save(taskList);
        return task;
    }

    // Updates the details of a specific task in a TaskList
    public Task updateTaskDetails(Long taskListId, Long taskId, String title, String description, LocalDate dueDate) {
        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new IllegalArgumentException("TaskList with ID " + taskListId + " not found."));
        Task task = taskList.getTasks().stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task with ID " + taskId + " not found in TaskList " + taskListId));
        if (title != null) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setTaskDescription(description);
        }
        if (dueDate != null) {
            task.setDueDate(dueDate);
        }
        taskListRepository.save(taskList);
        return task;
    }

    // Updates the title of a specific TaskList
    public TaskList updateTaskListTitle(Long taskListId, String title) {
        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new IllegalArgumentException("TaskList with ID " + taskListId + " not found."));
        if (title != null) {
            taskList.setTitle(title);
        }
        return taskListRepository.save(taskList);
    }
}
