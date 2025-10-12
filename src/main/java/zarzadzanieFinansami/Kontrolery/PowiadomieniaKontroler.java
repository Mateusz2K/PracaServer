package zarzadzanieFinansami.Kontrolery;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zarzadzanieFinansami.DTO.PowiadomienieOdpowiedzDTO;
import zarzadzanieFinansami.serwisy.PowiadomieniaUsługa;

import java.util.List;

@RestController
@RequestMapping("/api/powiadomienia")
public class PowiadomieniaKontroler {

    private final PowiadomieniaUsługa powiadomieniaUsługa;

    public PowiadomieniaKontroler(PowiadomieniaUsługa powiadomieniaUsługa) {
        this.powiadomieniaUsługa = powiadomieniaUsługa;
    }

    private String getCurrentUsername(Authentication authentication) {
        return authentication.getName();
    }

    @GetMapping
    public ResponseEntity<List<PowiadomienieOdpowiedzDTO>> pobierzWszystkiePowiadomienia(Authentication authentication) {
        String email = getCurrentUsername(authentication);
        List<PowiadomienieOdpowiedzDTO> powiadomienia = powiadomieniaUsługa.pobierzPowiadomieniaDlaUzytkownika(email);
        return ResponseEntity.ok(powiadomienia);
    }

    @GetMapping("/nieprzeczytane")
    public ResponseEntity<List<PowiadomienieOdpowiedzDTO>> pobierzNieprzeczytanePowiadomienia(Authentication authentication) {
        String email = getCurrentUsername(authentication);
        List<PowiadomienieOdpowiedzDTO> powiadomienia = powiadomieniaUsługa.pobierzNieprzeczytanePowiadomieniaDlaUzytkownika(email);
        return ResponseEntity.ok(powiadomienia);
    }

    @PostMapping("/{id}/przeczytaj")
    public ResponseEntity<PowiadomienieOdpowiedzDTO> oznaczJakoPrzeczytaj(@PathVariable Integer id, Authentication authentication) {
        String email = getCurrentUsername(authentication);
        PowiadomienieOdpowiedzDTO powiadomienie = powiadomieniaUsługa.oznaczJakoPrzeczytaj(id, email);
        return ResponseEntity.ok(powiadomienie);
    }

    @PostMapping("/przeczytaj-wszystkie")
    public ResponseEntity<Void> oznaczWszystkieJakoPrzeczytaj(Authentication authentication) {
        String email = getCurrentUsername(authentication);
        powiadomieniaUsługa.oznaczWszystkieJakoPrzeczytaj(email);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> usunPowiadomienie(@PathVariable Integer id, Authentication authentication) {
        String email = getCurrentUsername(authentication);
        powiadomieniaUsługa.usunPowiadomienie(id, email);
        return ResponseEntity.noContent().build();
    }
}