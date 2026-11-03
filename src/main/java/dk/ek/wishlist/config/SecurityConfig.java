package dk.ek.wishlist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security setup for the app. For now it only provides the password encoder
 * used to check passwords at login.
 */
@Configuration
public class SecurityConfig {

    /**
     * The encoder used to hash and check passwords.
     *
     * @return a BCrypt encoder; stored hashes in {@code app_user.password_hash} must be BCrypt hashes
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
