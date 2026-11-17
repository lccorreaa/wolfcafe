package edu.ncsu.csc326.wolfcafe.mapper;

import java.util.Comparator;

import edu.ncsu.csc326.wolfcafe.dto.UserDto;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;

/**
 * Maps user entities to password-free response DTOs.
 *
 * @author Lylan Bui (labui)
 */
public final class UserMapper {

    /**
     * Utility class; not meant to be instantiated.
     */
    private UserMapper() {
    }

    /**
     * Converts a user entity to safe response data.
     * @param user user entity to convert
     * @return the user's non-credential information
     */
    public static UserDto mapToUserDto(User user) {
        String role = user.getRoles().stream()
                .map(Role::getName)
                .min(Comparator.naturalOrder())
                .orElse(null);
        return new UserDto(user.getId(), user.getName(), user.getUsername(),
                user.getEmail(), user.getPhone(), role);
    }
}
