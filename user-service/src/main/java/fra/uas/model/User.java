package fra.uas.model;

public class User {

    private String username;
    private String firstName;
    private String lastName;
    private String password;

    // Full Constructor
    public User(
            String username,
            String firstName,
            String lastName,
            String password
    ) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }


    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return (
                "\nUser: " +
                        "username='" +
                        getUsername() +
                        "'" +
                        ", firstName='" +
                        getFirstName() +
                        "'" +
                        ", lastName='" +
                        getLastName() +
                        "'" +
                        ", password='" +
                        getPassword() +
                        "'"
        );
    }
}
