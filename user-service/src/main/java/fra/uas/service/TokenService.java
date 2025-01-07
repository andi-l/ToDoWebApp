package fra.uas.service;

import fra.uas.model.Token;
import fra.uas.repository.TokenRepository;
import fra.uas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenService {
    private TokenRepository tokenRepository = new TokenRepository();

    @Autowired
    private UserRepository userRepository;

    public String createToken(String username) {
        if (tokenRepository.tokenExists(username)) {
            tokenRepository.deleteToken(username);
        }
        String token = UUID.randomUUID().toString();
        tokenRepository.tokens.add(new Token(username, token));
        return token;
    }

    public void updateToken(String token) {
        tokenRepository.updateToken(token);

    }

    public void deleteToken(String username) {
        tokenRepository.deleteToken(username);
    }

    public boolean isTokenValid(String token) {
        Token tokenInfo = tokenRepository.getTokenByAuthtoken(token);
        if (tokenInfo != null) {
            String username = tokenInfo.getUsername();
            boolean userExists = userRepository.userList.stream()
                    .anyMatch(user -> user.getUsername().equals(username));
            boolean tokenNotExpired = tokenInfo.getExpiryTime().isAfter(LocalDateTime.now());
            if (!tokenNotExpired) {
                deleteToken(username);
                return false;
            }

            return userExists && tokenNotExpired;
        }
        return false;
    }

    public String getUsernameByToken(String token) {

        return tokenRepository.getUsernameByToken(token);
    }

    public void changeUsernameOfToken(String token, String newUsername) {

        tokenRepository.getTokenByAuthtoken(token).setUsername(newUsername);

    }
}
