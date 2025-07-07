package zarzadzanieFinansami.Kontrolery;

// import zarzadzanieFinansami.DTO.UzytkownikDTO; // Ten import jest teraz problemem
import zarzadzanieFinansami.DTO.logowanie.UzytkownikOdpowiedzDTO;
import zarzadzanieFinansami.modele.Uzytkownik;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zarzadzanieFinansami.serwisy.UzytkownikUsluga;
// import org.springframework.security.core.Authentication; // Potrzebne do sprawdzania uprawnień użytkownika
// import org.springframework.security.core.context.SecurityContextHolder; // Potrzebne do sprawdzania uprawnień użytkownika


import java.util.List;
import java.util.stream.Collectors; // Potrzebne do mapowania na DTO

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')") // Domyślnie wszystkie metody wymagają roli ADMIN
public class UzytkownikKontroler {

    private final UzytkownikUsluga uzytkownikUsluga;

    @Autowired
    public UzytkownikKontroler(UzytkownikUsluga uzytkownikUsluga){
        this.uzytkownikUsluga = uzytkownikUsluga;
    }

    // --- Endpoint GET /uzytkownik/{id} ---
    @GetMapping("/uzytkownik/{id}")
    // UWAGA: Pozwolenie USER na dostęp do dowolnego ID może być ryzykowne.
    // Rozważ dodanie logiki sprawdzającej, czy USER żąda własnych danych.
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    //  Nigdy nie zwracaj całej encji Uzytkownik
    public ResponseEntity<?> getUzytkownikPrzezId(@PathVariable("id") Integer id){ // Zmieniono typ ID na Integer
        // TODO: Dodać logikę sprawdzającą uprawnienia dla roli USER
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // String currentUsername = authentication.getName();
        // boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        // if (!isAdmin) {
        //    Uzytkownik requestedUser = uzytkownikUsluga.znajdzUzytkownikaPrzezId(id);
        //    if (requestedUser == null || !requestedUser.getEmail().equals(currentUsername)) { // Zakładając, że username to email
        //        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brak uprawnień do wyświetlenia tego użytkownika.");
        //    }
        // }

        Uzytkownik uzytkownik = uzytkownikUsluga.znajdzUzytkownikaPrzezId(id);
        if (uzytkownik == null) {
            return ResponseEntity.notFound().build();
        }
        // Zwróć DTO zamiast encji
        UzytkownikOdpowiedzDTO dto = mapToResponseDTO(uzytkownik); // Potrzebna metoda mapująca
        return ResponseEntity.ok(dto);
    }


    // --- Endpoint POST /uzytkownik ---
    @PostMapping("/uzytkownik")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')") // Dziedziczone z klasy, ale można zostawić
    // Zmieniono parametry na @RequestParam, co jest OK dla prostych danych
    public ResponseEntity<?> stworzUzytkownika(@RequestParam String nazwa, @RequestParam String email, @RequestParam String password){
        try {
            Uzytkownik newUzytkownik = uzytkownikUsluga.stworzUzytkownika(nazwa, email, password);
            // Zwróć DTO bez hasła i status 201 Created
            UzytkownikOdpowiedzDTO dto = mapToResponseDTO(newUzytkownik);
            // Idealnie byłoby dodać URI do nowo utworzonego zasobu
            // URI location = URI.create("/api/uzytkownik/" + newUzytkownik.getId());
            // return ResponseEntity.created(location).body(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            // Obsługa błędu, jeśli np. email już istnieje
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- Endpoint GET /uzytkownik ---
    @GetMapping("/uzytkownik")
    // @PreAuthorize("hasRole('ADMIN')") // Dziedziczone z klasy
    public ResponseEntity<List<UzytkownikOdpowiedzDTO>> znajdzUzytkownikow(){
        List<Uzytkownik> uzytkownicy = uzytkownikUsluga.znajdzWszystkichUzytkownikow();
        // Zmapuj listę encji na listę DTO
        List<UzytkownikOdpowiedzDTO> dtos = uzytkownicy.stream()
                .map(this::mapToResponseDTO) // Użyj metody mapującej
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // --- Endpoint DELETE /uzytkownik/{id} ---
    @DeleteMapping("/uzytkownik/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')") // Dziedziczone
    public ResponseEntity<Void> usunUzytkownika(@PathVariable Integer id){
        try {
            uzytkownikUsluga.usunUzytkownika(id);
            return ResponseEntity.noContent().build(); // 204 No Content - standard dla udanego DELETE bez zwracania treści
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Endpoint PUT /uzytkownik/{id} ---
    @PutMapping("/uzytkownik/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Dziedziczone
    // Zmieniono @RequestBody Uzytkownik na @RequestParam zgodnie z nowym serwisem
    public ResponseEntity<?> wymienUzytkownika(@PathVariable Integer id,
                                               @RequestParam String nowaNazwa,
                                               @RequestParam String nowyEmail,
                                               @RequestParam(required = false) String noweHaslo) { // Hasło opcjonalne
        try {
            Uzytkownik zaktualizowany = uzytkownikUsluga.zmienUzytkownika(id, nowaNazwa, nowyEmail, noweHaslo);
            // Zwróć zaktualizowany zasób jako DTO
            UzytkownikOdpowiedzDTO dto = mapToResponseDTO(zaktualizowany);
            return ResponseEntity.ok(dto);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- Prywatna metoda pomocnicza do mapowania na DTO ---
    private UzytkownikOdpowiedzDTO mapToResponseDTO(Uzytkownik uzytkownik) {
        if (uzytkownik == null) return null;
        // Zakładając, że UzytkownikResponseDTO ma konstruktor (Long id, String name, String email, String rola)
        return new UzytkownikOdpowiedzDTO(
                uzytkownik.getId(),
                uzytkownik.getName(),
                uzytkownik.getEmail(),
                uzytkownik.getRola().name()
        );
    }

}