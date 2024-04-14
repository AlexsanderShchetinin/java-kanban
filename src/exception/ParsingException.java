package exception;

import java.io.IOException;

public class ParsingException extends IOException {
    public ParsingException(String message) {
        super(message);
    }
}
