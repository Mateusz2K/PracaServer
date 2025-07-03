// src/main/java/zarzadzanieFinansami/Kontrolery/CelKontroler.java
package zarzadzanieFinansami.Kontrolery;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zarzadzanieFinansami.DTO.cel.CelWysylanieDTO;
import zarzadzanieFinansami.DTO.cel.CelOdpowiedzDTO;
import zarzadzanieFinansami.serwisy.CelUsługa;
import zarzadzanieFinansami.wyjątki.ForbiddenAccessException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cele")
@PreAuthorize("isAuthenticated()") // Zabezpieczenie na poziomie kontrolera
public class CelKontroler {

    private final CelUsługa celUsługa;

    @Autowired
    public CelKontroler(CelUsługa celUsługa) {
        this.celUsługa = celUsługa;
    }

    private String getCurrentUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ForbiddenAccessException("Nie można zidentyfikować użytkownika.");
        }
        return authentication.getName();
    }

    @PostMapping
    public ResponseEntity<CelOdpowiedzDTO> stworzCel(@Valid @RequestBody CelWysylanieDTO dto, Authentication authentication) {
        String emailUzytkownika = getCurrentUsername(authentication);
        CelOdpowiedzDTO stworzonyCel = celUsługa.stworzCel(dto, emailUzytkownika);
        return new ResponseEntity<>(stworzonyCel, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CelOdpowiedzDTO>> pobierzCeleUzytkownika(Authentication authentication) {
        String emailUzytkownika = getCurrentUsername(authentication);
        List<CelOdpowiedzDTO> cele = celUsługa.pobierzCeleUzytkownika(emailUzytkownika);
        return ResponseEntity.ok(cele);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CelOdpowiedzDTO> pobierzCelPoId(@PathVariable Integer id, Authentication authentication) {
        String emailUzytkownika = getCurrentUsername(authentication);
        CelOdpowiedzDTO cel = celUsługa.pobierzCelPoId(id, emailUzytkownika);
        return ResponseEntity.ok(cel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CelOdpowiedzDTO> aktualizujCel(@PathVariable Integer id, @Valid @RequestBody CelWysylanieDTO dto, Authentication authentication) {
        String emailUzytkownika = getCurrentUsername(authentication);
        CelOdpowiedzDTO zaktualizowanyCel = celUsługa.aktualizujCel(id, dto, emailUzytkownika);
        return ResponseEntity.ok(zaktualizowanyCel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> usunCel(@PathVariable Integer id, Authentication authentication) {
        String emailUzytkownika = getCurrentUsername(authentication);
        celUsługa.usunCel(id, emailUzytkownika);
        return ResponseEntity.noContent().build();
    }

    // Dodatkowy endpoint do dodawania środków do celu
    @PostMapping("/{id}/dodaj-srodki")
    public ResponseEntity<CelOdpowiedzDTO> dodajSrodkiDoCelu(@PathVariable Integer id,
                                                             @RequestParam BigDecimal kwota,
                                                             Authentication authentication) {
        String emailUzytkownika = getCurrentUsername(authentication);
        CelOdpowiedzDTO zaktualizowanyCel = celUsługa.dodajSrodkiDoCelu(id, kwota, emailUzytkownika);
        return ResponseEntity.ok(zaktualizowanyCel);
    }
}