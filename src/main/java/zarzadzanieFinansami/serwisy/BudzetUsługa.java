package zarzadzanieFinansami.serwisy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zarzadzanieFinansami.DTO.budzet.BudzetWysylanieDTO;
import zarzadzanieFinansami.DTO.budzet.BudzetOdpowiedzDTO;
import zarzadzanieFinansami.DTO.budzet.PozycjaBudzetuWysylanieDTO;
import zarzadzanieFinansami.DTO.budzet.PozycjaBudzetuOdpowiedzDTO;
import zarzadzanieFinansami.DTO.budzet.RegulaKwotowaDTO;
import zarzadzanieFinansami.DTO.budzet.RegulaProcentowaDTO;
import zarzadzanieFinansami.DTO.budzet.WydatkiKategoriiDTO;
import zarzadzanieFinansami.magazyn.*;
import zarzadzanieFinansami.modele.*;
import zarzadzanieFinansami.modele.enumeracje.KategorieBudzetEnum;
import zarzadzanieFinansami.modele.enumeracje.TypAlokacjiEnum;
import zarzadzanieFinansami.modele.enumeracje.TypRegulyBudzetowejEnum;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;

import zarzadzanieFinansami.wyjątki.DuplikatException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
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
            throw new DuplikatException("Budżet o nazwie '" + dto.getNazwa() + "' już istnieje w podobnym okresie.");
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
        // Ta metoda teraz również automatycznie generuje pozycje budżetu, jeśli to konieczne
        zastosujRegulyBudzetowe(budzet, dto, uzytkownik);

        // Krok 3: Jeśli użytkownik przesłał ręczną listę pozycji, ma ona priorytet.
        // W przeciwnym razie, użyte zostaną pozycje wygenerowane przez regułę.
        if (dto.getPozycjeBudzetu() != null && !dto.getPozycjeBudzetu().isEmpty()) {
            zastosujPozycjeBudzetu(budzet, dto, uzytkownik);
        }

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

        zastosujRegulyBudzetowe(budzet, dto, uzytkownik);

        // Jeśli użytkownik przesłał ręczną listę pozycji, synchronizuj ją.
        // W przeciwnym razie, zachowaj pozycje wygenerowane przez regułę.
        // Przekazanie `null` w DTO oznacza "nie ruszaj istniejących pozycji".
        if (dto.getPozycjeBudzetu() != null) {
            synchronizujPozycjeBudzetu(budzet, dto, uzytkownik);
        }

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
    private void zastosujRegulyBudzetowe(Budzet budzet, BudzetWysylanieDTO dto, Uzytkownik uzytkownik) {
        budzet.setTypReguly(dto.getTypReguly());

        if (dto.getTypReguly() == TypRegulyBudzetowejEnum.PROCENTOWA && dto.getRegulaProcentowa() != null) {
            RegulaProcentowaDTO regula = dto.getRegulaProcentowa();
            if (regula.isZastosuj()) {
                // Zapisz procenty w budżecie
                budzet.setProcentNaPotrzeby(regula.getProcentNaPotrzeby());
                budzet.setProcentNaZachcianki(regula.getProcentNaZachcianki());
                budzet.setProcentNaInwestycje(regula.getProcentNaInwestycje());

                // Automatycznie wygeneruj pozycje budżetu na podstawie tych reguł,
                // tylko jeśli użytkownik nie dostarczył własnej listy pozycji.
                if (dto.getPozycjeBudzetu() == null || dto.getPozycjeBudzetu().isEmpty()) {
                    generujPozycjeZRegulyProcentowej(budzet, uzytkownik);
                }
            } else {
                // Jeśli 'zastosuj' jest false, czyścimy reguły
                wyczyscProcentyRegul(budzet);
                budzet.getPozycjeBudzetu().clear(); // Czyścimy też pozycje, bo reguła została wyłączona
            }
        } else if (dto.getTypReguly() == TypRegulyBudzetowejEnum.KWOTOWA && dto.getRegulaKwotowa() != null) {
            // Dla reguły kwotowej, przeliczamy kwoty na procenty i zapisujemy w encji
            przeliczReguleKwotowaNaProcenty(budzet, dto.getRegulaKwotowa());
            // Tutaj również można dodać logikę generowania pozycji, jeśli jest taka potrzeba
        } else {
            // Jeśli typReguly == BRAK lub odpowiedni obiekt DTO jest null, czyścimy reguły
            wyczyscProcentyRegul(budzet);
            // Jeśli nie ma reguły, a użytkownik nie podał pozycji, lista powinna być pusta
            if (dto.getPozycjeBudzetu() == null || dto.getPozycjeBudzetu().isEmpty()) {
                budzet.getPozycjeBudzetu().clear();
            }
        }
    }

    private void generujPozycjeZRegulyProcentowej(Budzet budzet, Uzytkownik uzytkownik) {
        // Zamiast czyścić wszystko, będziemy dodawać tylko brakujące pozycje.
        // Pozwoli to użytkownikowi na ręczne dostosowanie kwot po automatycznym wygenerowaniu.
        // Przy aktualizacji budżetu, jeśli użytkownik prześle własną listę, metoda `synchronizujPozycjeBudzetu` zajmie się resztą.
        if (!budzet.getPozycjeBudzetu().isEmpty()) {
            return; // Jeśli pozycje już istnieją (np. z szablonu lub poprzedniej edycji), nie generuj ich ponownie.
        }

        // Generuj dla Potrzeb
        dodajPozycjeDlaKategoriiBudzetu(budzet, uzytkownik, KategorieBudzetEnum.POTRZEBY, budzet.getProcentNaPotrzeby());
        // Generuj dla Zachcianek
        dodajPozycjeDlaKategoriiBudzetu(budzet, uzytkownik, KategorieBudzetEnum.ZACHCIANKI, budzet.getProcentNaZachcianki());
        // Generuj dla Inwestycji
        dodajPozycjeDlaKategoriiBudzetu(budzet, uzytkownik, KategorieBudzetEnum.INWESTYCJE, budzet.getProcentNaInwestycje());
    }

    private void dodajPozycjeDlaKategoriiBudzetu(Budzet budzet, Uzytkownik uzytkownik, KategorieBudzetEnum typKategorii, Integer procentCalkowity) {
        if (procentCalkowity == null || procentCalkowity == 0 || budzet.getPrzewidywanyDochod() == null) return;

        List<Kategoria> kategorie = magazynKategorii.findByUzytkownikAndKategorieBudzet(uzytkownik, typKategorii);
        if (kategorie.isEmpty()) return;

        // Oblicz całkowitą kwotę dla tej grupy (np. 50% z dochodu na Potrzeby)
        BigDecimal kwotaCalkowitaDlaGrupy = budzet.getPrzewidywanyDochod()
                .multiply(new BigDecimal(procentCalkowity))
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);

        // Podziel kwotę po równo na wszystkie kategorie w tej grupie
        BigDecimal kwotaNaKategorie = kwotaCalkowitaDlaGrupy.divide(new BigDecimal(kategorie.size()), 2, RoundingMode.DOWN);

        // Obsługa reszty z dzielenia, aby suma się zgadzała
        BigDecimal reszta = kwotaCalkowitaDlaGrupy.subtract(kwotaNaKategorie.multiply(new BigDecimal(kategorie.size())));

        for (int i = 0; i < kategorie.size(); i++) {
            Kategoria kategoria = kategorie.get(i);
            PozycjaBudzetu nowaPozycja = new PozycjaBudzetu();
            nowaPozycja.setKategoria(kategoria);
            nowaPozycja.setTypAlokacji(TypAlokacjiEnum.KWOTOWA); // Generujemy jako kwotowe dla prostoty

            BigDecimal kwotaDlaTejPozycji = kwotaNaKategorie;
            if (i == 0) { // Dodaj resztę do pierwszej pozycji
                kwotaDlaTejPozycji = kwotaDlaTejPozycji.add(reszta);
            }

            nowaPozycja.setKwotaAlokowana(kwotaDlaTejPozycji);
            budzet.dodajPozycjeBudzetu(nowaPozycja);
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
    /**
     * Wypełnia lub aktualizuje dane pojedynczej pozycji budżetu na podstawie DTO.
     * Oblicza kwotę alokowaną i zwraca ją.
     */
    private BigDecimal zaktualizujDanePozycjiZDto(PozycjaBudzetu pozycja, PozycjaBudzetuWysylanieDTO pozDto, BigDecimal przewidywanyDochod) {
        pozycja.setTypAlokacji(pozDto.getTypAlokacji());

        if (pozDto.getTypAlokacji() == TypAlokacjiEnum.PROCENTOWA) {
            if (pozDto.getProcentAlokowany() == null || przewidywanyDochod == null) {
                throw new IllegalArgumentException("Procent alokowany i przewidywany dochód są wymagane dla alokacji procentowej.");
            }
            pozycja.setProcentAlokowany(pozDto.getProcentAlokowany());
            BigDecimal kwota = przewidywanyDochod
                    .multiply(pozDto.getProcentAlokowany())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            pozycja.setKwotaAlokowana(kwota);
        } else { // KWOTOWA
            if (pozDto.getKwotaAlokowana() == null) {
                throw new IllegalArgumentException("Kwota alokowana jest wymagana dla alokacji kwotowej.");
            }
            pozycja.setKwotaAlokowana(pozDto.getKwotaAlokowana());
            pozycja.setProcentAlokowany(null);
        }
        return pozycja.getKwotaAlokowana();
    }

    /**
     * Wydajna metoda do synchronizacji kolekcji pozycji budżetu.
     * Aktualizuje istniejące, dodaje nowe i usuwa brakujące pozycje.
     */
    private void synchronizujPozycjeBudzetu(Budzet budzet, BudzetWysylanieDTO dto, Uzytkownik uzytkownik) {
        // Krok 1: Stwórz mapę istniejących pozycji budżetu (klucz: ID kategorii) dla szybkiego dostępu.
        Map<Integer, PozycjaBudzetu> istniejacePozycjeMap = budzet.getPozycjeBudzetu().stream()
                .collect(Collectors.toMap(p -> p.getKategoria().getId(), p -> p));

        Set<Integer> kategorieZDto = new HashSet<>();
        BigDecimal nowaSumaAlokowana = BigDecimal.ZERO;

        if (dto.getPozycjeBudzetu() != null) {
            // Krok 2: Przejdź przez pozycje z DTO, aby zaktualizować istniejące lub stworzyć nowe.
            for (PozycjaBudzetuWysylanieDTO pozDto : dto.getPozycjeBudzetu()) {
                kategorieZDto.add(pozDto.getKategoriaId());
                PozycjaBudzetu pozycja = istniejacePozycjeMap.get(pozDto.getKategoriaId());

                if (pozycja != null) {
                    BigDecimal kwotaPozycji = zaktualizujDanePozycjiZDto(pozycja, pozDto, dto.getPrzewidywanyDochod());
                    nowaSumaAlokowana = nowaSumaAlokowana.add(kwotaPozycji);
                } else {
                    Kategoria kategoria = magazynKategorii.findByUzytkownikAndId(uzytkownik, pozDto.getKategoriaId())
                            .orElseThrow(() -> new DaneNieZnalesionoExeption("Kategoria o ID: " + pozDto.getKategoriaId() + " nie znaleziona dla użytkownika."));

                    PozycjaBudzetu nowaPozycja = new PozycjaBudzetu();
                    nowaPozycja.setKategoria(kategoria);
                    BigDecimal kwotaPozycji = zaktualizujDanePozycjiZDto(nowaPozycja, pozDto, dto.getPrzewidywanyDochod());
                    nowaSumaAlokowana = nowaSumaAlokowana.add(kwotaPozycji);
                    budzet.dodajPozycjeBudzetu(nowaPozycja);
                }
            }
        }

        // Krok 3: Usuń pozycje, które istnieją w budżecie,
        budzet.getPozycjeBudzetu().removeIf(p -> !kategorieZDto.contains(p.getKategoria().getId()));

        // Krok końcowy: Ustaw nową, przeliczoną sumę alokowaną.
        budzet.setSumaAlokowana(nowaSumaAlokowana);
    }

    private void zastosujPozycjeBudzetu(Budzet budzet, BudzetWysylanieDTO dto, Uzytkownik uzytkownik) {
        BigDecimal sumaAlokowana = BigDecimal.ZERO;
        if (dto.getPozycjeBudzetu() != null && !dto.getPozycjeBudzetu().isEmpty()) {
            for (PozycjaBudzetuWysylanieDTO pozDto : dto.getPozycjeBudzetu()) {
                Kategoria kategoria = magazynKategorii.findByUzytkownikAndId(uzytkownik, pozDto.getKategoriaId())
                        .orElseThrow(() -> new DaneNieZnalesionoExeption("Kategoria o ID: " + pozDto.getKategoriaId() + " nie znaleziona dla użytkownika."));
                PozycjaBudzetu pozycja = new PozycjaBudzetu();
                pozycja.setKategoria(kategoria);
                BigDecimal kwotaPozycji = zaktualizujDanePozycjiZDto(pozycja, pozDto, dto.getPrzewidywanyDochod());
                sumaAlokowana = sumaAlokowana.add(kwotaPozycji);
                budzet.dodajPozycjeBudzetu(pozycja);
            }
        }
        budzet.setSumaAlokowana(sumaAlokowana);
    }


    private BudzetOdpowiedzDTO mapToBudzetResponseDTO(Budzet budzet, Uzytkownik uzytkownik) {
        LocalDate dataPoczatkuOkresu = budzet.getDataPoczatkowa();
        LocalDate dataKoncaOkresu = budzet.getDataKoncowa() != null ? budzet.getDataKoncowa() : LocalDate.now();

        // Krok 1: Zbierz ID wszystkich kategorii z budżetu
        Set<Integer> kategoriaIds = budzet.getPozycjeBudzetu().stream()
                .map(p -> p.getKategoria().getId())
                .collect(Collectors.toSet());

        // Krok 2: Wykonaj JEDNO zapytanie do bazy danych, aby pobrać sumę wydatków dla wszystkich kategorii
        Map<Integer, BigDecimal> wydatkiNaKategorie;
        if (!kategoriaIds.isEmpty()) {
            wydatkiNaKategorie = magazynTransakcji.obliczSumeWydatkowDlaKategorii(uzytkownik, dataPoczatkuOkresu, dataKoncaOkresu, kategoriaIds)
                    .stream()
                    .collect(Collectors.toMap(WydatkiKategoriiDTO::getKategoriaId, WydatkiKategoriiDTO::getSumaWydatkow));
        } else {
            wydatkiNaKategorie = Collections.emptyMap();
        }

        // Krok 3: Oblicz sumę wszystkich rzeczywistych wydatków
        BigDecimal sumaRzeczywistychWydatkowCalegoBudzetu = wydatkiNaKategorie.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Krok 4: Zmapuj pozycje budżetu, pobierając dane z przygotowanej mapy (bez zapytań do bazy)
        List<PozycjaBudzetuOdpowiedzDTO> pozycjeDto = budzet.getPozycjeBudzetu().stream()
                .map(pozycja -> {
            BigDecimal sumaWydatkowDlaPozycji = wydatkiNaKategorie.getOrDefault(pozycja.getKategoria().getId(), BigDecimal.ZERO);
            return new PozycjaBudzetuOdpowiedzDTO(
                    pozycja.getId(),
                    pozycja.getKategoria().getId(),
                    pozycja.getKategoria().getNazwa(),
                    pozycja.getTypAlokacji(),
                    pozycja.getProcentAlokowany(),
                    pozycja.getKwotaAlokowana(),
                    sumaWydatkowDlaPozycji);
                }).collect(Collectors.toList());

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