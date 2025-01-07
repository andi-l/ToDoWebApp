package fra.uas.controller;

import fra.uas.model.User;
import fra.uas.model.UserDTO;
import fra.uas.service.TokenService;
import fra.uas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    public UserService userService;

    @Autowired
    public TokenService tokenService;

    // Create a new User
    @PostMapping("/user")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (userService.usernameExists(user.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        userService.createUser(user);
        return ResponseEntity.ok(
                "User " + user.getUsername() + " created successfully"
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDTO user) {
        if (userService.validateUser(user.getUsername(), user.getPassword())) {
            String token = tokenService.createToken(user.getUsername());
            return ResponseEntity.ok("Token: " + token);
        } else {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }

    // Protected endpoint
    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint(@RequestHeader("Authorization") String authToken) {
        if (tokenService.isTokenValid(authToken)) {
            return ResponseEntity.ok("Access to protected resource granted");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in or the session has expired");
        }
    }

    // Delete a User
    @DeleteMapping("/user")
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String token) {
        String username = tokenService.getUsernameByToken(token);
        boolean result = userService.deleteUser(username);
        if (result) {
            return ResponseEntity.ok("User " + username + " deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("User not found or could not be deleted");
        }
    }

    @GetMapping("/username")
    public ResponseEntity<?> getUsernameByToken(@RequestHeader("Authorization") String authToken) {
        String username = tokenService.getUsernameByToken(authToken);
        if (username != null) {
            return ResponseEntity.ok(username);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or user not found");
        }
    }


    // Get list of all Users
    @GetMapping("/list")
    public ResponseEntity<?> getUserList(@RequestHeader("Authorization") String token) {
        if (tokenService.isTokenValid(token)) {
            String username = tokenService.getUsernameByToken(token);
            User user = userService.getUser(username);
            if (user != null) {
                return ResponseEntity.ok(userService.getUserList());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User does not exist");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in or the session has expired");
    }
}
