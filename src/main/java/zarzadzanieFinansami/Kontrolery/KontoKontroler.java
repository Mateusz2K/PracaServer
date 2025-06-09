package zarzadzanieFinansami.Kontrolery;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zarzadzanieFinansami.DTO.konto.KontoResponseDTO;
import zarzadzanieFinansami.DTO.konto.KontoTworzenieDTO; // Zakładam, że masz to DTO
import zarzadzanieFinansami.DTO.konto.KontoUpdateDTO;   // Zakładam, że masz to DTO
import zarzadzanieFinansami.magazyn.MagazynUzytkownika;
import zarzadzanieFinansami.modele.Konto;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.modele.enumeracje.TypKontaEnum;
import zarzadzanieFinansami.modele.enumeracje.WalutaEnum;
import zarzadzanieFinansami.serwisy.KontoUsługa;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;
import zarzadzanieFinansami.wyjątki.ForbiddenAccessException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/konta")
public class KontoKontroler {

    private final KontoUsługa kontoUsluga;
    private final MagazynUzytkownika magazynUzytkownika;

    @Autowired
    public KontoKontroler(KontoUsługa kontoUsluga, MagazynUzytkownika magazynUzytkownika) {
        this.kontoUsluga = kontoUsluga;
        this.magazynUzytkownika = magazynUzytkownika;
    }

    // Metoda pomocnicza do mapowania encji Konto na KontoResponseDTO
    private KontoResponseDTO mapKontoToKontoResponseDTO(Konto konto) {
        if (konto == null) {
            return null;
        }
        return new KontoResponseDTO(
                konto.getId(),
                konto.getNazwa(),
                konto.getBilans(),
                konto.getTyp() != null ? konto.getTyp().name() : null,
                konto.getWaluta() != null ? konto.getWaluta().name() : null,
                konto.getDataUtworzenia(),
                konto.getUzytkownik() != null ? konto.getUzytkownik().getId() : null
        );
    }

    // Metoda pomocnicza do pobierania bieżącego użytkownika
    private Uzytkownik pobierzBiezacegoUzytkownika(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenAccessException("Użytkownik nie jest uwierzytelniony.");
        }
        String username = authentication.getName();
        Uzytkownik currentUser = magazynUzytkownika.findByNazwa(username);
        if (currentUser == null) {
            throw new DaneNieZnalesionoExeption("Nie można zidentyfikować użytkownika: " + username);
        }
        return currentUser;
    }

    // Tworzenie nowego konta
    @PostMapping
    public ResponseEntity<?> stworzKonto(@Valid @RequestBody KontoTworzenieDTO kontoTworzenieDTO, Authentication authentication) {
        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication);
        try {
            // Mapowanie z DTO na encję Konto
            Konto noweKontoEncja = new Konto();
            noweKontoEncja.setNazwa(kontoTworzenieDTO.getNazwa());
            noweKontoEncja.setBilans(kontoTworzenieDTO.getBilans());
            try {
                noweKontoEncja.setTyp(TypKontaEnum.valueOf(kontoTworzenieDTO.getTyp().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Nieprawidłowa wartość dla typu konta: " + kontoTworzenieDTO.getTyp() +
                                ". Dostępne wartości: " + java.util.Arrays.toString(TypKontaEnum.values()));
            }

            try {
                noweKontoEncja.setWaluta(WalutaEnum.valueOf(kontoTworzenieDTO.getWaluta().toUpperCase())); // Dodaj .toUpperCase()
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Nieprawidłowa wartość dla waluty: " + kontoTworzenieDTO.getWaluta() +
                                ". Dostępne wartości: " + java.util.Arrays.toString(WalutaEnum.values()));
            }
            // Data utworzenia i użytkownik zostaną ustawione w serwisie lub przez @PrePersist

            Konto zapisaneKonto = kontoUsluga.stworzenieKontoDlaUzytkownika(currentUser, noweKontoEncja);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapKontoToKontoResponseDTO(zapisaneKonto));
        } catch (DaneNieZnalesionoExeption e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            //Log.error("Błąd tworzenia konta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił błąd podczas tworzenia konta.");
        }
    }


    // ZMODYFIKOWANY ENDPOINT: Pobieranie kont (swoich lub wszystkich dla admina)
    @GetMapping
    public ResponseEntity<?> pobierzWszystkieKonta(Authentication authentication) {
        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication);
        try {
            List<Konto> konta = kontoUsluga.pobierzKontaDlaUzytkownikaLubWszystkieDlaAdmina(currentUser);
            List<KontoResponseDTO> dtos = konta.stream()
                    .map(this::mapKontoToKontoResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (DaneNieZnalesionoExeption e) { // Błąd identyfikacji użytkownika
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) { // Błąd autentykacji
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            // Log.error("Błąd pobierania kont", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił błąd podczas pobierania kont.");
        }
    }

    // ZMODYFIKOWANY ENDPOINT: Pobieranie konta po ID (swojego lub dowolnego dla admina)
    @GetMapping("/{idKonta}")
    public ResponseEntity<?> pobierzKontoPoId(@PathVariable Integer idKonta, Authentication authentication) {
        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication);
        try {
            Konto konto = kontoUsluga.pobierzKontoPoIdDlaUzytkownikaLubAdmina(idKonta, currentUser);
            return ResponseEntity.ok(mapKontoToKontoResponseDTO(konto));
        } catch (DaneNieZnalesionoExeption e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            // Log.error("Błąd pobierania konta po ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił błąd podczas pobierania konta.");
        }
    }

    // Aktualizacja konta (tylko właściciel)
    @PutMapping("/{idKonta}")
    public ResponseEntity<?> aktualizujKonto(@PathVariable Integer idKonta,
                                             @Valid @RequestBody KontoUpdateDTO kontoUpdateDTO,
                                             Authentication authentication) {
        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication);
        try {
            Konto zaktualizowaneKontoEncja = kontoUsluga.updateKonto(idKonta, kontoUpdateDTO, currentUser);
            return ResponseEntity.ok(mapKontoToKontoResponseDTO(zaktualizowaneKontoEncja));
        } catch (DaneNieZnalesionoExeption e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            // Log.error("Błąd aktualizacji konta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił wewnętrzny błąd serwera.");
        }
    }

    // Usuwanie konta (tylko właściciel)
    @DeleteMapping("/{idKonta}")
    public ResponseEntity<?> usunKonto(@PathVariable Integer idKonta, Authentication authentication) {
        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication);
        try {
            kontoUsluga.usunKonto(idKonta, currentUser);
            return ResponseEntity.noContent().build();
        } catch (DaneNieZnalesionoExeption e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            // Log.error("Błąd usuwania konta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił wewnętrzny błąd serwera.");
        }
    }
}