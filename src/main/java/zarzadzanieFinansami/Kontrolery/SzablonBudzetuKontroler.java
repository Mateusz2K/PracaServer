// src/main/java/zarzadzanieFinansami/Kontrolery/SzablonBudzetuKontroler.java
package zarzadzanieFinansami.Kontrolery;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zarzadzanieFinansami.DTO.budzet.szablon.SzablonBudzetuWysylanieDTO;
import zarzadzanieFinansami.DTO.budzet.szablon.SzablonBudzetuOdpowiedzDTO;
import zarzadzanieFinansami.serwisy.SzablonBudzetuUsługa;
import zarzadzanieFinansami.wyjątki.ForbiddenAccessException;

import java.util.List;

@RestController
@RequestMapping("/api/szablony-budzetu")
@PreAuthorize("isAuthenticated()")
public class SzablonBudzetuKontroler {

    private final SzablonBudzetuUsługa szablonBudzetuUsługa;

    @Autowired
    public SzablonBudzetuKontroler(SzablonBudzetuUsługa szablonBudzetuUsługa) {
        this.szablonBudzetuUsługa = szablonBudzetuUsługa;
    }

    private String getCurrentUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ForbiddenAccessException("Nie można zidentyfikować użytkownika.");
        }
        return authentication.getName();
    }

    @PostMapping("/moje") // Tworzenie szablonu przez użytkownika
    public ResponseEntity<SzablonBudzetuOdpowiedzDTO> stworzMojSzablon(
            @Valid @RequestBody SzablonBudzetuWysylanieDTO dto, Authentication authentication) {
        SzablonBudzetuOdpowiedzDTO stworzonySzablon = szablonBudzetuUsługa.stworzSzablonUzytkownika(dto, getCurrentUsername(authentication));
        return new ResponseEntity<>(stworzonySzablon, HttpStatus.CREATED);
    }

    @PostMapping("/systemowe") // Tworzenie szablonu systemowego (np. przez admina)
    @PreAuthorize("hasRole('ADMIN')") // Tylko admin może tworzyć szablony systemowe
    public ResponseEntity<SzablonBudzetuOdpowiedzDTO> stworzSzablonSystemowy(
            @Valid @RequestBody SzablonBudzetuWysylanieDTO dto) {
        SzablonBudzetuOdpowiedzDTO stworzonySzablon = szablonBudzetuUsługa.stworzSzablonSystemowy(dto);
        return new ResponseEntity<>(stworzonySzablon, HttpStatus.CREATED);
    }

    @GetMapping // Pobieranie dostępnych szablonów (systemowe + własne użytkownika)
    public ResponseEntity<List<SzablonBudzetuOdpowiedzDTO>> pobierzDostepneSzablony(Authentication authentication) {
        List<SzablonBudzetuOdpowiedzDTO> szablony = szablonBudzetuUsługa.pobierzDostepneSzablony(getCurrentUsername(authentication));
        return ResponseEntity.ok(szablony);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SzablonBudzetuOdpowiedzDTO> pobierzSzablonPoId(@PathVariable Long id, Authentication authentication) {
        SzablonBudzetuOdpowiedzDTO szablon = szablonBudzetuUsługa.pobierzSzablonPoId(id, getCurrentUsername(authentication));
        return ResponseEntity.ok(szablon);
    }

    @DeleteMapping("/moje/{id}") // Użytkownik usuwa swój szablon
    public ResponseEntity<Void> usunMojSzablon(@PathVariable Long id, Authentication authentication) {
        szablonBudzetuUsługa.usunSzablonUzytkownika(id, getCurrentUsername(authentication));
        return ResponseEntity.noContent().build();
    }

    // TODO: Endpointy do aktualizacji szablonów (osobno dla użytkownika i systemowych przez admina)
    @PutMapping("/moje/{id}") // Użytkownik aktualizuje swój szablon")
    public ResponseEntity<SzablonBudzetuOdpowiedzDTO> zaktualizujMojSzablon(@PathVariable Long id, SzablonBudzetuWysylanieDTO szablonBudzetuWysylanieDTO, Authentication authentication) {
        String uzytkownikNazwa = getCurrentUsername(authentication);
        SzablonBudzetuOdpowiedzDTO zaktualizowanySzablon = szablonBudzetuUsługa.zaktualizujSzablonUzytkownika(id, szablonBudzetuWysylanieDTO, uzytkownikNazwa);
        return ResponseEntity.ok(zaktualizowanySzablon);

    }
}