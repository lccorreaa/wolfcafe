package edu.ncsu.csc326.wolfcafe.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Provides details on errors.
 *
 * @author CSC 326 Course Staff
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {

	/** Error timestamp */
    private LocalDateTime timeStamp;
    /** Error message */
    private String message;
    /** Error details */
    private String details;
}
