package zarzadzanieFinansami.Kontrolery;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zarzadzanieFinansami.DTO.transakcja.TransakcjaPobieranieDTO; // Nowe DTO
import zarzadzanieFinansami.DTO.transakcja.TransakcjaOdpowiedzDTO;
import zarzadzanieFinansami.DTO.transakcja.TransakcjaTworzenieDTO;
import zarzadzanieFinansami.magazyn.MagazynUzytkownika;
import zarzadzanieFinansami.modele.Transakcja;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.serwisy.TransakcjaUsługa;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;
import zarzadzanieFinansami.wyjątki.ForbiddenAccessException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TransakcjaKontroler {

    private final TransakcjaUsługa transakcjaUsługa;
    private final MagazynUzytkownika magazynUzytkownika;

    @Autowired
    public TransakcjaKontroler(TransakcjaUsługa transakcjaUsługa, MagazynUzytkownika magazynUzytkownika) {
        this.transakcjaUsługa = transakcjaUsługa;
        this.magazynUzytkownika = magazynUzytkownika;
    }

    private TransakcjaOdpowiedzDTO mapToDto(Transakcja transakcja) {
        if (transakcja == null) return null;
        return new TransakcjaOdpowiedzDTO(
                transakcja.getId(),
                transakcja.getOpis(),
                transakcja.getKwota(),
                transakcja.getTyp(),
                transakcja.getData(),
                transakcja.getKategoria() != null ? transakcja.getKategoria().getId() : null,
                transakcja.getKategoria() != null ? transakcja.getKategoria().getNazwa() : "Brak kategorii",
                transakcja.getKonto() != null ? transakcja.getKonto().getId() : null
        );
    }

    // Endpoint do dodawania transakcji do konkretnego konta
    @PostMapping("transakcje")
    public ResponseEntity<?> dodajTransakcje(@Valid @RequestBody TransakcjaTworzenieDTO dto,
                                             Authentication authentication) {
        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication);
        try {
            if (dto.getKontoId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Konto nie może być puste.");
            }
            Transakcja nowaTransakcja = transakcjaUsługa.dodajTransakcjeDoKonta(dto.getKontoId(), dto, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(nowaTransakcja));
        } catch (DaneNieZnalesionoExeption e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Log.error("Nieoczekiwany błąd podczas dodawania transakcji dla konta {}", kontoId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił nieoczekiwany błąd serwera.");
        }
    }

    // NOWY ENDPOINT do dynamicznego pobierania transakcji
    @PostMapping("/transakcje/pobierz")
    public ResponseEntity<?> pobierzTransakcjeDynamicznie(
            @RequestBody TransakcjaPobieranieDTO kryteria, // Klient powinien wysłać przynajmniej pusty obiekt {}
            Authentication authentication) {

        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication);
        List<Transakcja> transakcje;

        try {
            transakcje = transakcjaUsługa.pobierzTransakcjeWedlugKryteriow(
                    kryteria.getKategoriaId(),
                    kryteria.getKontoId(),
                    kryteria.getDataOd(),
                    kryteria.getDataDo(),
                    currentUser
            );

            List<TransakcjaOdpowiedzDTO> dtos = transakcje.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);

        } catch (DaneNieZnalesionoExeption e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // logger.error("Nieoczekiwany błąd podczas dynamicznego pobierania transakcji", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił nieoczekiwany błąd serwera.");
        }
    }


    // Endpoint do pobierania transakcji dla konkretnego konta (może zostać zastąpiony przez /transakcje/pobierz)
//    @GetMapping("/konta/{kontoId}/transakcje")
//    public ResponseEntity<?> pobierzTransakcjeDlaKonta(@PathVariable Integer kontoId, Authentication authentication) {
//        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication);
//        try {
//            // Można by wywołać nową metodę serwisową z odpowiednimi parametrami
//            // List<Transakcja> transakcje = transakcjaUsługa.pobierzTransakcjeWedlugKryteriow(kontoId, null, null, currentUser);
//            List<Transakcja> transakcje = transakcjaUsługa.pobierzTransakcjeDlaKonta(kontoId, currentUser);
//            List<TransakcjaResponseDTO> dtos = transakcje.stream().map(this::mapToDto).collect(Collectors.toList());
//            return ResponseEntity.ok(dtos);
//        } catch (DaneNieZnalesionoExeption e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        } catch (ForbiddenAccessException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        }
//    }

    // Endpoint do usuwania transakcji
    @DeleteMapping("/transakcje/{transakcjaId}")
    public ResponseEntity<?> usunTransakcje(@PathVariable Integer transakcjaId, Authentication authentication) {
        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication);
        try {
            transakcjaUsługa.usunTransakcje(transakcjaId, currentUser);
            return ResponseEntity.noContent().build();
        } catch (DaneNieZnalesionoExeption e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // Endpoint do pobierania transakcji użytkownika dla okresu (może zostać zastąpiony przez /transakcje/pobierz)
//    @GetMapping("/transakcje/okres")
//    public ResponseEntity<?> getTransakcjeDlaOkresuUrzytkownika(Authentication authentication,
//                                                                @RequestParam String dataOd,
//                                                                @RequestParam String dataDo) {
//        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication);
//        try {
//            // Można by wywołać nową metodę serwisową z odpowiednimi parametrami
//            // List<Transakcja> transakcje = transakcjaUsługa.pobierzTransakcjeWedlugKryteriow(null, dataOd, dataDo, currentUser);
//            List<Transakcja> transakcje = transakcjaUsługa.pobierzTransakcjeDlaOkresu(dataOd, dataDo, currentUser);
//            List<TransakcjaResponseDTO> dtos = transakcje.stream()
//                    .map(this::mapToDto)
//                    .collect(Collectors.toList());
//            return ResponseEntity.ok(dtos);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        } catch (DaneNieZnalesionoExeption | ForbiddenAccessException e) { // Połączone dla uproszczenia
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
//        } catch (Exception e) {
//            //logger.error("Nieoczekiwany błąd podczas pobierania transakcji dla okresu", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił nieoczekiwany błąd serwera.");
//        }
//    }
//
//    // Endpoint do pobierania transakcji dla konta w okresie (może zostać zastąpiony przez /transakcje/pobierz)
//    @GetMapping("/konta/{kontoId}/transakcje/okres")
//    public ResponseEntity<?> getTransakcjeDlaKontaWOkresie(
//            Authentication authentication,
//            @PathVariable Integer kontoId,
//            @RequestParam String dataOd,
//            @RequestParam String dataDo) {
//
//        Uzytkownik currentUser = pobierzBiezacegoUzytkownika(authentication);
//        try {
//            // Można by wywołać nową metodę serwisową z odpowiednimi parametrami
//            // List<Transakcja> transakcje = transakcjaUsługa.pobierzTransakcjeWedlugKryteriow(kontoId, dataOd, dataDo, currentUser);
//            List<Transakcja> transakcje = transakcjaUsługa.pobierzTransakcjeDlaKontaWOkresie(kontoId, dataOd, dataDo, currentUser);
//            List<TransakcjaResponseDTO> dtos = transakcje.stream()
//                    .map(this::mapToDto)
//                    .collect(Collectors.toList());
//            return ResponseEntity.ok(dtos);
//        } catch (DaneNieZnalesionoExeption e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        } catch (ForbiddenAccessException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        } catch (Exception e) {
//            //logger.error("Nieoczekiwany błąd podczas pobierania transakcji dla konta w okresie", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił nieoczekiwany błąd serwera.");
//        }
//    }

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
}