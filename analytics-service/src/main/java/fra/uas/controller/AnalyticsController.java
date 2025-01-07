package fra.uas.controller;

import fra.uas.model.TaskList;
import fra.uas.model.TaskStatistics;
import fra.uas.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    //Retrieve all user statistics
    @PostMapping("/task-statistics")
    public TaskStatistics getTaskStatistics(@RequestBody List<TaskList> taskLists) {
        return analyticsService.getTaskStatistics(taskLists);
    }

    //Retrieve user statistics in a date range
    @PostMapping("/task-statistics/date-range")
    public TaskStatistics getTaskStatisticsForDateRange(
            @RequestBody List<TaskList> taskLists,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return analyticsService.getTaskStatisticsForDateRange(taskLists, startDate, endDate);
    }

    //Retrieve user statistics of one whole year
    @PostMapping("/task-statistics/year")
    public TaskStatistics getTaskStatisticsForYear(
            @RequestBody List<TaskList> taskLists,
            @RequestParam int year) {
        return analyticsService.getTaskStatisticsForYear(taskLists, year);
    }
}
