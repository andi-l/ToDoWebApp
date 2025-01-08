package fra.uas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    //Endpoint to forward GraphQL Data Retrievement queries
    @PostMapping
    public ResponseEntity<?> forwardGraphQL(@RequestBody String graphqlQuery, @RequestHeader("Authorization") String authToken) {
        var resp = protectedEndpoint(authToken);
        if (resp.getStatusCode().is2xxSuccessful()) {

            String url = todoBackendUrl + "/graphql";

            // Set appropriate headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");

            // Forward the request
            HttpEntity<String> entity = new HttpEntity<>(graphqlQuery, headers);
            return restTemplate.postForEntity(url, entity, String.class);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to book a room");
        }
    }

    //Endpoint to create a Tasklist
    @PostMapping("/tasklists")
    public ResponseEntity<?> createTaskList(@RequestBody Map<String, Object> payload, @RequestHeader("Authorization") String authToken) {
        var reso = protectedEndpoint(authToken);
        if (reso.getStatusCode().is2xxSuccessful()) {
            String query = "mutation($username: String!, $title: String!) { createTaskList(username: $username, title: $title) { id username title creationDate } }";
            String url = todoBackendUrl + "/graphql";
            Map<String, Object> body = Map.of("query", query, "variables", payload);
            return restTemplate.postForEntity(url, body, String.class);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to book a room");
        }
    }

    //Endpoint to retrieve all Tasklists
    @GetMapping("/tasklists")
    public ResponseEntity<?> getAllTaskLists(@RequestHeader("Authorization") String authToken) {
        var reso = protectedEndpoint(authToken);
        if (reso.getStatusCode().is2xxSuccessful()) {


            String query = "{ getAllTaskLists { id username title creationDate tasks { id title taskDescription completed dueDate } } }";
            String url = todoBackendUrl + "/graphql";
            Map<String, Object> body = Map.of("query", query);
            return restTemplate.postForEntity(url, body, String.class);

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to book a room");
        }
    }

    //Endpoint to retrieve all Tasklists of one user
    @GetMapping("/tasklists/username")
    public ResponseEntity<?> getTaskListsByUsername(@RequestParam String username, @RequestHeader("Authorization") String authToken) {
        // Check if the user is authenticated
        var reso = protectedEndpoint(authToken);
        if (reso.getStatusCode().is2xxSuccessful()) {
            // Define the GraphQL query
            String query = "{ getTaskListsByUsername(username: \"" + username + "\") { id username title creationDate tasks { id username title taskDescription completed creationDate dueDate completionDate } } }";

            // Set the backend URL
            String url = todoBackendUrl + "/graphql";

            // Build the request body
            Map<String, Object> body = Map.of("query", query);

            // Forward the request to the backend
            return restTemplate.postForEntity(url, body, String.class);
        } else {
            // Return unauthorized if the user is not authenticated
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to login to view task lists.");
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
        String query = "mutation($taskListId: ID!, $taskIds: [ID!]!) { deleteTasksFromTaskList(taskListId: $taskListId, taskIds: $taskIds) { id username title creationDate tasks { id title taskDescription completed dueDate } } }";
        String url = todoBackendUrl + "/graphql";

        // Build request body
        Map<String, Object> variables = Map.of(
                "taskListId", taskListId,
                "taskIds", taskIds
        );
        Map<String, Object> body = Map.of(
                "query", query,
                "variables", variables
        );

        // Forward the request to the backend
        return restTemplate.postForEntity(url, body, String.class);
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
            return restTemplate.postForEntity(url, body, String.class);
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

        // Build request variables
        Map<String, Object> variables = Map.of(
                "taskListId", taskListId,
                "taskId", taskId,
                "isCompleted", isCompleted
        );

        // Build the request body
        Map<String, Object> body = Map.of(
                "query", query,
                "variables", variables
        );

        // Forward the request to the GraphQL backend
        String url = todoBackendUrl + "/graphql";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to toggle task completion: " + e.getMessage());
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
        String taskListsJson = (String) response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        Root root = objectMapper.readValue(taskListsJson, Root.class);

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
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
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

        // Extract request variables
        Map<String, Object> variables = Map.of(
                "taskListId", taskListId,
                "title", updates.get("title")
        );

        // Build the request body
        Map<String, Object> body = Map.of("query", query, "variables", variables);

        // Forward the request to the GraphQL backend
        String url = todoBackendUrl + "/graphql";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update task list title: " + e.getMessage());
        }
    }
}
