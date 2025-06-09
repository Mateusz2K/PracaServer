package zarzadzanieFinansami.serwisy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zarzadzanieFinansami.DTO.konto.KontoUpdateDTO; // Jeśli masz DTO do aktualizacji konta
import zarzadzanieFinansami.magazyn.MagazynKonta;
import zarzadzanieFinansami.modele.Konto;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.modele.enumeracje.RolaEnum; // Importuj RolaEnum
import zarzadzanieFinansami.modele.enumeracje.TypKontaEnum;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;
import zarzadzanieFinansami.wyjątki.ForbiddenAccessException;

import java.util.List;
import java.util.Objects;

@Service
public class KontoUsługa {

    Logger logger = LoggerFactory.getLogger(KontoUsługa.class);

    private final MagazynKonta magazynKonta;

    @Autowired
    public KontoUsługa(MagazynKonta magazynKonta) {
        this.magazynKonta = magazynKonta;
    }

    // Metoda do tworzenia konta (pozostaje bez zmian, konto jest zawsze tworzone dla currentUser)
    @Transactional
    public Konto stworzenieKontoDlaUzytkownika(Uzytkownik uzytkownik, Konto konto) {
        konto.setUzytkownik(uzytkownik);
        Konto zapisaneKonto = magazynKonta.save(konto);
        logger.info("Pomyślnie utworzono konto o ID: {} dla użytkownika: {}. Body konta: {}", zapisaneKonto.getId(), uzytkownik.getName(), zapisaneKonto.toString()); // Logowanie sukcesu
        // konto.setDataUtworzenia(); // Jeśli masz metodę @PrePersist, to jest zbędne tutaj
        return magazynKonta.save(konto);
    }


    // ZMODYFIKOWANA METODA: Pobieranie kont
    @Transactional(readOnly = true)
    public List<Konto> pobierzKontaDlaUzytkownikaLubWszystkieDlaAdmina(Uzytkownik currentUser) {
        if (currentUser.getRola() == RolaEnum.ADMIN) {
            return magazynKonta.findAll(); // Admin widzi wszystkie konta
        } else {
            return magazynKonta.findByUzytkownik(currentUser); // Zwykły użytkownik widzi tylko swoje
        }
    }

    // ZMODYFIKOWANA METODA: Pobieranie konta po ID
    @Transactional(readOnly = true)
    public Konto pobierzKontoPoIdDlaUzytkownikaLubAdmina(Integer kontoId, Uzytkownik currentUser) {
        Konto konto = magazynKonta.findById(kontoId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Konto o ID " + kontoId + " nie zostało znalezione."));

        if (currentUser.getRola() == RolaEnum.ADMIN) {
            return konto; // Admin może zobaczyć każde konto
        } else {
            // Zwykły użytkownik może zobaczyć tylko swoje konto
            if (konto.getUzytkownik() != null && Objects.equals(konto.getUzytkownik().getId(), currentUser.getId())) {
                return konto;
            } else {
                // Jeśli konto nie należy do użytkownika, a użytkownik nie jest adminem
                throw new ForbiddenAccessException("Brak uprawnień do wyświetlenia tego konta.");
            }
        }
    }


    // Metoda aktualizacji konta - tutaj admin NIE POWINIEN domyślnie móc aktualizować kont innych użytkowników
    // chyba że jest to jawnie wymagane i przemyślane. Na razie zostawiamy jak było - tylko właściciel.
    @Transactional
    public Konto updateKonto(Integer kontoId, KontoUpdateDTO updateDTO, Uzytkownik currentUser) {
        Konto kontoDoAktualizacji = magazynKonta.findById(kontoId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Konto o ID " + kontoId + " nie zostało znalezione."));

        if (kontoDoAktualizacji.getUzytkownik() == null || !Objects.equals(kontoDoAktualizacji.getUzytkownik().getId(), currentUser.getId())) {
            throw new ForbiddenAccessException("Brak uprawnień do modyfikacji tego konta.");
        }

        if (updateDTO.getNazwa() != null && !updateDTO.getNazwa().isBlank()) {
            kontoDoAktualizacji.setNazwa(updateDTO.getNazwa());
        }
        if (updateDTO.getTypKonta() != null && !updateDTO.getTypKonta().isBlank()) {
            try {
                kontoDoAktualizacji.setTyp(TypKontaEnum.valueOf(updateDTO.getTypKonta().toUpperCase()));
                }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Nieprawidłowa wartość dla typu konta");
            }
        }
//        jeśli w wymaganiach potrzeba zmianę waluty
//        if (updateDTO.getWaluta() != null && !updateDTO.getWaluta().isBlank()) {
//            try {
//                kontoDoAktualizacji.setWaluta(WalutaEnum.valueOf(updateDTO.getWaluta().toUpperCase()));
//            } catch (IllegalArgumentException e) {
//                throw new IllegalArgumentException("Nieprawidłowa wartość dla waluty: " + updateDTO.getWaluta());
//            }
//        }
        if (updateDTO.getBilans() != null) {
            kontoDoAktualizacji.setBilans(updateDTO.getBilans());
        }
        return magazynKonta.save(kontoDoAktualizacji);
    }

    // Metoda usuwania konta - podobnie jak update, tylko właściciel.
    @Transactional
    public void usunKonto(Integer kontoId, Uzytkownik currentUser) {
        Konto kontoDoUsuniecia = magazynKonta.findById(kontoId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Konto o ID " + kontoId + " nie zostało znalezione."));

        if (kontoDoUsuniecia.getUzytkownik() == null || !Objects.equals(kontoDoUsuniecia.getUzytkownik().getId(), currentUser.getId())) {
            throw new ForbiddenAccessException("Brak uprawnień do usunięcia tego konta.");
        }
        magazynKonta.delete(kontoDoUsuniecia);
    }
}