// src/main/java/zarzadzanieFinansami/serwisy/BudzetUsługa.java
package zarzadzanieFinansami.serwisy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zarzadzanieFinansami.DTO.budzet.BudzetRequestDTO;
import zarzadzanieFinansami.DTO.budzet.BudzetResponseDTO;
import zarzadzanieFinansami.DTO.budzet.PozycjaBudzetuRequestDTO;
import zarzadzanieFinansami.DTO.budzet.PozycjaBudzetuResponseDTO;
import zarzadzanieFinansami.magazyn.*;
import zarzadzanieFinansami.modele.*;
import zarzadzanieFinansami.modele.enumeracje.TypAlokacjiEnum;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;
import zarzadzanieFinansami.wyjątki.DuplikatException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BudzetUsługa {

    private final MagazynBudzet magazynBudzetu;
    private final MagazynUzytkownika magazynUzytkownika;
    private final MagazynKategorii magazynKategorii;
    private final MagazynSzablonBudzetu magazynSzablonuBudzetu;
    private final MagazynTransakcji magazynTransakcji; // Do obliczania rzeczywistych wydatków

    @Autowired
    public BudzetUsługa(MagazynBudzet magazynBudzetu, MagazynUzytkownika magazynUzytkownika,
                        MagazynKategorii magazynKategorii, MagazynSzablonBudzetu magazynSzablonuBudzetu,
                        MagazynTransakcji magazynTransakcji) {
        this.magazynBudzetu = magazynBudzetu;
        this.magazynUzytkownika = magazynUzytkownika;
        this.magazynKategorii = magazynKategorii;
        this.magazynSzablonuBudzetu = magazynSzablonuBudzetu;
        this.magazynTransakcji = magazynTransakcji;
    }

    private Uzytkownik pobierzBiezacegoUzytkownika(String username) {
        return magazynUzytkownika.findByNazwa(username);
    }

    @Transactional
    public BudzetResponseDTO stworzBudzet(BudzetRequestDTO dto, String username) {
        Uzytkownik uzytkownik = pobierzBiezacegoUzytkownika(username);

        // Walidacja daty końcowej jeśli okresowość nie jest ustawiona
        if (dto.getOkresowosc() == null && dto.getDataKoncowa() == null) {
            throw new IllegalArgumentException("Data końcowa musi być ustawiona, jeśli okresowość nie jest zdefiniowana.");
        }
        if (dto.getDataKoncowa() != null && dto.getDataPoczatkowa().isAfter(dto.getDataKoncowa())) {
            throw new IllegalArgumentException("Data początkowa nie może być późniejsza niż data końcowa.");
        }

        // Sprawdzenie unikalności nazwy budżetu w danym okresie (np. aby nie mieć dwóch "Budżet na Styczeń")
        // To jest uproszczone sprawdzenie, można je doprecyzować
        LocalDate endCheckDate = dto.getDataKoncowa() != null ? dto.getDataKoncowa() : dto.getDataPoczatkowa().plusYears(100);
        if (magazynBudzetu.existsByNazwaAndUzytkownikAndDataPoczatkowaBetween(dto.getNazwa(), uzytkownik, dto.getDataPoczatkowa().minusDays(1), endCheckDate.plusDays(1))) {
            // throw new DuplikatException("Budżet o nazwie '" + dto.getNazwa() + "' już istnieje w podobnym okresie.");
        }


        Budzet budzet = new Budzet();
        budzet.setNazwa(dto.getNazwa());
        budzet.setUzytkownik(uzytkownik);
        budzet.setDataPoczatkowa(dto.getDataPoczatkowa());
        budzet.setDataKoncowa(dto.getDataKoncowa()); // Może być null
        budzet.setOkresowosc(dto.getOkresowosc());
        budzet.setPrzewidywanyDochod(dto.getPrzewidywanyDochod());
        budzet.setAktywny(true); // Domyślnie aktywny

        if (dto.getSzablonId() != null) {
            SzablonBudzetu szablon = magazynSzablonuBudzetu.findById(dto.getSzablonId())
                    .orElseThrow(() -> new DaneNieZnalesionoExeption("Szablon budżetu o ID: " + dto.getSzablonId() + " nie znaleziony."));
            // Sprawdzenie czy szablon jest systemowy lub należy do użytkownika
            if (szablon.getUzytkownik() != null && !szablon.getUzytkownik().getId().equals(uzytkownik.getId())) {
                throw new DaneNieZnalesionoExeption("Nie masz uprawnień do użycia tego szablonu.");
            }
            budzet.setOpartyNaSzablonie(szablon);
            // Można tu zaaplikować pozycje z szablonu, jeśli nie są podane w DTO
            // Lub jeśli są podane, to je użyć. Na razie zakładamy, że DTO ma priorytet.
            if (szablon.getProcentNaPotrzeby() != null) budzet.setProcentNaPotrzeby(szablon.getProcentNaPotrzeby());
            if (szablon.getProcentNaZachcianki() != null) budzet.setProcentNaZachcianki(szablon.getProcentNaZachcianki());
            if (szablon.getProcentNaInwestycje() != null) budzet.setProcentNaInwestycje(szablon.getProcentNaInwestycje());
        }

        // Ustawienia reguły procentowej z DTO (mają priorytet nad szablonem)
        if(dto.isZastosujReguleProcentowa()){
            if(dto.getProcentNaPotrzeby() != null) budzet.setProcentNaPotrzeby(dto.getProcentNaPotrzeby());
            if(dto.getProcentNaZachcianki() != null) budzet.setProcentNaZachcianki(dto.getProcentNaZachcianki());
            if(dto.getProcentNaInwestycje() != null) budzet.setProcentNaInwestycje(dto.getProcentNaInwestycje());
        }


        BigDecimal sumaAlokowana = BigDecimal.ZERO;
        if (dto.getPozycjeBudzetu() != null && !dto.getPozycjeBudzetu().isEmpty()) {
            for (PozycjaBudzetuRequestDTO pozDto : dto.getPozycjeBudzetu()) {
                Kategoria kategoria = magazynKategorii.findByUzytkownikAndId(uzytkownik, pozDto.getKategoriaId())
                        .orElseThrow(() -> new DaneNieZnalesionoExeption("Kategoria o ID: " + pozDto.getKategoriaId() + " nie znaleziona dla użytkownika."));

                PozycjaBudzetu pozycja = new PozycjaBudzetu();
                pozycja.setKategoria(kategoria);
                pozycja.setTypAlokacji(pozDto.getTypAlokacji());

                if (pozDto.getTypAlokacji() == TypAlokacjiEnum.PROCENTOWA) {
                    if (pozDto.getProcentAlokowany() == null || dto.getPrzewidywanyDochod() == null) {
                        throw new IllegalArgumentException("Procent alokowany i przewidywany dochód są wymagane dla alokacji procentowej.");
                    }
                    pozycja.setProcentAlokowany(pozDto.getProcentAlokowany());
                    BigDecimal kwota = dto.getPrzewidywanyDochod()
                            .multiply(pozDto.getProcentAlokowany())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    pozycja.setKwotaAlokowana(kwota);
                } else { // KWOTOWA
                    if (pozDto.getKwotaAlokowana() == null) {
                        throw new IllegalArgumentException("Kwota alokowana jest wymagana dla alokacji kwotowej.");
                    }
                    pozycja.setKwotaAlokowana(pozDto.getKwotaAlokowana());
                }
                sumaAlokowana = sumaAlokowana.add(pozycja.getKwotaAlokowana());
                budzet.dodajPozycjeBudzetu(pozycja);
            }
        }
        budzet.setSumaAlokowana(sumaAlokowana);

        Budzet zapisanyBudzet = magazynBudzetu.save(budzet);
        return mapToBudzetResponseDTO(zapisanyBudzet, uzytkownik);
    }

    @Transactional(readOnly = true)
    public List<BudzetResponseDTO> pobierzBudzetyUzytkownika(String username) {
        Uzytkownik uzytkownik = pobierzBiezacegoUzytkownika(username);
        return magazynBudzetu.findByUzytkownikOrderByDataPoczatkowaDesc(uzytkownik)
                .stream()
                .map(b -> mapToBudzetResponseDTO(b, uzytkownik))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BudzetResponseDTO pobierzBudzetPoId(Long budzetId, String username) {
        Uzytkownik uzytkownik = pobierzBiezacegoUzytkownika(username);
        Budzet budzet = magazynBudzetu.findByIdAndUzytkownik(budzetId, uzytkownik)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Budżet o ID: " + budzetId + " nie znaleziony lub nie należy do użytkownika."));
        return mapToBudzetResponseDTO(budzet, uzytkownik);
    }

    @Transactional
    public BudzetResponseDTO aktualizujBudzet(Long budzetId, BudzetRequestDTO dto, String username) {
        Uzytkownik uzytkownik = pobierzBiezacegoUzytkownika(username);
        Budzet budzet = magazynBudzetu.findByIdAndUzytkownik(budzetId, uzytkownik)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Budżet o ID: " + budzetId + " nie znaleziony lub nie należy do użytkownika."));

        // Walidacja daty końcowej jeśli okresowość nie jest ustawiona
        if (dto.getOkresowosc() == null && dto.getDataKoncowa() == null) {
            throw new IllegalArgumentException("Data końcowa musi być ustawiona, jeśli okresowość nie jest zdefiniowana.");
        }
        if (dto.getDataKoncowa() != null && dto.getDataPoczatkowa().isAfter(dto.getDataKoncowa())) {
            throw new IllegalArgumentException("Data początkowa nie może być późniejsza niż data końcowa.");
        }

        budzet.setNazwa(dto.getNazwa());
        budzet.setDataPoczatkowa(dto.getDataPoczatkowa());
        budzet.setDataKoncowa(dto.getDataKoncowa());
        budzet.setOkresowosc(dto.getOkresowosc());
        budzet.setPrzewidywanyDochod(dto.getPrzewidywanyDochod());
        // aktywny, szablonId - zakładamy, że nie są aktualizowane tą metodą, lub wymagają osobnych endpointów/logiki

        if(dto.isZastosujReguleProcentowa()){
            if(dto.getProcentNaPotrzeby() != null) budzet.setProcentNaPotrzeby(dto.getProcentNaPotrzeby());
            if(dto.getProcentNaZachcianki() != null) budzet.setProcentNaZachcianki(dto.getProcentNaZachcianki());
            if(dto.getProcentNaInwestycje() != null) budzet.setProcentNaInwestycje(dto.getProcentNaInwestycje());
        } else { // Jeśli odznaczono, można wyzerować
            budzet.setProcentNaPotrzeby(null);
            budzet.setProcentNaZachcianki(null);
            budzet.setProcentNaInwestycje(null);
        }


        // Aktualizacja pozycji - najprościej usunąć stare i dodać nowe
        // Bardziej zaawansowane byłoby porównywanie i aktualizowanie istniejących
        budzet.getPozycjeBudzetu().clear(); // Hibernate zajmie się usunięciem starych (orphanRemoval=true)
        BigDecimal nowaSumaAlokowana = BigDecimal.ZERO;

        if (dto.getPozycjeBudzetu() != null && !dto.getPozycjeBudzetu().isEmpty()) {
            for (PozycjaBudzetuRequestDTO pozDto : dto.getPozycjeBudzetu()) {
                Kategoria kategoria = magazynKategorii.findByUzytkownikAndId(uzytkownik, pozDto.getKategoriaId())
                        .orElseThrow(() -> new DaneNieZnalesionoExeption("Kategoria o ID: " + pozDto.getKategoriaId() + " nie znaleziona dla użytkownika."));

                PozycjaBudzetu pozycja = new PozycjaBudzetu();
                pozycja.setKategoria(kategoria);
                pozycja.setTypAlokacji(pozDto.getTypAlokacji());

                if (pozDto.getTypAlokacji() == TypAlokacjiEnum.PROCENTOWA) {
                    if (pozDto.getProcentAlokowany() == null || dto.getPrzewidywanyDochod() == null) {
                        throw new IllegalArgumentException("Procent alokowany i przewidywany dochód są wymagane dla alokacji procentowej.");
                    }
                    pozycja.setProcentAlokowany(pozDto.getProcentAlokowany());
                    BigDecimal kwota = dto.getPrzewidywanyDochod()
                            .multiply(pozDto.getProcentAlokowany())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    pozycja.setKwotaAlokowana(kwota);
                } else { // KWOTOWA
                    if (pozDto.getKwotaAlokowana() == null) {
                        throw new IllegalArgumentException("Kwota alokowana jest wymagana dla alokacji kwotowej.");
                    }
                    pozycja.setKwotaAlokowana(pozDto.getKwotaAlokowana());
                }
                nowaSumaAlokowana = nowaSumaAlokowana.add(pozycja.getKwotaAlokowana());
                budzet.dodajPozycjeBudzetu(pozycja);
            }
        }
        budzet.setSumaAlokowana(nowaSumaAlokowana);

        Budzet zaktualizowanyBudzet = magazynBudzetu.save(budzet);
        return mapToBudzetResponseDTO(zaktualizowanyBudzet, uzytkownik);
    }


    @Transactional
    public void usunBudzet(Long budzetId, String username) {
        Uzytkownik uzytkownik = pobierzBiezacegoUzytkownika(username);
        Budzet budzet = magazynBudzetu.findByIdAndUzytkownik(budzetId, uzytkownik)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Budżet o ID: " + budzetId + " nie znaleziony lub nie należy do użytkownika."));
        magazynBudzetu.delete(budzet);
    }

    private BudzetResponseDTO mapToBudzetResponseDTO(Budzet budzet, Uzytkownik uzytkownik) {
        List<PozycjaBudzetuResponseDTO> pozycjeDto = new ArrayList<>();
        BigDecimal sumaRzeczywistychWydatkowCalegoBudzetu = BigDecimal.ZERO;

        LocalDate dataPoczatkuOkresu = budzet.getDataPoczatkowa();
        LocalDate dataKoncaOkresu = budzet.getDataKoncowa() != null ? budzet.getDataKoncowa() : LocalDate.now(); // Jeśli data końca null, bierzemy do teraz

        if(budzet.getOkresowosc() != null && budzet.getDataKoncowa() == null) {
            // Dla budżetów cyklicznych bez daty końcowej, możemy chcieć analizować bieżący cykl
            // Np. jeśli MIESIECZNY i dataPoczatkowa to 01.05, a dziś 15.06, to analizujemy 01.06-30.06
            // To wymaga bardziej zaawansowanej logiki określania bieżącego okresu.
            // Dla uproszczenia, jeśli dataKoncowa jest null, bierzemy od datyPoczatkowej do "teraz"
            // LUB można rzucić błąd/wymusić datę końcową jeśli okresowość nie jest używana do dynamicznego wyznaczania końca
            dataKoncaOkresu = LocalDate.now(); // Uproszczenie, do dopracowania dla budżetów cyklicznych
        }


        for (PozycjaBudzetu pozycja : budzet.getPozycjeBudzetu()) {
            List<Transakcja> transakcjeDlaPozycji = magazynTransakcji.findByKonto_UzytkownikAndKategoriaAndTypAndDataBetweenOrderByDataDesc(
                    uzytkownik,
                    pozycja.getKategoria(),
                    TypTransakcjiEnum.KOSZT,
                    dataPoczatkuOkresu,
                    dataKoncaOkresu
            );
            BigDecimal sumaWydatkowDlaPozycji = transakcjeDlaPozycji.stream()
                    .map(Transakcja::getKwota)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            sumaRzeczywistychWydatkowCalegoBudzetu = sumaRzeczywistychWydatkowCalegoBudzetu.add(sumaWydatkowDlaPozycji);

            pozycjeDto.add(new PozycjaBudzetuResponseDTO(
                    pozycja.getId(),
                    pozycja.getKategoria().getId(),
                    pozycja.getKategoria().getNazwa(),
                    pozycja.getTypAlokacji(),
                    pozycja.getProcentAlokowany(),
                    pozycja.getKwotaAlokowana(),
                    sumaWydatkowDlaPozycji
            ));
        }

        return new BudzetResponseDTO(
                budzet.getId(),
                budzet.getNazwa(),
                uzytkownik.getId(),
                budzet.getDataPoczatkowa(),
                budzet.getDataKoncowa(),
                budzet.getOkresowosc(),
                budzet.getPrzewidywanyDochod(),
                budzet.getSumaAlokowana(),
                sumaRzeczywistychWydatkowCalegoBudzetu,
                budzet.isAktywny(),
                budzet.getOpartyNaSzablonie() != null ? budzet.getOpartyNaSzablonie().getId() : null,
                budzet.getProcentNaPotrzeby(),
                budzet.getProcentNaZachcianki(),
                budzet.getProcentNaInwestycje(),
                pozycjeDto
        );
    }
    // Metoda do pobierania aktywnego budżetu użytkownika na dany dzień (do wykorzystania przy dodawaniu transakcji)
    public Optional<Budzet> getAktywnyBudzetNaDzien(Uzytkownik uzytkownik, LocalDate data) {
        return magazynBudzetu.findByUzytkownikAndAktywnyTrueAndDataPoczatkowaLessThanEqualAndDataKoncowaGreaterThanEqual(
                uzytkownik, data, data);
    }
}