package zarzadzanieFinansami.serwisy;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import zarzadzanieFinansami.DTO.budzet.BudzetWysylanieDTO;
import zarzadzanieFinansami.DTO.budzet.PozycjaBudzetuWysylanieDTO;
import zarzadzanieFinansami.magazyn.MagazynKategorii;
import zarzadzanieFinansami.magazyn.MagazynTransakcji;
import zarzadzanieFinansami.modele.Kategoria;
import zarzadzanieFinansami.modele.Transakcja;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.modele.enumeracje.TypAlokacjiEnum;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SugestieBudzetoweUsługa {

    @Autowired
    private MagazynTransakcji magazynTransakcji;

    @Autowired
    private MagazynKategorii magazynKategorii; // Aby pobrać wszystkie kategorie użytkownika

    public Map<Kategoria, BigDecimal> obliczSrednieMiesieczneWydatkiNaKategorie(Uzytkownik uzytkownik, LocalDate dataOd, LocalDate dataDo) {
        List<Kategoria> kategorieUzytkownika = magazynKategorii.findByUzytkownik(uzytkownik);
        Map<Kategoria, BigDecimal> srednieWydatki = new HashMap<>();
        long liczbaMiesiecy = ChronoUnit.MONTHS.between(dataOd.withDayOfMonth(1), dataDo.withDayOfMonth(1)) + 1;

        if (liczbaMiesiecy <= 0) {
            return srednieWydatki; // Lub rzuć wyjątek
        }

        for (Kategoria kategoria : kategorieUzytkownika) {
            Specification<Transakcja> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("konto").get("uzytkownik"), uzytkownik));
                predicates.add(cb.equal(root.get("kategoria"), kategoria));
                predicates.add(cb.equal(root.get("typ"), TypTransakcjiEnum.KOSZT));
                predicates.add(cb.between(root.get("data"), dataOd, dataDo));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            List<Transakcja> transakcjeWKategorii = magazynTransakcji.findAll(spec);

            BigDecimal sumaWydatkowWKategorii = transakcjeWKategorii.stream()
                    .map(Transakcja::getKwota)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sredniaDlaKategorii = sumaWydatkowWKategorii.divide(BigDecimal.valueOf(liczbaMiesiecy), 2, RoundingMode.HALF_UP);
            srednieWydatki.put(kategoria, sredniaDlaKategorii);
        }
        return srednieWydatki;
    }

    public BigDecimal obliczSrednieMiesieczneDochody(Uzytkownik uzytkownik, LocalDate dataOd, LocalDate dataDo) {
        long liczbaMiesiecy = ChronoUnit.MONTHS.between(dataOd.withDayOfMonth(1), dataDo.withDayOfMonth(1)) + 1;
        if (liczbaMiesiecy <= 0) return BigDecimal.ZERO;

        Specification<Transakcja> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("konto").get("uzytkownik"), uzytkownik));
            predicates.add(cb.equal(root.get("typ"), TypTransakcjiEnum.PRZYCHÓD));
            predicates.add(cb.between(root.get("data"), dataOd, dataDo));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<Transakcja> dochody = magazynTransakcji.findAll(spec);
        BigDecimal sumaDochodow = dochody.stream().map(Transakcja::getKwota).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sumaDochodow.divide(BigDecimal.valueOf(liczbaMiesiecy), 2, RoundingMode.HALF_UP);
    }

    // Metoda do generowania propozycji budżetu (DTO)
    public BudzetWysylanieDTO generujPropozycjeBudzetu(Uzytkownik uzytkownik, LocalDate dataAnalizyOd, LocalDate dataAnalizyDo, BigDecimal aktualnyPrzewidywanyDochod, TypAlokacjiEnum typAlokacji) {
        Map<Kategoria, BigDecimal> srednieWydatkiNaKategorie = obliczSrednieMiesieczneWydatkiNaKategorie(uzytkownik, dataAnalizyOd, dataAnalizyDo);
        BigDecimal srednieDochodyHistoryczne = obliczSrednieMiesieczneDochody(uzytkownik, dataAnalizyOd, dataAnalizyDo);

        BudzetWysylanieDTO propozycja = new BudzetWysylanieDTO();
        // Ustaw przewidywany dochód (może być podany przez użytkownika lub historyczny)
        BigDecimal dochodDoBudzetowania = (aktualnyPrzewidywanyDochod != null && aktualnyPrzewidywanyDochod.compareTo(BigDecimal.ZERO) > 0)
                ? aktualnyPrzewidywanyDochod : srednieDochodyHistoryczne;
        propozycja.setPrzewidywanyDochod(dochodDoBudzetowania);


        List<PozycjaBudzetuWysylanieDTO> pozycjePropozycji = new ArrayList<>();
        for (Map.Entry<Kategoria, BigDecimal> entry : srednieWydatkiNaKategorie.entrySet()) {
            PozycjaBudzetuWysylanieDTO pozycja = getPozycjaBudzetuRequestDTO(aktualnyPrzewidywanyDochod, typAlokacji,  entry, srednieDochodyHistoryczne);
            pozycjePropozycji.add(pozycja);
        }
        propozycja.setPozycjeBudzetu(pozycjePropozycji);
        return propozycja;
    }

    private static @NotNull PozycjaBudzetuWysylanieDTO getPozycjaBudzetuRequestDTO(BigDecimal aktualnyPrzewidywanyDochod, TypAlokacjiEnum typAlokacji, Map.Entry<Kategoria, BigDecimal> entry, BigDecimal srednieDochodyHistoryczne) {
        Kategoria kategoria = entry.getKey();
        BigDecimal sredniWydatek = entry.getValue();

        PozycjaBudzetuWysylanieDTO pozycja = new PozycjaBudzetuWysylanieDTO();
        pozycja.setKategoriaId(kategoria.getId());


        // Propozycja kwotowa oparta na średniej
        pozycja.setKwotaAlokowana(sredniWydatek);

        pozycja.setTypAlokacji(typAlokacji);

        // Propozycja procentowa oparta na historycznym udziale w dochodach
        if (srednieDochodyHistoryczne.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal procentWDochodach = sredniWydatek
                    .multiply(BigDecimal.valueOf(100))
                    .divide(srednieDochodyHistoryczne, 2, RoundingMode.HALF_UP);
            pozycja.setProcentAlokowany(procentWDochodach);

            // Jeśli użytkownik podał aktualny dochód, przelicz sugerowaną kwotę
            if (aktualnyPrzewidywanyDochod != null && aktualnyPrzewidywanyDochod.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal kwotaNaPodstawieProcentuIAktualnegoDochodu = procentWDochodach
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP) // Dzielimy procent przez 100
                        .multiply(aktualnyPrzewidywanyDochod)
                        .setScale(2, RoundingMode.HALF_UP);
                // Możemy zdecydować, którą kwotę zasugerować - np. tę z procentu od aktualnego dochodu
                pozycja.setKwotaAlokowana(kwotaNaPodstawieProcentuIAktualnegoDochodu);
            }
        }
        return pozycja;
    }
}
