package zarzadzanieFinansami.serwisy;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zarzadzanieFinansami.DTO.budzet.BudzetRequestDTO;
import zarzadzanieFinansami.DTO.budzet.PozycjaBudzetuRequestDTO;
import zarzadzanieFinansami.magazyn.MagazynKategorii;
import zarzadzanieFinansami.magazyn.MagazynTransakcji;
import zarzadzanieFinansami.modele.Kategoria;
import zarzadzanieFinansami.modele.Transakcja;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.modele.enumeracje.TypAlokacjiEnum;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;

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
            List<Transakcja> transakcjeWKategorii = magazynTransakcji.findByKonto_UzytkownikAndKategoriaAndTypAndDataBetweenOrderByDataDesc(
                    uzytkownik,
                    kategoria,
                    TypTransakcjiEnum.KOSZT,
                    dataOd,
                    dataDo
            );

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

        List<Transakcja> dochody = magazynTransakcji.findByKonto_UzytkownikAndTypAndDataBetweenOrderByDataDesc(
                uzytkownik,
                TypTransakcjiEnum.PRZYCHÓD,
                dataOd,
                dataDo
        );
        BigDecimal sumaDochodow = dochody.stream().map(Transakcja::getKwota).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sumaDochodow.divide(BigDecimal.valueOf(liczbaMiesiecy), 2, RoundingMode.HALF_UP);
    }

    // Metoda do generowania propozycji budżetu (DTO)
    public BudzetRequestDTO generujPropozycjeBudzetu(Uzytkownik uzytkownik, LocalDate dataAnalizyOd, LocalDate dataAnalizyDo, BigDecimal aktualnyPrzewidywanyDochod, TypAlokacjiEnum typAlokacji) {
        Map<Kategoria, BigDecimal> srednieWydatkiNaKategorie = obliczSrednieMiesieczneWydatkiNaKategorie(uzytkownik, dataAnalizyOd, dataAnalizyDo);
        BigDecimal srednieDochodyHistoryczne = obliczSrednieMiesieczneDochody(uzytkownik, dataAnalizyOd, dataAnalizyDo);

        BudzetRequestDTO propozycja = new BudzetRequestDTO();
        // Ustaw przewidywany dochód (może być podany przez użytkownika lub historyczny)
        BigDecimal dochodDoBudzetowania = (aktualnyPrzewidywanyDochod != null && aktualnyPrzewidywanyDochod.compareTo(BigDecimal.ZERO) > 0)
                ? aktualnyPrzewidywanyDochod : srednieDochodyHistoryczne;
        propozycja.setPrzewidywanyDochod(dochodDoBudzetowania);


        List<PozycjaBudzetuRequestDTO> pozycjePropozycji = new ArrayList<>();
        for (Map.Entry<Kategoria, BigDecimal> entry : srednieWydatkiNaKategorie.entrySet()) {
            PozycjaBudzetuRequestDTO pozycja = getPozycjaBudzetuRequestDTO(aktualnyPrzewidywanyDochod, typAlokacji,  entry, srednieDochodyHistoryczne);
            pozycjePropozycji.add(pozycja);
        }
        propozycja.setPozycjeBudzetu(pozycjePropozycji);
        return propozycja;
    }

    private static @NotNull PozycjaBudzetuRequestDTO getPozycjaBudzetuRequestDTO(BigDecimal aktualnyPrzewidywanyDochod, TypAlokacjiEnum typAlokacji, Map.Entry<Kategoria, BigDecimal> entry, BigDecimal srednieDochodyHistoryczne) {
        Kategoria kategoria = entry.getKey();
        BigDecimal sredniWydatek = entry.getValue();

        PozycjaBudzetuRequestDTO pozycja = new PozycjaBudzetuRequestDTO();
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
