package fra.uas;

import fra.uas.model.User;
import fra.uas.model.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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
}
