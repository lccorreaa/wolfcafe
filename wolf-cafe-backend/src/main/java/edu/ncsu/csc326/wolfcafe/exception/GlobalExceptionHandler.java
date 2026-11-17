package edu.ncsu.csc326.wolfcafe.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Turns exceptions thrown while handling a request into error responses.
 *
 * @author CSC 326 Course Staff
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Handles WolfCafe API exceptions, answering with the status the exception carries.
	 * @param exception a WolfCafeAPI exception
	 * @param webRequest the request that caused the exception
	 * @return a ResponseEntity encapsulating the exception information for presentation to the front end
	 */
	@ExceptionHandler(WolfCafeAPIException.class)
	public ResponseEntity<ErrorDetails> handleAPIException(WolfCafeAPIException exception, WebRequest webRequest) {
		ErrorDetails errorDetails = new ErrorDetails(
				LocalDateTime.now(),
				exception.getMessage(),
				webRequest.getDescription(false)
		);

		return new ResponseEntity<>(errorDetails, exception.getStatus());
	}

	/**
	 * Handles a request body that fails the validation annotations on its DTO
	 * (for example, @NotBlank on ItemDto.name), answering with 400 Bad Request
	 * and every failed check in the message.
	 * @param exception the validation failure
	 * @param webRequest the request that caused the exception
	 * @return a ResponseEntity encapsulating the validation errors for presentation to the front end
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDetails> handleValidationException(MethodArgumentNotValidException exception,
			WebRequest webRequest) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getDefaultMessage())
				.collect(Collectors.joining(" "));
		ErrorDetails errorDetails = new ErrorDetails(
				LocalDateTime.now(),
				message,
				webRequest.getDescription(false)
		);

		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}
}
