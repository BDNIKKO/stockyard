package com.stockyard.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 8, max = 72, message = "must be 8-72 characters") String password,
            @NotBlank @Size(max = 120) String shopName) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record MakerResponse(Long id, String email, String shopName) {
        static MakerResponse of(Maker m) { return new MakerResponse(m.getId(), m.getEmail(), m.getShopName()); }
    }

    public record AuthResponse(String token, MakerResponse maker) {}
}
