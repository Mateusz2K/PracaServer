package zarzadzanieFinansami.Kontrolery;

// import zarzadzanieFinansami.DTO.UzytkownikDTO; // Niepotrzebny import
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus; // Dodaj ten import
import org.springframework.http.ResponseEntity; // Dodaj ten import
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zarzadzanieFinansami.DTO.logowanie.RejestrowanieOdpowiedzDTO;
import zarzadzanieFinansami.DTO.logowanie.RejestrowanieUzytkownikaWysylanieDTO;
import zarzadzanieFinansami.DTO.logowanie.ZmianaUzytkownikaWysylanieDTO;
import zarzadzanieFinansami.serwisy.UzytkownikUsluga;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;
// import java.net.URI; // Potrzebne dla ResponseEntity.created

@RestController
@RequestMapping("/api/rejestracja")
public class RejestrowawnieKontroler {


    private final UzytkownikUsluga uzytkownikUsluga;


    public RejestrowawnieKontroler(UzytkownikUsluga uzytkownikUsluga) {
        this.uzytkownikUsluga = uzytkownikUsluga;
    }

    @PostMapping("/rejestruj")
    // Zwracanie ResponseEntity daje większą kontrolę nad odpowiedzią HTTP
    public ResponseEntity<RejestrowanieOdpowiedzDTO> rejestruj(@Valid @RequestBody RejestrowanieUzytkownikaWysylanieDTO uzytkownik){
        // Wywołanie serwisu jest poprawne zgodnie z jego nową sygnaturą
        // Zakłada, że obiekt 'uzytkownik' z @RequestBody zawiera surowe hasło w polu 'haslo'
        uzytkownikUsluga.stworzUzytkownika(uzytkownik.getNazwa(), uzytkownik.getEmail(), uzytkownik.getHasło());

        RejestrowanieOdpowiedzDTO odpowiedz = new RejestrowanieOdpowiedzDTO("Użytkownik został zarejestrowany", uzytkownik.getNazwa());
        return ResponseEntity.status(HttpStatus.CREATED).body(odpowiedz);
        // Alternatywnie zwrócić np. ID:
        // Uzytkownik stworzony = uzytkownikUsluga.stworzUzytkownika(...);
        // return ResponseEntity.status(HttpStatus.CREATED).body(stworzony.getId()); // Zwraca tylko ID
    }
    @PostMapping("/odzyskaj-dane") // lub np. /api/auth/odzyskaj-dane
    public ResponseEntity<?> odzyskajDaneUzytkownika(@Valid @RequestBody ZmianaUzytkownikaWysylanieDTO requestDTO) {
        try {
            // Załóżmy, że uzytkownikUsluga ma metodę do obsługi tej logiki
            uzytkownikUsluga.zmienDaneLogowaniaPoEmail(requestDTO.getNowyEmail(), requestDTO.getNowaNazwa(), requestDTO.getNoweHaslo());
            return ResponseEntity.ok("Jeśli użytkownik o podanym emailu istnieje, jego dane zostały zaktualizowane. " +
                    "Jeśli podałeś nowe hasło, użyj go przy następnym logowaniu.");
        } catch (DaneNieZnalesionoExeption e) { // Jeśli użytkownik o danym emailu nie istnieje
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) { // Np. jeśli nowa nazwa jest już zajęta
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}