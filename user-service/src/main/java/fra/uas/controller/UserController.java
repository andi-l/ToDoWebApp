package fra.uas.controller;

import fra.uas.model.User;
import fra.uas.service.TokenService;
import fra.uas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
}
