package edu.ncsu.csc326.wolfcafe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised when an ingredient cannot be added because its input is invalid.
 *
 * @author Lylan Bui
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidIngredientException extends RuntimeException {

    /** Default serial version uid */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs the exception with the given message.
     * @param message exception message
     */
    public InvalidIngredientException(String message) {
        super(message);
    }
}
