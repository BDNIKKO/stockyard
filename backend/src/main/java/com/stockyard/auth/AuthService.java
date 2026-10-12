package com.stockyard.auth;

import com.stockyard.auth.AuthDtos.*;
import com.stockyard.channel.Channel;
import com.stockyard.channel.ChannelRepository;
import com.stockyard.common.ApiException;
import com.stockyard.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {
    private static final List<String> DEFAULT_CHANNELS = List.of("Direct", "Etsy", "In-person market");

    private final MakerRepository makers;
    private final ChannelRepository channels;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(MakerRepository makers, ChannelRepository channels, PasswordEncoder encoder, JwtService jwt) {
        this.makers = makers;
        this.channels = channels;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (makers.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("An account with that email already exists");
        }
        Maker maker = makers.save(new Maker(email, encoder.encode(req.password()), req.shopName().trim()));
        DEFAULT_CHANNELS.forEach(name -> channels.save(new Channel(maker, name)));
        return new AuthResponse(jwt.issue(maker.getId(), maker.getEmail()), MakerResponse.of(maker));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Maker maker = makers.findByEmailIgnoreCase(req.email().trim())
                .filter(m -> encoder.matches(req.password(), m.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return new AuthResponse(jwt.issue(maker.getId(), maker.getEmail()), MakerResponse.of(maker));
    }

    @Transactional(readOnly = true)
    public MakerResponse me(Long makerId) {
        return makers.findById(makerId).map(MakerResponse::of)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
    }
}
