package fra.uas.model;

import java.time.LocalDateTime;

public class Token {
    private String username;
    private String tokenValue;
    private LocalDateTime expiryTime;

    public Token(String username, String tokenValue) {
        this.username = username;
        this.tokenValue = tokenValue;
        this.expiryTime = LocalDateTime.now().plusHours(1);
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    // Check if the token is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    @Override
    public String toString() {
        return "Token{" +
                "username='" + username + '\'' +
                ", tokenValue='" + tokenValue + '\'' +
                ", expiryTime=" + expiryTime +
                '}';
    }
}
