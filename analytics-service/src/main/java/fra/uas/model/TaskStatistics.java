package fra.uas.model;

public class TaskStatistics {
    private int totalTasks;
    private int totalTaskLists;
    private int completedTasks;
    private int pendingTasks;
    private double completionRate;


    public TaskStatistics(int totalTasks, int completedTasks, double completionRate, int totalTaskLists, int pendingTasks) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.completionRate = completionRate;
        this.totalTaskLists = totalTaskLists;
        this.pendingTasks = pendingTasks;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }


    @Override
    public String toString() {

        return "TaskStatistics{" +
                "totalTasks=" + totalTasks +
                ", completedTasks=" + completedTasks +
                ", completionRate=" + completionRate +
                "}";
    }


    public int getTotalTaskLists() {
        return totalTaskLists;
    }

    public void setTotalTaskLists(int totalTaskLists) {
        this.totalTaskLists = totalTaskLists;
    }

    public int getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(int pendingTasks) {
        this.pendingTasks = pendingTasks;
    }
}
