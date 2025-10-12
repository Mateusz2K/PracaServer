package zarzadzanieFinansami.Kontrolery;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zarzadzanieFinansami.DTO.zasady.ZasadyPowiadomienOdpowiedzDTO;
import zarzadzanieFinansami.DTO.zasady.ZasadyPowiadomienWysylanieDTO;
import zarzadzanieFinansami.serwisy.ZasadyPowiadomienUsługa;

import java.util.List;

@RestController
@RequestMapping("/api/zasady-powiadomien")
public class ZasadyPowiadomienKontroler {

    private final ZasadyPowiadomienUsługa zasadyPowiadomienUsługa;

    public ZasadyPowiadomienKontroler(ZasadyPowiadomienUsługa zasadyPowiadomienUsługa) {
        this.zasadyPowiadomienUsługa = zasadyPowiadomienUsługa;
    }

    private String getCurrentUsername(Authentication authentication) {
        return authentication.getName();
    }

    @PostMapping
    public ResponseEntity<ZasadyPowiadomienOdpowiedzDTO> stworzZasade(@Valid @RequestBody ZasadyPowiadomienWysylanieDTO dto, Authentication authentication) {
        String email = getCurrentUsername(authentication);
        ZasadyPowiadomienOdpowiedzDTO nowaZasada = zasadyPowiadomienUsługa.stworzZasade(dto, email);
        return new ResponseEntity<>(nowaZasada, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ZasadyPowiadomienOdpowiedzDTO>> pobierzWszystkieZasady(Authentication authentication) {
        String email = getCurrentUsername(authentication);
        List<ZasadyPowiadomienOdpowiedzDTO> zasady = zasadyPowiadomienUsługa.pobierzZasadyDlaUzytkownika(email);
        return ResponseEntity.ok(zasady);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZasadyPowiadomienOdpowiedzDTO> aktualizujZasade(@PathVariable Integer id,
                                                                         @Valid @RequestBody ZasadyPowiadomienWysylanieDTO dto,
                                                                         Authentication authentication) {
        String email = getCurrentUsername(authentication);
        ZasadyPowiadomienOdpowiedzDTO zaktualizowanaZasada = zasadyPowiadomienUsługa.aktualizujZasade(id, dto, email);
        return ResponseEntity.ok(zaktualizowanaZasada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> usunZasade(@PathVariable Integer id, Authentication authentication) {
        String email = getCurrentUsername(authentication);
        zasadyPowiadomienUsługa.usunZasade(id, email);
        return ResponseEntity.noContent().build();
    }
}
