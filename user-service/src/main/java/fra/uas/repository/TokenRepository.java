package fra.uas.repository;

import fra.uas.model.Token;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TokenRepository {
    public List<Token> tokens = new ArrayList<>();

    public Boolean tokenExists(String username) {
        for (Token tokenInList : tokens) {
            if (tokenInList.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void updateToken(String token) {
        for (Token tokenInList : tokens) {
            if (tokenInList.getTokenValue().equals(token)) {
                tokenInList.setExpiryTime(LocalDateTime.now().plusHours(1));
            }
        }

    }

    public void deleteToken(String username) {
        tokens.removeIf(tokenInList -> tokenInList.getUsername().equals(username));
    }

    public boolean isTokenValid(String token) {
        Token tokenInfo = getTokenByAuthtoken(token);
        if (tokenInfo != null && tokenInfo.getTokenValue().equals(token)) {
            return tokenInfo.getExpiryTime().isAfter(LocalDateTime.now());
        }
        return false;
    }

    public Token getTokenByAuthtoken(String token) {

        for (Token tokenInList : tokens) {
            if (tokenInList.getTokenValue().equals(token)) {
                return tokenInList;
            }
        }
        return null;
    }

    public Token getTokenByUsername(String username) {

        for (Token tokenInList : tokens) {
            if (tokenInList.getUsername().equals(username)) {
                return tokenInList;
            }
        }
        return null;
    }

    public String getUsernameByToken(String token) {

        return getTokenByAuthtoken(token).getUsername();
    }
}
