package fra.uas;

import fra.uas.model.User;
import fra.uas.model.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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
}
