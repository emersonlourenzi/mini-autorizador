package br.com.miniautorizador.service.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public String hash(String raw) {
        return encoder.encode(raw);
    }

    public boolean matches(String raw, String stored) {
        return encoder.matches(raw, stored);
    }
}
