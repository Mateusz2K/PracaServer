package zarzadzanieFinansami.konfikuracja;

import org.springframework.beans.factory.annotation.Autowired; // Dodaj ten import
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
// import org.springframework.security.konfikuracja.Customizer; // Nie jest już używane, jeśli httpBasic jest wyłączone
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Dodaj dla @PreAuthorize
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Dodaj ten import
import zarzadzanieFinansami.JWT.AutoryzacjaTokenuFiltr; // Załóżmy, że ta klasa będzie w tym pakiecie
import zarzadzanieFinansami.JWT.JwtAutoryzacjaEntryPoint; // Importuj swoją klasę
import zarzadzanieFinansami.serwisy.DetaleUzytkownikówSerwis;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity // Włącz obsługę adnotacji takich jak @PreAuthorize na metodach
public class KonfiguracjaZabezpieczen {

    private final DetaleUzytkownikówSerwis detaleUzytkownikówSerwis;
    private final JwtAutoryzacjaEntryPoint JwtAutoryzacjaEntryPoint; // Dodaj pole
    private final AutoryzacjaTokenuFiltr AutoryzacjaTokenuFiltr; // Dodaj pole dla filtra JWT

    @Autowired // Adnotacja @Autowired jest opcjonalna przy jednym konstruktorze, ale dodaję dla jasności
    public KonfiguracjaZabezpieczen(DetaleUzytkownikówSerwis detaleUzytkownikówSerwis,
                                    JwtAutoryzacjaEntryPoint JwtAutoryzacjaEntryPoint, // Wstrzyknij JwtAutoryzacjaEntryPoint
                                    AutoryzacjaTokenuFiltr AutoryzacjaTokenuFiltr) { // Wstrzyknij AutoryzacjaTokenuFiltr
        this.detaleUzytkownikówSerwis = detaleUzytkownikówSerwis;
        this.JwtAutoryzacjaEntryPoint = JwtAutoryzacjaEntryPoint; // Przypisz
        this.AutoryzacjaTokenuFiltr = AutoryzacjaTokenuFiltr; // Przypisz
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Wyłącz CSRF
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(JwtAutoryzacjaEntryPoint) // Teraz powinno działać
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Endpointy logowania/rejestracji
                        .requestMatchers("/api/rejestracja/**").permitAll() // Endpoint rejestracji
                        // Możesz dodać inne publiczne ścieżki, np. Swagger UI
                        //.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated() // Wszystkie inne żądania wymagają uwierzytelnienia
                )
                // .httpBasic(Customizer.withDefaults()) // Upewnij się, że jest wyłączone dla JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Sesje bezstanowe
                )
                .authenticationProvider(authenticationProvider()); // Ustaw swojego dostawcę uwierzytelniania

        // Dodaj swój filtr JWT PRZED standardowym filtrem Springa
        http.addFilterBefore(AutoryzacjaTokenuFiltr, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(detaleUzytkownikówSerwis);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}