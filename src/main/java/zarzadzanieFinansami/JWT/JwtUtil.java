package zarzadzanieFinansami.JWT;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys; // Dla Keys
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.security.SecureRandom; // Do generowania losowych bajtów
import java.util.Base64; // Do kodowania losowych bajtów na string (opcjonalnie, jeśli chcemy logować wygenerowany string)


@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // Adnotacja @Value jest teraz opcjonalna dla tego pola, jeśli chcemy priorytetyzować auto-generowanie
    // Jeśli spring.token.secret-key nie istnieje w properties, jwtSecretString będzie null
    @Value("${spring.token.secret-key:#{null}}") // Dodajemy wartość domyślną null, jeśli klucz nie istnieje
    private String jwtSecretStringFromProperties;

    @Value("${jwt.expiration.ms}")
    private int jwtExpirationMs;

    private SecretKey jwtSecret;

    // Minimalna wymagana długość klucza w bajtach dla HS512 (512 bitów / 8 = 64 bajty)
    private static final int MIN_KEY_LENGTH_BYTES_HS512 = 64;

    @PostConstruct
    public void init() {
        boolean useGeneratedKey = false;
        if (jwtSecretStringFromProperties != null && !jwtSecretStringFromProperties.trim().isEmpty()) {
            byte[] keyBytes = jwtSecretStringFromProperties.getBytes(StandardCharsets.UTF_8);
            // Dodatkowe logowanie długości klucza w bajtach
            logger.info("JwtUtil init: Klucz z properties nie jest null/pusty. Długość w bajtach: {}. Wymagane: {}", keyBytes.length, MIN_KEY_LENGTH_BYTES_HS512);

            if (keyBytes.length >= MIN_KEY_LENGTH_BYTES_HS512) {
                this.jwtSecret = Keys.hmacShaKeyFor(keyBytes);
                logger.info("Użyto klucza JWT z pliku konfiguracyjnego.");
            } else {
                logger.warn("Klucz JWT z pliku konfiguracyjnego ('{}') jest za krótki ({} bajtów, wymagane {} bajtów dla HS512). Generowanie nowego klucza.",
                        jwtSecretStringFromProperties, keyBytes.length, MIN_KEY_LENGTH_BYTES_HS512);
                useGeneratedKey = true;
            }
        } else {
            logger.warn("Nie znaleziono klucza JWT w pliku konfiguracyjnym ('spring.token.secret-key'). Generowanie nowego klucza.");
            useGeneratedKey = true;
        }

        if (useGeneratedKey) {
            // Generowanie bezpiecznego klucza odpowiedniego dla HS512
            this.jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            logger.warn("Wygenerowano nowy, losowy klucz JWT. Jest to odpowiednie dla środowiska deweloperskiego.");
            logger.warn("UWAGA: W środowisku produkcyjnym należy używać stałego, bezpiecznie przechowywanego klucza skonfigurowanego w plikach properties/yml.");
            // Opcjonalnie: logowanie wygenerowanego klucza w formacie Base64 (tylko dla celów deweloperskich!)
            // String generatedKeyBase64 = Base64.getEncoder().encodeToString(this.jwtSecret.getEncoded());
            // logger.info("Wygenerowany klucz JWT (Base64 - tylko DEV): {}", generatedKeyBase64);
        }
    }

    public String generateJwtToken(Authentication authentication) {
        String username = authentication.getName();

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(jwtSecret, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty or invalid: {}", e.getMessage());
        }
        return false;
    }
}