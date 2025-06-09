package zarzadzanieFinansami.Kontrolery;

// import zarzadzanieFinansami.DTO.UzytkownikDTO; // Niepotrzebny import
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus; // Dodaj ten import
import org.springframework.http.ResponseEntity; // Dodaj ten import
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zarzadzanieFinansami.DTO.logowanie.RejestrowanieUzytkownikaRequestDTO;
import zarzadzanieFinansami.serwisy.UzytkownikUsluga;
// import java.net.URI; // Potrzebne dla ResponseEntity.created

@RestController
@RequestMapping("/api")
public class RejestrowawnieKontroler {


    private final UzytkownikUsluga uzytkownikUsluga;


    public RejestrowawnieKontroler(UzytkownikUsluga uzytkownikUsluga) {
        this.uzytkownikUsluga = uzytkownikUsluga;
    }

    @PostMapping("/rejestr")
    // Zwracanie ResponseEntity daje większą kontrolę nad odpowiedzią HTTP
    public ResponseEntity<Void> rejestruj(@Valid @RequestBody RejestrowanieUzytkownikaRequestDTO uzytkownik){
        // Wywołanie serwisu jest poprawne zgodnie z jego nową sygnaturą
        // Zakłada, że obiekt 'uzytkownik' z @RequestBody zawiera surowe hasło w polu 'haslo'
        uzytkownikUsluga.stworzUzytkownika(uzytkownik.getNazwa(), uzytkownik.getEmail(), uzytkownik.getHasło());


        return ResponseEntity.status(HttpStatus.CREATED).build();
        // Alternatywnie zwrócić np. ID:
        // Uzytkownik stworzony = uzytkownikUsluga.stworzUzytkownika(...);
        // return ResponseEntity.status(HttpStatus.CREATED).body(stworzony.getId()); // Zwraca tylko ID
    }
}