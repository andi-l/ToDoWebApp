package fra.uas.service;

import fra.uas.model.Task;
import fra.uas.model.TaskList;
import fra.uas.model.TaskStatistics;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AnalyticsService {

    public AnalyticsService() {
    }

    //Get all user statistics
    public TaskStatistics getTaskStatistics(List<TaskList> taskLists) {
        return calculateTaskStatistics(taskLists, null, null);
    }

    //Get all user statistics for a date range
    public TaskStatistics getTaskStatisticsForDateRange(List<TaskList> taskLists, LocalDate startDate, LocalDate endDate) {
        return calculateTaskStatistics(taskLists, startDate, endDate);
    }

    //Retrieve all user statistics for one whole year
    public TaskStatistics getTaskStatisticsForYear(List<TaskList> taskLists, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return calculateTaskStatistics(taskLists, startDate, endDate);
    }


    //Calculate statistics based on startdate and enddate
    private TaskStatistics calculateTaskStatistics(List<TaskList> taskLists, LocalDate startDate, LocalDate endDate) {
        int totalTasks = 0;
        int completedTasks = 0;
        int pendingTasks = 0;
        int totalTaskLists = 0;

        for (TaskList taskList : taskLists) {
            totalTaskLists++;
            List<Task> tasks = taskList.getTasks();

            for (Task task : tasks) {
                LocalDate completionDate = task.getCompletionDate();

                if (startDate != null && endDate != null) {
                    LocalDate relevantDate = completionDate != null ? completionDate : task.getCreationDate();
                    if (relevantDate == null || relevantDate.isBefore(startDate) || relevantDate.isAfter(endDate)) {
                        continue;
                    }
                }

                totalTasks++;
                if (task.isCompleted()) {
                    completedTasks++;
                } else {
                    pendingTasks++;
                }
            }
        }

        double completionRate = totalTasks > 0 ? Math.round((double) completedTasks / totalTasks * 100 * 100.0) / 100.0 : 0;

        return new TaskStatistics(totalTasks, completedTasks, completionRate, totalTaskLists, pendingTasks);
    }
}
