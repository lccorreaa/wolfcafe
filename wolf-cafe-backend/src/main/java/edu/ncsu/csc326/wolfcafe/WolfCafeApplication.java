package edu.ncsu.csc326.wolfcafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class for the WolfCafe application.
 *
 * @author CSC 326 Course Staff
 */
@SpringBootApplication
public class WolfCafeApplication {

	/**
	 * Starts the WolfCafe application.
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(WolfCafeApplication.class, args);
	}

}
