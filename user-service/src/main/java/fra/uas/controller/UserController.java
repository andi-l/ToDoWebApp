package fra.uas.controller;

import fra.uas.model.User;
import fra.uas.model.UserDTO;
import fra.uas.service.TokenService;
import fra.uas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

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
}
