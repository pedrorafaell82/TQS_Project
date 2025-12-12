package tqs.soundshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tqs.soundshop.entities.User;

import java.util.Set;

public record RegisterUserRequest(
        @NotBlank String name,
        @Email String email,
        @Size(min = 6) String password,
        Set<User.Role> roles
) {}
