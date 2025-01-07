package fra.uas.repository;

import fra.uas.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
@Repository
public class UserRepository {
    public ArrayList<User> userList = new ArrayList<>();
}
