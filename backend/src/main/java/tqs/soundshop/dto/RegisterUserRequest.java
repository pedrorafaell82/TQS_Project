package tqs.soundshop.dto;

public record RegisterUserRequest(
        String email,
        String name,
        String password,
        String role
) {}
