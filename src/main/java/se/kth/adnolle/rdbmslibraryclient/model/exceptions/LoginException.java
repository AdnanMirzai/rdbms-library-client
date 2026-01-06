package se.kth.adnolle.rdbmslibraryclient.model.exceptions;

public class LoginException extends RuntimeException {
    public LoginException(String message) {
        super(message);
    }
}
