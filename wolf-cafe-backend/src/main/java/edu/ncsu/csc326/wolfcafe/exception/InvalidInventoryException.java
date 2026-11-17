package edu.ncsu.csc326.wolfcafe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception if an inventory update contains invalid values, such as a negative
 * or missing amount, or an ingredient that is not in the inventory.
 *
 * @author Sandra Jose (sajose)
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidInventoryException extends RuntimeException {

    /** Default serial version uid*/
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs the exception with the given message.
	 * @param message exception message
	 */
	public InvalidInventoryException(String message) {
        super(message);
    }

}
