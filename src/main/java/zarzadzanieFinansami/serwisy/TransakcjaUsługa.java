package zarzadzanieFinansami.serwisy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.domain.Specification;
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

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TransakcjaUsługa {

    private final MagazynTransakcji magazynTransakcji;
    private final MagazynKonta magazynKonta;
    private final MagazynKategorii magazynKategorii;


    @Autowired
    public TransakcjaUsługa(MagazynTransakcji magazynTransakcji, MagazynKonta magazynKonta, MagazynKategorii magazynKategorii) {
        this.magazynTransakcji = magazynTransakcji;
        this.magazynKonta = magazynKonta;
        this.magazynKategorii = magazynKategorii;
    }

    public Optional<Transakcja> pobierzTransakcjePoId(Integer transakcjaId) {
        return magazynTransakcji.findById(transakcjaId);
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
    public List<Transakcja> pobierzTransakcjeWedlugKryteriow(
            Integer kategoriaId, // ID kategorii z DTO
            Integer kontoId,     // ID konta z DTO
            LocalDate dataOd,
            LocalDate dataDo,
            TypTransakcjiEnum typTransakcji,
            Uzytkownik currentUser) {
    
        // --- Walidacja i przygotowanie danych ---
    
        // Sprawdzenie dostępu do konta, jeśli zostało podane
        if (kontoId != null) {
            Konto konto = magazynKonta.findById(kontoId)
                    .orElseThrow(() -> new DaneNieZnalesionoExeption("Konto o ID " + kontoId + " nie zostało znalezione."));
    
            if (!Objects.equals(konto.getUzytkownik().getId(), currentUser.getId())) {
                throw new ForbiddenAccessException("Brak uprawnień do wyświetlenia transakcji dla tego konta.");
            }
        }
    
        // Sprawdzenie, czy kategoria istnieje, jeśli została podana
        if (kategoriaId != null) {
            if (!magazynKategorii.existsById(kategoriaId)) {
                throw new DaneNieZnalesionoExeption("Kategoria o ID " + kategoriaId + " nie została znaleziona.");
            }
            // Można tu dodać weryfikację, czy kategoria należy do użytkownika, jeśli jest taka relacja
//            if(!magazynKategorii.findById(kategoriaId).get().getUzytkownik().equals(currentUser)){
//                throw new ForbiddenAccessException("Brak uprawnień do wyświetlenia transakcji dla tej kategorii.");
//            }
        }
    
        // --- Dynamiczne budowanie zapytania za pomocą Specification ---
        return magazynTransakcji.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Jawne złączenie (join) z encją Konto dla większej kontroli i czytelności
            Join<Transakcja, Konto> kontoJoin = root.join("konto");

            // Podstawowy warunek: transakcje muszą należeć do bieżącego użytkownika
            predicates.add(cb.equal(kontoJoin.get("uzytkownik"), currentUser));
    
            if (kontoId != null) {
                predicates.add(cb.equal(kontoJoin.get("id"), kontoId));
            }
            if (kategoriaId != null) {
                predicates.add(cb.equal(root.get("kategoria").get("id"), kategoriaId));
            }
            if (typTransakcji != null) {
                predicates.add(cb.equal(root.get("typ"), typTransakcji));
            }
    
            // Filtrowanie po dacie
            if (dataOd != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("data"), dataOd));
            }
            if (dataDo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("data"), dataDo));
            }
    
            query.orderBy(cb.desc(root.get("data"))); // Sortowanie wyników
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }


}
