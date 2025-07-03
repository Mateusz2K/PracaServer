// src/main/java/zarzadzanieFinansami/Kontrolery/BudzetKontroler.java
package zarzadzanieFinansami.Kontrolery;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zarzadzanieFinansami.DTO.budzet.BudzetWysylanieDTO;
import zarzadzanieFinansami.DTO.budzet.BudzetOdpowiedzDTO;
import zarzadzanieFinansami.serwisy.BudzetUsługa;
import zarzadzanieFinansami.wyjątki.ForbiddenAccessException;

import java.util.List;

@RestController
@RequestMapping("/api/budzety")
@PreAuthorize("isAuthenticated()")
public class BudzetKontroler {

    private final BudzetUsługa budzetUsługa;

    @Autowired
    public BudzetKontroler(BudzetUsługa budzetUsługa) {
        this.budzetUsługa = budzetUsługa;
    }

    private String getCurrentUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ForbiddenAccessException("Nie można zidentyfikować użytkownika.");
        }
        return authentication.getName();
    }

    @PostMapping
    public ResponseEntity<BudzetOdpowiedzDTO> stworzBudzet(@Valid @RequestBody BudzetWysylanieDTO dto, Authentication authentication) {
        BudzetOdpowiedzDTO stworzonyBudzet = budzetUsługa.stworzBudzet(dto, getCurrentUsername(authentication));
        return new ResponseEntity<>(stworzonyBudzet, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BudzetOdpowiedzDTO>> pobierzMojeBudzety(Authentication authentication) {
        List<BudzetOdpowiedzDTO> budzety = budzetUsługa.pobierzBudzetyUzytkownika(getCurrentUsername(authentication));
        return ResponseEntity.ok(budzety);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudzetOdpowiedzDTO> pobierzBudzetPoId(@PathVariable Long id, Authentication authentication) {
        BudzetOdpowiedzDTO budzet = budzetUsługa.pobierzBudzetPoId(id, getCurrentUsername(authentication));
        return ResponseEntity.ok(budzet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudzetOdpowiedzDTO> aktualizujBudzet(@PathVariable Long id,
                                                               @Valid @RequestBody BudzetWysylanieDTO dto,
                                                               Authentication authentication) {
        BudzetOdpowiedzDTO zaktualizowanyBudzet = budzetUsługa.aktualizujBudzet(id, dto, getCurrentUsername(authentication));
        return ResponseEntity.ok(zaktualizowanyBudzet);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> usunBudzet(@PathVariable Long id, Authentication authentication) {
        budzetUsługa.usunBudzet(id, getCurrentUsername(authentication));
        return ResponseEntity.noContent().build();
    }
}