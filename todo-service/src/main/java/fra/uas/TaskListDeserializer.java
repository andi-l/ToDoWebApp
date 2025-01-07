package fra.uas;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fra.uas.model.Task;
import fra.uas.model.TaskList;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskListDeserializer extends JsonDeserializer<TaskList> {

    @Override
    public TaskList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectNode node = p.getCodec().readTree(p);

        String username = node.get("username").asText();
        String title = node.get("title").asText();
        String creationDateStr = node.get("creationDate").asText();
        LocalDate creationDate = (creationDateStr != null && !creationDateStr.isEmpty()) ? LocalDate.parse(creationDateStr) : null;
        List<Task> tasks = new ArrayList<>();
        if (node.has("tasks") && node.get("tasks").isArray()) {
            for (JsonNode taskNode : node.get("tasks")) {
                Task task = p.getCodec().treeToValue(taskNode, Task.class);
                tasks.add(task);
            }
        }
        return new TaskList(username, creationDate, title, tasks);
    }
}
