package se.kth.adnolle.rdbmslibraryclient.model.exceptions;

public class DeleteException extends RuntimeException {
    public DeleteException(String message) {
        super(message);
    }
}
