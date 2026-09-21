package Haleon.auth.dto;

public record LoginResponse(
        String token,
        String tokenType
) {
}