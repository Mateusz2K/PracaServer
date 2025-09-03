// src/main/java/zarzadzanieFinansami/serwisy/BudzetUsługa.java
package zarzadzanieFinansami.serwisy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zarzadzanieFinansami.DTO.budzet.BudzetWysylanieDTO;
import zarzadzanieFinansami.DTO.budzet.BudzetOdpowiedzDTO;
import zarzadzanieFinansami.DTO.budzet.PozycjaBudzetuWysylanieDTO;
import zarzadzanieFinansami.DTO.budzet.PozycjaBudzetuOdpowiedzDTO;
import zarzadzanieFinansami.DTO.budzet.RegulaKwotowaDTO;
import zarzadzanieFinansami.DTO.budzet.RegulaProcentowaDTO;
import zarzadzanieFinansami.magazyn.*;
import zarzadzanieFinansami.modele.*;
import zarzadzanieFinansami.modele.enumeracje.TypAlokacjiEnum;
import zarzadzanieFinansami.modele.enumeracje.TypRegulyBudzetowejEnum;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;

import jakarta.persistence.criteria.Predicate;
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
    public BudzetOdpowiedzDTO stworzBudzet(BudzetWysylanieDTO dto, String username) {
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

        // Krok 1: Zastosuj ustawienia z szablonu jako domyślne (jeśli istnieje)
        if (dto.getSzablonId() != null) {
            SzablonBudzetu szablon = magazynSzablonuBudzetu.findById(dto.getSzablonId())
                    .orElseThrow(() -> new DaneNieZnalesionoExeption("Szablon budżetu o ID: " + dto.getSzablonId() + " nie znaleziony."));
            if (szablon.getUzytkownik() != null && !szablon.getUzytkownik().getId().equals(uzytkownik.getId()) && !szablon.isCzyPubliczny()) {
                throw new DaneNieZnalesionoExeption("Nie masz uprawnień do użycia tego szablonu.");
            }
            budzet.setOpartyNaSzablonie(szablon);
            zastosujRegulyZSzablonu(budzet, szablon);
        }

        // Krok 2: Zastosuj reguły z DTO, które mają priorytet nad szablonem
        zastosujRegulyBudzetowe(budzet, dto);

        // Krok 3: Przetwórz i dodaj pozycje budżetu z DTO
        zastosujPozycjeBudzetu(budzet, dto, uzytkownik);

        Budzet zapisanyBudzet = magazynBudzetu.save(budzet);
        return mapToBudzetResponseDTO(zapisanyBudzet, uzytkownik);
    }

    @Transactional(readOnly = true)
    public List<BudzetOdpowiedzDTO> pobierzBudzetyUzytkownika(String username) {
        Uzytkownik uzytkownik = pobierzBiezacegoUzytkownika(username);
        return magazynBudzetu.findByUzytkownikOrderByDataPoczatkowaDesc(uzytkownik)
                .stream()
                .map(b -> mapToBudzetResponseDTO(b, uzytkownik))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BudzetOdpowiedzDTO pobierzBudzetPoId(Long budzetId, String username) {
        Uzytkownik uzytkownik = pobierzBiezacegoUzytkownika(username);
        Budzet budzet = magazynBudzetu.findByIdAndUzytkownik(budzetId, uzytkownik)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Budżet o ID: " + budzetId + " nie znaleziony lub nie należy do użytkownika."));
        return mapToBudzetResponseDTO(budzet, uzytkownik);
    }

    @Transactional
    public BudzetOdpowiedzDTO aktualizujBudzet(Long budzetId, BudzetWysylanieDTO dto, String username) {
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

        // Zastosuj reguły z DTO
        zastosujRegulyBudzetowe(budzet, dto);

        // Aktualizacja pozycji - usuwamy stare i dodajemy nowe na podstawie DTO
        budzet.getPozycjeBudzetu().clear(); // Hibernate zajmie się usunięciem starych (orphanRemoval=true)
        budzet.setSumaAlokowana(BigDecimal.ZERO);
        zastosujPozycjeBudzetu(budzet, dto, uzytkownik);

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

    // --- METODY POMOCNICZE ---

    private void zastosujRegulyZSzablonu(Budzet budzet, SzablonBudzetu szablon) {
        budzet.setTypReguly(szablon.getTypReguly());
        // Szablon przechowuje tylko procenty, nawet jeśli jego typ to KWOTOWY (bo kwoty nie mają sensu bez dochodu)
        // Dlatego po prostu kopiujemy procenty.
        budzet.setProcentNaPotrzeby(szablon.getProcentNaPotrzeby());
        budzet.setProcentNaZachcianki(szablon.getProcentNaZachcianki());
        budzet.setProcentNaInwestycje(szablon.getProcentNaInwestycje());
    }

    /**
     * Stosuje reguły budżetowe (procentowe lub kwotowe) z DTO do encji Budzet.
     * Dane z DTO mają zawsze priorytet nad danymi z szablonu.
     */
    private void zastosujRegulyBudzetowe(Budzet budzet, BudzetWysylanieDTO dto) {
        budzet.setTypReguly(dto.getTypReguly());

        if (dto.getTypReguly() == TypRegulyBudzetowejEnum.PROCENTOWA && dto.getRegulaProcentowa() != null) {
            RegulaProcentowaDTO regula = dto.getRegulaProcentowa();
            if (regula.isZastosuj()) {
                budzet.setProcentNaPotrzeby(regula.getProcentNaPotrzeby());
                budzet.setProcentNaZachcianki(regula.getProcentNaZachcianki());
                budzet.setProcentNaInwestycje(regula.getProcentNaInwestycje());
            } else {
                // Jeśli 'zastosuj' jest false, czyścimy reguły
                wyczyscProcentyRegul(budzet);
            }
        } else if (dto.getTypReguly() == TypRegulyBudzetowejEnum.KWOTOWA && dto.getRegulaKwotowa() != null) {
            // Dla reguły kwotowej, przeliczamy kwoty na procenty i zapisujemy w encji
            przeliczReguleKwotowaNaProcenty(budzet, dto.getRegulaKwotowa());
        } else {
            // Jeśli typReguly == BRAK lub odpowiedni obiekt DTO jest null, czyścimy reguły
            wyczyscProcentyRegul(budzet);
        }
    }

    private void wyczyscProcentyRegul(Budzet budzet) {
        budzet.setProcentNaPotrzeby(null);
        budzet.setProcentNaZachcianki(null);
        budzet.setProcentNaInwestycje(null);
    }

    private void przeliczReguleKwotowaNaProcenty(Budzet budzet, RegulaKwotowaDTO regula) {
        BigDecimal dochod = budzet.getPrzewidywanyDochod();
        if (dochod == null || dochod.compareTo(BigDecimal.ZERO) == 0) {
            // Nie można policzyć procentów bez dochodu, więc czyścimy
            wyczyscProcentyRegul(budzet);
            return;
        }

        BigDecimal kwotaPotrzeby = regula.getKwotaNaPotrzeby() != null ? regula.getKwotaNaPotrzeby() : BigDecimal.ZERO;
        BigDecimal kwotaZachcianki = regula.getKwotaNaZachcianki() != null ? regula.getKwotaNaZachcianki() : BigDecimal.ZERO;
        BigDecimal kwotaInwestycje = regula.getKwotaNaInwestycje() != null ? regula.getKwotaNaInwestycje() : BigDecimal.ZERO;

        budzet.setProcentNaPotrzeby(kwotaPotrzeby.multiply(new BigDecimal("100")).divide(dochod, 0, RoundingMode.HALF_UP).intValue());
        budzet.setProcentNaZachcianki(kwotaZachcianki.multiply(new BigDecimal("100")).divide(dochod, 0, RoundingMode.HALF_UP).intValue());
        budzet.setProcentNaInwestycje(kwotaInwestycje.multiply(new BigDecimal("100")).divide(dochod, 0, RoundingMode.HALF_UP).intValue());
    }

    private void zastosujPozycjeBudzetu(Budzet budzet, BudzetWysylanieDTO dto, Uzytkownik uzytkownik) {
        BigDecimal sumaAlokowana = BigDecimal.ZERO;
        if (dto.getPozycjeBudzetu() != null && !dto.getPozycjeBudzetu().isEmpty()) {
            for (PozycjaBudzetuWysylanieDTO pozDto : dto.getPozycjeBudzetu()) {
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
    }


    private BudzetOdpowiedzDTO mapToBudzetResponseDTO(Budzet budzet, Uzytkownik uzytkownik) {
        List<PozycjaBudzetuOdpowiedzDTO> pozycjeDto = new ArrayList<>();
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
            // Używamy Specification, aby dynamicznie budować zapytanie, tak jak w TransakcjaUsługa
            LocalDate finalDataKoncaOkresu = dataKoncaOkresu;
            Specification<Transakcja> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("konto").get("uzytkownik"), uzytkownik));
                predicates.add(cb.equal(root.get("kategoria"), pozycja.getKategoria()));
                predicates.add(cb.equal(root.get("typ"), TypTransakcjiEnum.KOSZT));
                predicates.add(cb.between(root.get("data"), dataPoczatkuOkresu, finalDataKoncaOkresu));

                query.orderBy(cb.desc(root.get("data"))); // Zachowujemy sortowanie

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            List<Transakcja> transakcjeDlaPozycji = magazynTransakcji.findAll(spec);
            BigDecimal sumaWydatkowDlaPozycji = transakcjeDlaPozycji.stream()
                    .map(Transakcja::getKwota)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            sumaRzeczywistychWydatkowCalegoBudzetu = sumaRzeczywistychWydatkowCalegoBudzetu.add(sumaWydatkowDlaPozycji);

            pozycjeDto.add(new PozycjaBudzetuOdpowiedzDTO(
                    pozycja.getId(),
                    pozycja.getKategoria().getId(),
                    pozycja.getKategoria().getNazwa(),
                    pozycja.getTypAlokacji(),
                    pozycja.getProcentAlokowany(),
                    pozycja.getKwotaAlokowana(),
                    sumaWydatkowDlaPozycji
            ));
        }

        return new BudzetOdpowiedzDTO(
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
                budzet.getTypReguly(),
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