package fra.uas.service;

import fra.uas.model.User;
import fra.uas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService {

    @Autowired
    public UserRepository userRepository;

    // Create a User
    public void createUser(User user) {
        userRepository.userList.add(user);
    }

    // Delete a User
    public boolean deleteUser(String username) {
        boolean existed = userRepository.userList.removeIf(user -> user.getUsername().equals(username));
        return existed;
    }

    // Check if Username already exists
    public boolean usernameExists(String name) {
        return userRepository.userList.stream()
                .anyMatch(user -> user.getUsername().equalsIgnoreCase(name));
    }

    // Return the userRepository
    public ArrayList<User> getUserList() {
        ArrayList<User> userList = new ArrayList<>();
        for (User user : userRepository.userList) {
            userList.add(user);
        }
        return userList;
    }


    public boolean validateUser(String username, String password) {
        return userRepository.userList.stream()
                .anyMatch(user -> user.getUsername().equalsIgnoreCase(username) && user.getPassword().equals(password));
    }


    public User getUser(String username) {
        for (User user : userRepository.userList) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
}
