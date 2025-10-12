package zarzadzanieFinansami.Kontrolery;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Import
import org.springframework.web.bind.annotation.*;
import zarzadzanieFinansami.DTO.kategoria.KategoriaOdpowiedzDTO;
import zarzadzanieFinansami.DTO.kategoria.KategoriaWysylanieDTO;
import zarzadzanieFinansami.magazyn.MagazynUzytkownika; // Import
import zarzadzanieFinansami.modele.Kategoria;
import zarzadzanieFinansami.modele.Uzytkownik; // Import
import zarzadzanieFinansami.modele.enumeracje.KategorieBudzetEnum;
import zarzadzanieFinansami.serwisy.KategoriaUsługa;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;
import zarzadzanieFinansami.wyjątki.DuplikatException;
import zarzadzanieFinansami.wyjątki.ForbiddenAccessException; // Import

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/kategorie")
public class KategoriaKontroler {

    private final KategoriaUsługa kategoriaUsługa;
    private final MagazynUzytkownika magazynUzytkownika; // <-- DODAJ

    @Autowired
    public KategoriaKontroler(KategoriaUsługa kategoriaUsługa, MagazynUzytkownika magazynUzytkownika) { // <-- DODAJ
        this.kategoriaUsługa = kategoriaUsługa;
        this.magazynUzytkownika = magazynUzytkownika; // <-- PRZYPISZ
    }

    // Metoda pomocnicza do mapowania (pozostaje bez zmian)
    private KategoriaOdpowiedzDTO mapToDto(Kategoria kategoria) {
        if (kategoria == null) return null;
        return new KategoriaOdpowiedzDTO(
                kategoria.getId(),
                kategoria.getNazwa(),
                kategoria.getTypTransakcji(),
                kategoria.getKategorieBudzet()
        );
    }

    // Metoda pomocnicza do pobierania bieżącego użytkownika (skopiuj z KontoKontroler)
    private Uzytkownik pobierzBiezacegoUzytkownika(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenAccessException("Użytkownik nie jest uwierzytelniony.");
        }
        String username = authentication.getName();
        Uzytkownik currentUser = magazynUzytkownika.findByNazwa(username); // Upewnij się, że MagazynUzytkownika ma findByNazwa
        if (currentUser == null) {
            // Ten scenariusz nie powinien wystąpić przy poprawnym uwierzytelnieniu, ale warto go obsłużyć
            throw new DaneNieZnalesionoExeption("Nie można zidentyfikować użytkownika: " + username);
        }
        return currentUser;
    }


    // Tworzenie nowej kategorii
    @PostMapping
    public ResponseEntity<?> stworzKategorie(@Valid @RequestBody KategoriaWysylanieDTO dto, Authentication authentication) { // <-- DODAJ Authentication
        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication); // <-- POBIERZ UŻYTKOWNIKA
        try {
            Kategoria nowaKategoria = kategoriaUsługa.stworzKategorie(dto, currentUser); // <-- PRZEKAŻ UŻYTKOWNIKA
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(nowaKategoria));
        } catch (DuplikatException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (DaneNieZnalesionoExeption e) { // Obsługa błędu pobierania użytkownika
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) { // Obsługa błędu braku autentykacji
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
        // Dodaj ogólną obsługę Exception
    }

    // Pobieranie wszystkich kategorii DLA BIEŻĄCEGO UŻYTKOWNIKA
    @GetMapping
    public ResponseEntity<?> pobierzWszystkieKategorie(Authentication authentication) { // <-- DODAJ Authentication
        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication); // <-- POBIERZ UŻYTKOWNIKA
        try {
            List<Kategoria> kategorie = kategoriaUsługa.pobierzWszystkieKategorie(currentUser); // <-- PRZEKAŻ UŻYTKOWNIKA
            List<KategoriaOdpowiedzDTO> dtos = kategorie.stream().map(this::mapToDto).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (DaneNieZnalesionoExeption e) { // Obsługa błędu pobierania użytkownika
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) { // Obsługa błędu braku autentykacji
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
        // Dodaj ogólną obsługę Exception
    }

    // Pobieranie kategorii po ID DLA BIEŻĄCEGO UŻYTKOWNIKA
    @GetMapping("/{id}")
    public ResponseEntity<?> pobierzKategoriePoId(@PathVariable Integer id, Authentication authentication) { // <-- DODAJ Authentication
        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication); // <-- POBIERZ UŻYTKOWNIKA
        try {
            Kategoria kategoria = kategoriaUsługa.pobierzKategoriePoId(id, currentUser); // <-- PRZEKAŻ UŻYTKOWNIKA
            return ResponseEntity.ok(mapToDto(kategoria));
        } catch (DaneNieZnalesionoExeption e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ForbiddenAccessException e) { // Obsługa błędu braku autentykacji
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
        // Dodaj ogólną obsługę Exception
    }

    // Aktualizacja kategorii DLA BIEŻĄCEGO UŻYTKOWNIKA
    // @PreAuthorize("hasRole('ADMIN')") // Jeśli kategorie globalne
    @PutMapping("/{id}")
    public ResponseEntity<?> aktualizujKategorie(@PathVariable Integer id, @Valid @RequestBody KategoriaWysylanieDTO dto, Authentication authentication) { // <-- DODAJ Authentication
        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication); // <-- POBIERZ UŻYTKOWNIKA
        try {
            Kategoria zaktualizowanaKategoria = kategoriaUsługa.aktualizujKategorie(id, dto, currentUser); // <-- PRZEKAŻ UŻYTKOWNIKA
            return ResponseEntity.ok(mapToDto(zaktualizowanaKategoria));
        } catch (DaneNieZnalesionoExeption e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DuplikatException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (ForbiddenAccessException e) { // Obsługa błędu braku uprawnień (jeśli kategoria nie należy do użytkownika)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
        // Dodaj ogólną obsługę Exception
    }

    // Usuwanie kategorii DLA BIEŻĄCEGO UŻYTKOWNIKA
    @DeleteMapping("/{id}")
    public ResponseEntity<?> usunKategorie(@PathVariable Integer id, Authentication authentication) {
        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication); // <-- POBIERZ UŻYTKOWNIKA
        try {
            kategoriaUsługa.usunKategorie(id, currentUser); // <-- PRZEKAŻ UŻYTKOWNIKA
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (DaneNieZnalesionoExeption e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) { // Obsługa błędu, jeśli kategoria jest używana
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ForbiddenAccessException e) { // Obsługa błędu braku uprawnień
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
    @GetMapping("/kategorieBudzet")
    public ResponseEntity<?> pobierzKategoriePoKategoriiBudzetu(@RequestParam KategorieBudzetEnum kategoriaBudzetu, Authentication authentication) {
        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication);
        try {
            List<Kategoria> kategorie = kategoriaUsługa.pobierzKategoriePoKategoriiBudzetu(kategoriaBudzetu, currentUser);
            List<KategoriaOdpowiedzDTO> dtos = kategorie.stream().map(this::mapToDto).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (DaneNieZnalesionoExeption e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}