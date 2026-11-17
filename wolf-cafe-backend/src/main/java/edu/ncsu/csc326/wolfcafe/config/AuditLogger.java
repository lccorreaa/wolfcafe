package edu.ncsu.csc326.wolfcafe.config;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Records who did what in WolfCafe.  Every major create, read, update, and delete
 * operation should be logged with the user, their role, and the action, so call
 * {@link #log(String)} from each controller method once the action has succeeded.
 *
 * Entries are written through SLF4J to the logger named "wolfcafe.audit", so they
 * appear in the console with the rest of the application's output, prefixed with
 * that name.  To also keep them in a file, add a line like
 * {@code logging.file.name=logs/wolfcafe.log} to application.properties.
 *
 * Never pass a password, token, or other secret to this class.
 *
 * @author CSC 326 Course Staff
 */
@Component
public class AuditLogger {

	/** Name of the user recorded when nobody is logged in, such as on registration. */
	public static final String ANONYMOUS_USER = "anonymous";

	/** Logger that every audit entry is written to. */
	private static final Logger LOG = LoggerFactory.getLogger("wolfcafe.audit");

	/**
	 * Logs an action taken by the user making the current request.
	 * @param action description of what the user did, e.g. "created item 7 (Coffee)"
	 */
	public void log(String action) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			log(ANONYMOUS_USER, "none", action);
			return;
		}
		String roles = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.sorted()
				.collect(Collectors.joining(","));
		log(authentication.getName(), roles, action);
	}

	/**
	 * Logs an action for a user named explicitly.  Use this where the request has
	 * no logged in user yet, such as registering or logging in.
	 * @param user username the action is about
	 * @param role role(s) of that user, or "none"
	 * @param action description of what happened
	 */
	public void log(String user, String role, String action) {
		LOG.info("user={} role={} action={}", user, role, action);
	}

}
