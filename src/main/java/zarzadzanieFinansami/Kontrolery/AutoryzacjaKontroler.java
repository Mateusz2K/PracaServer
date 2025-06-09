package zarzadzanieFinansami.Kontrolery;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails; // Możesz potrzebować, jeśli chcesz pobrać więcej szczegółów
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zarzadzanieFinansami.DTO.logowanie.JwtResponseDTO;
import zarzadzanieFinansami.DTO.logowanie.LoginRequestDTO;
import zarzadzanieFinansami.JWT.JwtUtil; // Upewnij się, że ścieżka do JwtUtil jest poprawna

@RestController
@RequestMapping("/api/auth") // Wspólny prefix dla endpointów autentykacji
public class AutoryzacjaKontroler {

    AuthenticationManager authenticationManager;

    JwtUtil jwtUtil;

    // wstrzyknąć UserDetailsService, jeśli potrzebuje dodatkowych informacji o użytkowniku
    // @Autowired
    // UserDetailsService userDetailsService;

    @Autowired
    public AutoryzacjaKontroler(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {

        // Uwierzytelnij użytkownika za pomocą nazwy i hasła
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getNazwa(), loginRequest.getHasło()));

        // Jeśli uwierzytelnienie się powiodło, ustaw je w kontekście bezpieczeństwa
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Wygeneruj token JWT
        String jwt = jwtUtil.generateJwtToken(authentication);

        // Pobierz szczegóły użytkownika
         UserDetails userDetails = (UserDetails) authentication.getPrincipal();
         String username = userDetails.getUsername();
         // Prostszy sposób, jeśli UserDetails.getUsername() to email

        // Zwróć token w odpowiedzi
        return ResponseEntity.ok(new JwtResponseDTO(jwt, username));
    }

    // dodać inne endpointy, np. /register, /refresh-token itp.
    //  endpoint /register (jeśli go masz) również powinien być publicznie dostępny
    // w konfiguracji Spring Security.
}
