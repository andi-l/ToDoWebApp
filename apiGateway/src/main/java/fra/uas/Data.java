package fra.uas;

import fra.uas.model.TaskList;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Data {

    @JsonProperty("getTaskListsByUsername")
    private List<TaskList> getTaskListsByUsername;

    public List<TaskList> getGetTaskListsByUsername() {
        return getTaskListsByUsername;
    }

}
