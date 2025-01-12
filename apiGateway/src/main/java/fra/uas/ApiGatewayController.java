package fra.uas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fra.uas.model.TaskList;
import fra.uas.model.User;
import fra.uas.model.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3001", allowCredentials = "true")
@RestController
@RequestMapping("/gateway")
public class ApiGatewayController {

    private final RestTemplate restTemplate;

    @Value("${todo.backend.url}")
    private String todoBackendUrl;

    @Value("${user.backend.url}")
    private String userServiceUrl;

    @Value("${analytic.backend.url}")
    private String AnalyticServiceUrl;

    public ApiGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    //Endpoint to create a user
    @RequestMapping(value = "/user", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUser(@RequestBody User user) {

        HttpEntity<User> request = new HttpEntity<>(user);
        try {
            // Sends the user data to create a new user
            return restTemplate.postForEntity(userServiceUrl + "/user", request, String.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Username already exists");
        }
    }

    // Endpoint for user login. Sends POST request to User Service for authentication
    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginUser(@RequestBody UserDTO user) {

        HttpEntity<UserDTO> request = new HttpEntity<>(user);
        try {
            // Attempts to authenticate the user with the provided credentials
            return restTemplate.postForEntity(userServiceUrl + "/login", request, String.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getStatusText());
        }
    }


    // Endpoint to delete a user. Sends DELETE request to User Service
    @RequestMapping(value = "/user", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<?> request = new HttpEntity<>(headers);
        try {
            // Deletes the user associated with the token
            return restTemplate.exchange(userServiceUrl + "/user", HttpMethod.DELETE, request, String.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getStatusText());
        }
    }

    @RequestMapping(value = "/protected", method = RequestMethod.GET)
    public ResponseEntity<?> protectedEndpoint(@RequestHeader("Authorization") String authToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        HttpEntity<?> request = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(userServiceUrl + "/protected", HttpMethod.GET, request, String.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getStatusText());
        }
    }

    //Method to handle TaskList Service Responses
    private ResponseEntity<?> processGraphQLResponse(ResponseEntity<String> response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());

            // Check if the response contains errors
            if (root.has("errors")) {
                JsonNode errors = root.get("errors");
                String errorMessage = errors.get(0).get("message").asText();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }

            // Return the data node if no errors are present
            if (root.has("data")) {
                return ResponseEntity.ok(root.get("data"));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected GraphQL response structure.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process GraphQL response: " + e.getMessage());
        }
    }


    //Endpoint to forward GraphQL Data Retrievement queries
    @PostMapping
    public ResponseEntity<?> forwardGraphQL(@RequestBody String graphqlQuery, @RequestHeader("Authorization") String authToken) {
        // Validate authorization
        var reso = protectedEndpoint(authToken);
        if (!reso.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to perform this action.");
        }

        // Forward the request to the GraphQL service
        String url = todoBackendUrl + "/graphql";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(graphqlQuery, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return processGraphQLResponse(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the GraphQL request: " + e.getMessage());
        }
    }


    //Endpoint to create a Tasklist
    @PostMapping("/tasklists")
    public ResponseEntity<?> createTaskList(@RequestBody Map<String, Object> payload, @RequestHeader("Authorization") String authToken) {
        // Validate authorization
        var reso = protectedEndpoint(authToken);
        if (!reso.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to log in to create a task list.");
        }

        // GraphQL mutation query
        String query = "mutation($username: String!, $title: String!) { createTaskList(username: $username, title: $title) { id username title creationDate } }";
        String url = todoBackendUrl + "/graphql";

        Map<String, Object> body = Map.of("query", query, "variables", payload);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            return processGraphQLResponse(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create task list: " + e.getMessage());
        }
    }

    //Endpoint to retrieve all Tasklists
    @GetMapping("/tasklists")
    public ResponseEntity<?> getAllTaskLists(@RequestHeader("Authorization") String authToken) {
        // Validate authorization
        var reso = protectedEndpoint(authToken);
        if (!reso.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to log in to access task lists.");
        }

        // GraphQL query
        String query = "{ getAllTaskLists { id username title creationDate tasks { id title taskDescription completed dueDate } } }";
        String url = todoBackendUrl + "/graphql";

        Map<String, Object> body = Map.of("query", query);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            return processGraphQLResponse(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve task lists: " + e.getMessage());
        }
    }


    //Endpoint to retrieve all Tasklists of a specific user
    @GetMapping("/tasklists/username")
    public ResponseEntity<?> getTaskListsByUsername(@RequestParam String username, @RequestHeader("Authorization") String authToken) {
        // Validate authorization
        var reso = protectedEndpoint(authToken);
        if (!reso.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to view task lists.");
        }

        // GraphQL query
        String query = "{ getTaskListsByUsername(username: \"" + username + "\") { id username title creationDate tasks { id username title taskDescription completed creationDate dueDate completionDate } } }";
        String url = todoBackendUrl + "/graphql";
        Map<String, Object> body = Map.of("query", query);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            return processGraphQLResponse(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve task lists for user: " + e.getMessage());
        }
    }

    //Endpoint to retrieve a Tasklist by its ID
    @PostMapping("/getTaskListById")
    public ResponseEntity<?> forwardGetTaskListById(@RequestBody String graphqlQuery, @RequestHeader("Authorization") String authToken) {
        var reso = protectedEndpoint(authToken);
        if (!reso.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to view the task list.");
        }

        String url = todoBackendUrl + "/graphql";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(graphqlQuery, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            JsonNode root = new ObjectMapper().readTree(response.getBody());
            JsonNode taskListNode = root.path("data").path("getTaskListById");
            if (taskListNode.isNull()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task list not found.");
            }

            return processGraphQLResponse(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the GraphQL request: " + e.getMessage());
        }
    }

    //Endpoint to create a Tasklist with tasks
    @PostMapping("/createTaskListWithTasks")
    public ResponseEntity<?> forwardCreateTaskListWithTasks(@RequestBody String graphqlQuery, @RequestHeader("Authorization") String authToken) {
        var reso = protectedEndpoint(authToken);
        if (!reso.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to create a task list with tasks.");
        }

        String url = todoBackendUrl + "/graphql";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(graphqlQuery, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return processGraphQLResponse(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the GraphQL request: " + e.getMessage());
        }
    }


    //Endpoint to add tasks to a Tasklist
    @PostMapping("/addTasksToTaskList")
    public ResponseEntity<?> forwardAddTasksToTaskList(@RequestBody String graphqlQuery, @RequestHeader("Authorization") String authToken) {
        var reso = protectedEndpoint(authToken);
        if (!reso.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to add tasks to the task list.");
        }

        String url = todoBackendUrl + "/graphql";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(graphqlQuery, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return processGraphQLResponse(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the GraphQL request: " + e.getMessage());
        }
    }



    //Endpoint to delete a Task from a Tasklist
    @DeleteMapping("/tasklists/{taskListId}/tasks")
    public ResponseEntity<?> deleteTasksFromTaskList(
            @PathVariable Long taskListId,
            @RequestBody List<Long> taskIds,
            @RequestHeader("Authorization") String authToken) {

        // Validate authorization
        var reso = protectedEndpoint(authToken);
        if (!reso.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to modify tasks.");
        }

        // GraphQL mutation query
        String query = "mutation($taskListId: ID!, $taskIds: [ID!]!) { deleteTasksFromTaskList(taskListId: $taskListId, taskIds: $taskIds) }";
        String url = todoBackendUrl + "/graphql";


        // Build request body
        Map<String, Object> variables = Map.of(
                "taskListId", taskListId,
                "taskIds", taskIds
        );
        Map<String, Object> body = Map.of("query", query,
                "variables", variables
        );

        try {
            System.out.println("hello");
            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            JsonNode root = new ObjectMapper().readTree(response.getBody());
            System.out.println("hello2");
            System.out.println(root);
            boolean isDeleted = root.get("data").get("deleteTasksFromTaskList").asBoolean();;
            System.out.println(isDeleted);
            if (isDeleted) {
                return ResponseEntity.ok("Task  deleted successfully.");
            } else {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Task cannot be deleted");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the task: " + e.getMessage());
        }

    }


    //Endpoint to delete a Tasklist
    @DeleteMapping("/tasklists/{taskListId}")
    public ResponseEntity<?> deleteTaskList(
            @PathVariable Long taskListId,
            @RequestHeader("Authorization") String authToken) {

        //  authorization
        var reso = protectedEndpoint(authToken);
        if (!reso.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to delete a task list.");
        }

        // GraphQL mutation query
        String query = "mutation($taskListId: ID!) { deleteTaskList(taskListId: $taskListId) }";
        String url = todoBackendUrl + "/graphql";

        //  request body
        Map<String, Object> variables = Map.of("taskListId", taskListId);
        Map<String, Object> body = Map.of("query", query, "variables", variables);

        // Forward the request
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            JsonNode root = new ObjectMapper().readTree(response.getBody());
            boolean isDeleted = root.get("data").get("deleteTaskList").asBoolean();

            if (isDeleted) {
                return ResponseEntity.ok("Task list deleted successfully.");
            } else {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Task list cannot be deleted");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the task list: " + e.getMessage());
        }
    }

    @PatchMapping("/tasklists/{taskListId}/tasks/{taskId}/completion")
    public ResponseEntity<?> toggleTaskCompletion(
            @PathVariable Long taskListId,
            @PathVariable Long taskId,
            @RequestParam boolean isCompleted,
            @RequestHeader("Authorization") String authToken) {

        // Validate authorization
        var reso = protectedEndpoint(authToken);
        if (!reso.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to modify tasks.");
        }

        // GraphQL mutation query
        String query = """
                    mutation ToggleTaskCompletion($taskListId: ID!, $taskId: ID!, $isCompleted: Boolean!) {
                        toggleTaskCompletion(taskListId: $taskListId, taskId: $taskId, isCompleted: $isCompleted) {
                            id
                            title
                            completed
                            completionDate
                        }
                    }
                """;

        Map<String, Object> variables = Map.of(
                "taskListId", taskListId,
                "taskId", taskId,
                "isCompleted", isCompleted
        );
        Map<String, Object> body = Map.of("query", query, "variables", variables);

        String url = todoBackendUrl + "/graphql";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return processGraphQLResponse(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to toggle task completion: " + e.getMessage());
        }
    }


    //Update the details of a Task in a Tasklist
    @PatchMapping("/tasklists/{taskListId}/tasks/{taskId}")
    public ResponseEntity<?> updateTaskDetails(
            @PathVariable Long taskListId,
            @PathVariable Long taskId,
            @RequestBody Map<String, String> updates,
            @RequestHeader("Authorization") String authToken) {

        // Validate authorization
        var reso = protectedEndpoint(authToken);
        if (!reso.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to modify tasks.");
        }

        // GraphQL mutation query
        String query = """
                    mutation UpdateTaskDetails($taskListId: ID!, $taskId: ID!, $title: String, $description: String, $dueDate: String) {
                        updateTaskDetails(taskListId: $taskListId, taskId: $taskId, title: $title, description: $description, dueDate: $dueDate) {
                            id
                            title
                            taskDescription
                            dueDate
                        }
                    }
                """;

        // Extract request variables
        Map<String, Object> variables = Map.of(
                "taskListId", taskListId,
                "taskId", taskId,
                "title", updates.get("title"),
                "description", updates.get("description"),
                "dueDate", updates.get("dueDate")
        );

        // Build the request body
        Map<String, Object> body = Map.of("query", query, "variables", variables);

        // Forward the request to the GraphQL backend
        String url = todoBackendUrl + "/graphql";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            return processGraphQLResponse(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update task details: " + e.getMessage());
        }
    }


    //Update the details of a Tasklist
    @PatchMapping("/tasklists/{taskListId}")
    public ResponseEntity<?> updateTaskListTitle(
            @PathVariable Long taskListId,
            @RequestBody Map<String, String> updates,
            @RequestHeader("Authorization") String authToken) {

        // Validate authorization
        var reso = protectedEndpoint(authToken);
        if (!reso.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to modify task lists.");
        }

        // GraphQL mutation query
        String query = """
                    mutation UpdateTaskListTitle($taskListId: ID!, $title: String!) {
                        updateTaskListTitle(taskListId: $taskListId, title: $title) {
                            id
                            title
                        }
                    }
                """;

        Map<String, Object> variables = Map.of(
                "taskListId", taskListId,
                "title", updates.get("title")
        );

        Map<String, Object> body = Map.of("query", query, "variables", variables);

        String url = todoBackendUrl + "/graphql";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return processGraphQLResponse(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update task list title: " + e.getMessage());
        }
    }


    private List<TaskList> getAndValidateTaskLists(String authToken, String username) throws JsonProcessingException {
        // Validate authorization
        var reso = protectedEndpoint(authToken);
        if (!reso.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Unauthorized access. Please login.");
        }

        // Retrieve task lists
        var response = getTaskListsByUsername(username, authToken);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error retrieving task lists: " + response.getStatusCode());
        }

        // Extract and deserialize task lists so that the Analyticservice can use them
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Object responseBody = response.getBody();
        String taskListsJson;

        if (responseBody instanceof String) {
            taskListsJson = (String) responseBody;
        } else if (responseBody instanceof ObjectNode) {
            taskListsJson = objectMapper.writeValueAsString(responseBody);
        } else {
            throw new RuntimeException("Unexpected response body type: " + responseBody.getClass());
        }

        // Wrap the JSON in a "data" field
        String wrappedJson = "{\"data\":" + taskListsJson + "}";

        // Deserialize into Root class
        Root root = objectMapper.readValue(wrappedJson, Root.class);

        return root.getData().getGetTaskListsByUsername();
    }

    //Forward the request to the analytic service
    private ResponseEntity<String> forwardAnalyticsRequest(
            String url, List<TaskList> taskLists) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<TaskList>> request = new HttpEntity<>(taskLists, headers);

            return restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to forward analytics request: " + e.getMessage(), e);
        }
    }

    //Retrieve all analytics of the user
    @RequestMapping(value = "/analytics", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTaskStatistics(
            @RequestHeader("Authorization") String authToken,
            @RequestParam String username) {
        try {
            List<TaskList> taskLists = getAndValidateTaskLists(authToken, username);
            String url = AnalyticServiceUrl + "/task-statistics";
            return forwardAnalyticsRequest(url, taskLists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    //Retrieve the analytics of the user of one specific year
    @RequestMapping(value = "/analytics/year", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTaskStatisticsForYear(
            @RequestHeader("Authorization") String authToken,
            @RequestParam String username,
            @RequestParam int year) {
        try {
            List<TaskList> taskLists = getAndValidateTaskLists(authToken, username);
            String url = AnalyticServiceUrl + "/task-statistics/year?year=" + year;
            return forwardAnalyticsRequest(url, taskLists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    //Retrieve the analytics of the user of one specific date range
    @RequestMapping(value = "/analytics/date-range", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTaskStatisticsForDateRange(
            @RequestHeader("Authorization") String authToken,
            @RequestParam String username,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        try {
            List<TaskList> taskLists = getAndValidateTaskLists(authToken, username);
            String url = AnalyticServiceUrl + "/task-statistics/date-range?startDate=" + startDate + "&endDate=" + endDate;
            return forwardAnalyticsRequest(url, taskLists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


}
