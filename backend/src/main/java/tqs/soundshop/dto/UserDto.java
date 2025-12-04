package tqs.soundshop.dto;

public record UserDto(
        Long id,
        String email,
        String name,
        String role   
) {}

