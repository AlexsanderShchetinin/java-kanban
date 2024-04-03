package exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String massege, Throwable cause) {
        super(massege, cause);
    }
}
