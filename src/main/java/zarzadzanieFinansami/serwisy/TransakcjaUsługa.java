package zarzadzanieFinansami.serwisy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zarzadzanieFinansami.DTO.transakcja.TransakcjaTworzenieDTO;
import zarzadzanieFinansami.magazyn.MagazynKategorii;
import zarzadzanieFinansami.magazyn.MagazynKonta;
import zarzadzanieFinansami.magazyn.MagazynTransakcji;
import zarzadzanieFinansami.modele.Kategoria;
import zarzadzanieFinansami.modele.Konto;
import zarzadzanieFinansami.modele.Transakcja;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;
import zarzadzanieFinansami.wyjątki.ForbiddenAccessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

@Service
public class TransakcjaUsługa {

    private final MagazynTransakcji magazynTransakcji;
    private final MagazynKonta magazynKonta;
    private final MagazynKategorii magazynKategorii;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");


    @Autowired
    public TransakcjaUsługa(MagazynTransakcji magazynTransakcji, MagazynKonta magazynKonta, MagazynKategorii magazynKategorii) {
        this.magazynTransakcji = magazynTransakcji;
        this.magazynKonta = magazynKonta;
        this.magazynKategorii = magazynKategorii;
    }
    @Transactional // Kluczowe dla spójności danych!
    public Transakcja dodajTransakcjeDoKonta(Integer kontoId, TransakcjaTworzenieDTO dto, Uzytkownik currentUser) {
        // 1. Znajdź konto
        Konto konto = magazynKonta.findById(kontoId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Konto o ID " + kontoId + " nie zostało znalezione."));

        // 2. Weryfikuj, czy zalogowany użytkownik jest właścicielem konta
        if (!Objects.equals(konto.getUzytkownik().getId(), currentUser.getId())) {
            throw new ForbiddenAccessException("Brak uprawnień do dodania transakcji do tego konta.");
        }

        // 3. Znajdź kategorię
        Kategoria kategoria = magazynKategorii.findById(dto.getKategoriaId())
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Kategoria o ID " + dto.getKategoriaId() + " nie została znaleziona."));
        // Można dodać weryfikację, czy kategoria należy do użytkownika, jeśli kategorie są per użytkownik

        // 4. Stwórz nową transakcję
        Transakcja nowaTransakcja = new Transakcja();
        nowaTransakcja.setOpis(dto.getOpis());
        nowaTransakcja.setKwota(dto.getKwota());
        nowaTransakcja.setTyp(dto.getTyp());
        nowaTransakcja.setData(dto.getData());
        nowaTransakcja.setKategoria(kategoria);
        nowaTransakcja.setKonto(konto);

        // 5. Zaktualizuj bilans konta
        BigDecimal nowyBilans;
        if (dto.getTyp() == TypTransakcjiEnum.PRZYCHÓD) {
            nowyBilans = konto.getBilans().add(dto.getKwota());
        } else if (dto.getTyp() == TypTransakcjiEnum.KOSZT) {
            nowyBilans = konto.getBilans().subtract(dto.getKwota());
        } else {
            throw new IllegalArgumentException("Nieznany typ transakcji: " + dto.getTyp());
        }
        konto.setBilans(nowyBilans);

        // 6. Zapisz zmiany
        magazynKonta.save(konto); // Zapisz zaktualizowane konto (z nowym bilansem)
        return magazynTransakcji.save(nowaTransakcja); // Zapisz nową transakcję
    }

    @Transactional(readOnly = true) // Operacja tylko do odczytu
    public List<Transakcja> pobierzTransakcjeDlaKonta(Integer kontoId, Uzytkownik currentUser) {
        Konto konto = magazynKonta.findById(kontoId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Konto o ID " + kontoId + " nie zostało znalezione."));

        if (!Objects.equals(konto.getUzytkownik().getId(), currentUser.getId())) {
            throw new ForbiddenAccessException("Brak uprawnień do wyświetlenia transakcji dla tego konta.");
        }
        // Możesz potrzebować metody w MagazynTransakcji lub wykorzystać relację
        // return magazynTransakcji.findByKontoId(kontoId); // Jeśli masz taką metodę
        return konto.getTransakcje(); // Jeśli relacja Konto -> Transakcje jest EAGER lub zainicjowana
        // Dla LAZY, to wywoła dodatkowe zapytanie, co jest OK w transakcji.
        // Upewnij się, że masz `private List<Transakcja> transakcje;` w encji Konto
        // z `@OneToMany(mappedBy = "konto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)`
    }


    @Transactional
    public void usunTransakcje(Integer transakcjaId, Uzytkownik currentUser) {
        Transakcja transakcja = magazynTransakcji.findById(transakcjaId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Transakcja o ID " + transakcjaId + " nie została znaleziona."));

        Konto konto = transakcja.getKonto();
        if (!Objects.equals(konto.getUzytkownik().getId(), currentUser.getId())) {
            throw new ForbiddenAccessException("Brak uprawnień do usunięcia tej transakcji.");
        }

        // Odwróć wpływ transakcji na bilans konta
        BigDecimal kwotaTransakcji = transakcja.getKwota();
        if (transakcja.getTyp() == TypTransakcjiEnum.PRZYCHÓD) {
            konto.setBilans(konto.getBilans().subtract(kwotaTransakcji));
        } else if (transakcja.getTyp() == TypTransakcjiEnum.KOSZT) {
            konto.setBilans(konto.getBilans().add(kwotaTransakcji));
        }

        magazynKonta.save(konto);
        magazynTransakcji.delete(transakcja);
    }

    @Transactional(readOnly = true)
    public List<Transakcja> pobierzTransakcjeDlaOkresu(String dataOdStr, String dataDoStr, Uzytkownik currentUser) {
        LocalDate dataOd;
        LocalDate dataDo;

        try {
            dataOd = LocalDate.parse(dataOdStr, DATE_FORMATTER);
            dataDo = LocalDate.parse(dataDoStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Nieprawidłowy format daty. Oczekiwano formatu YYYY-MM-DD (np. 2023-01-15). Błąd: " + e.getMessage());
        }

        if (dataOd.isAfter(dataDo)) {
            throw new IllegalArgumentException("Data 'od' (" + dataOdStr + ") nie może być późniejsza niż data 'do' (" + dataDoStr + ").");
        }

        return magazynTransakcji.findByKonto_UzytkownikAndDataBetweenOrderByDataDesc(currentUser, dataOd, dataDo);
    }
    @Transactional(readOnly = true)
    public List<Transakcja> pobierzTransakcjeDlaKontaWOkresie(Integer kontoId, String dataOdStr, String dataDoStr, Uzytkownik currentUser) {
        // 1. Znajdź konto
        Konto konto = magazynKonta.findById(kontoId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Konto o ID " + kontoId + " nie zostało znalezione."));

        // 2. Weryfikuj, czy zalogowany użytkownik jest właścicielem konta
        // Użyj .equals() do porównywania obiektów Integer, a nie ==
        if (konto.getUzytkownik() == null || !konto.getUzytkownik().getId().equals(currentUser.getId())) {
            throw new ForbiddenAccessException("Brak uprawnień do wyświetlenia transakcji dla tego konta.");
        }

        // 3. Parsuj daty (tak jak w metodzie pobierzTransakcjeDlaOkresu)
        LocalDate dataOd;
        LocalDate dataDo;
        try {
            dataOd = LocalDate.parse(dataOdStr, DATE_FORMATTER);
            dataDo = LocalDate.parse(dataDoStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Nieprawidłowy format daty. Oczekiwano formatu YYYY-MM-DD (np. 2023-01-15). Błąd: " + e.getMessage());
        }

        if (dataOd.isAfter(dataDo)) {
            throw new IllegalArgumentException("Data 'od' (" + dataOdStr + ") nie może być późniejsza niż data 'do' (" + dataDoStr + ").");
        }

        // 4. Wywołaj nową metodę repozytorium
        return magazynTransakcji.findByKontoAndDataBetweenOrderByDataDesc(konto, dataOd, dataDo);
    }
    @Transactional(readOnly = true)
    public List<Transakcja> pobierzTransakcjeWedlugKryteriow(
            Integer kategoriaId, // ID kategorii z DTO
            Integer kontoId,     // ID konta z DTO
            String dataOdStr,
            String dataDoStr,
            Uzytkownik currentUser) {

        LocalDate dataOd = null;
        LocalDate dataDo = null;

        if (dataOdStr != null && !dataOdStr.trim().isEmpty()) {
            try {
                dataOd = LocalDate.parse(dataOdStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Nieprawidłowy format daty 'od'. Oczekiwano formatu dd.MM.yyyy. Błąd: " + e.getMessage());
            }
        }

        if (dataDoStr != null && !dataDoStr.trim().isEmpty()) {
            try {
                dataDo = LocalDate.parse(dataDoStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Nieprawidłowy format daty 'do'. Oczekiwano formatu dd.MM.yyyy. Błąd: " + e.getMessage());
            }
        }

        if (dataOd != null && dataDo != null && dataOd.isAfter(dataDo)) {
            throw new IllegalArgumentException("Data 'od' (" + dataOdStr + ") nie może być późniejsza niż data 'do' (" + dataDoStr + ").");
        }

        Kategoria kategoria = null;
        if (kategoriaId != null) {
            kategoria = magazynKategorii.findById(kategoriaId)
                    .orElseThrow(() -> new DaneNieZnalesionoExeption("Kategoria o ID " + kategoriaId + " nie została znaleziona."));
            // Opcjonalnie: Weryfikacja, czy kategoria należy do użytkownika, jeśli jest taka potrzeba
            // if (kategoria.getUzytkownik() != null && !Objects.equals(kategoria.getUzytkownik().getId(), currentUser.getId())) {
            //     throw new ForbiddenAccessException("Brak uprawnień do użycia tej kategorii.");
            // }
        }

        if (kontoId != null) {
            // Scenariusz: Podano ID konta
            Konto konto = magazynKonta.findById(kontoId)
                    .orElseThrow(() -> new DaneNieZnalesionoExeption("Konto o ID " + kontoId + " nie zostało znalezione."));

            if (konto.getUzytkownik() == null || !konto.getUzytkownik().getId().equals(currentUser.getId())) {
                throw new ForbiddenAccessException("Brak uprawnień do wyświetlenia transakcji dla tego konta.");
            }

            if (kategoria != null) {
                // Podano konto i kategorię
                if (dataOd != null && dataDo != null) {
                    return magazynTransakcji.findByKontoAndKategoriaAndDataBetweenOrderByDataDesc(konto, kategoria, dataOd, dataDo);
                } else if (dataOd != null) {
                    return magazynTransakcji.findByKontoAndKategoriaAndDataGreaterThanEqualOrderByDataDesc(konto, kategoria, dataOd);
                } else if (dataDo != null) {
                    return magazynTransakcji.findByKontoAndKategoriaAndDataLessThanEqualOrderByDataDesc(konto, kategoria, dataDo);
                } else {
                    return magazynTransakcji.findByKontoAndKategoriaOrderByDataDesc(konto, kategoria);
                }
            } else {
                // Podano konto, ale nie kategorię
                if (dataOd != null && dataDo != null) {
                    return magazynTransakcji.findByKontoAndDataBetweenOrderByDataDesc(konto, dataOd, dataDo);
                } else if (dataOd != null) {
                    return magazynTransakcji.findByKontoAndDataGreaterThanEqualOrderByDataDesc(konto, dataOd);
                } else if (dataDo != null) {
                    return magazynTransakcji.findByKontoAndDataLessThanEqualOrderByDataDesc(konto, dataDo);
                } else {
                    return magazynTransakcji.findByKontoOrderByDataDesc(konto);
                }
            }
        } else {
            // Scenariusz: Nie podano ID konta (wszystkie konta użytkownika)
            if (kategoria != null) {
                // Nie podano konta, ale podano kategorię
                if (dataOd != null && dataDo != null) {
                    return magazynTransakcji.findByKonto_UzytkownikAndKategoriaAndDataBetweenOrderByDataDesc(currentUser, kategoria, dataOd, dataDo);
                } else if (dataOd != null) {
                    return magazynTransakcji.findByKonto_UzytkownikAndKategoriaAndDataGreaterThanEqualOrderByDataDesc(currentUser, kategoria, dataOd);
                } else if (dataDo != null) {
                    return magazynTransakcji.findByKonto_UzytkownikAndKategoriaAndDataLessThanEqualOrderByDataDesc(currentUser, kategoria, dataDo);
                } else {
                    return magazynTransakcji.findByKonto_UzytkownikAndKategoriaOrderByDataDesc(currentUser, kategoria);
                }
            } else {
                // Nie podano konta ani kategorii
                if (dataOd != null && dataDo != null) {
                    return magazynTransakcji.findByKonto_UzytkownikAndDataBetweenOrderByDataDesc(currentUser, dataOd, dataDo);
                } else if (dataOd != null) {
                    return magazynTransakcji.findByKonto_UzytkownikAndDataGreaterThanEqualOrderByDataDesc(currentUser, dataOd);
                } else if (dataDo != null) {
                    return magazynTransakcji.findByKonto_UzytkownikAndDataLessThanEqualOrderByDataDesc(currentUser, dataDo);
                } else {
                    return magazynTransakcji.findByKonto_UzytkownikOrderByDataDesc(currentUser);
                }
            }
        }
    }
}
