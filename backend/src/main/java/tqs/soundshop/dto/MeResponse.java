package tqs.soundshop.dto;

import java.util.List;

public record MeResponse(
    boolean authenticated,
    String username,
    List<String> roles
) {}