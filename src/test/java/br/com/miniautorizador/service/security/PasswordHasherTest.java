package br.com.miniautorizador.service.security;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PasswordHasherTest {
    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    void hashesWithRandomSaltAndChecksPasswordPreservingLeadingZeros() {
        var first = hasher.hash("0123");
        var second = hasher.hash("0123");
        assertThat(first).startsWith("$2a$12$").hasSize(60).isNotEqualTo(second);
        assertThat(hasher.matches("0123", first)).isTrue();
        assertThat(hasher.matches("9999", first)).isFalse();
        assertThat(hasher.matches("0123", "0123")).isFalse();
    }
}
