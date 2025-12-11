package tqs.soundshop.dto;

import tqs.soundshop.entities.User;
import java.util.Set;

public record UserDto(
        Long id,
        String email,
        String name,
        Set<User.Role> roles
) {}
