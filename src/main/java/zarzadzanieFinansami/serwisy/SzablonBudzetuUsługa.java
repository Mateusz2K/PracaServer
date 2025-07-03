// src/main/java/zarzadzanieFinansami/serwisy/SzablonBudzetuUsługa.java
package zarzadzanieFinansami.serwisy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zarzadzanieFinansami.DTO.budzet.szablon.PozycjaSzablonuBudzetuWysylanieDTO;
import zarzadzanieFinansami.DTO.budzet.szablon.PozycjaSzablonuBudzetuOdpowiedzDTO;
import zarzadzanieFinansami.DTO.budzet.szablon.SzablonBudzetuWysylanieDTO;
import zarzadzanieFinansami.DTO.budzet.szablon.SzablonBudzetuOdpowiedzDTO;
import zarzadzanieFinansami.magazyn.MagazynKategorii;
import zarzadzanieFinansami.magazyn.MagazynSzablonBudzetu;
import zarzadzanieFinansami.magazyn.MagazynUzytkownika;
import zarzadzanieFinansami.modele.Kategoria;
import zarzadzanieFinansami.modele.PozycjaSzablonuBudzetu;
import zarzadzanieFinansami.modele.SzablonBudzetu;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.modele.enumeracje.RolaEnum;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;
import zarzadzanieFinansami.wyjątki.DuplikatException;
import zarzadzanieFinansami.wyjątki.ForbiddenAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class SzablonBudzetuUsługa {

    private final MagazynSzablonBudzetu magazynSzablonuBudzetu;
    private final MagazynUzytkownika magazynUzytkownika;
    private final MagazynKategorii magazynKategorii; // Potrzebne, jeśli pozycje szablonu linkują do Kategoria

    @Autowired
    public SzablonBudzetuUsługa(MagazynSzablonBudzetu magazynSzablonuBudzetu,
                                MagazynUzytkownika magazynUzytkownika,
                                MagazynKategorii magazynKategorii) {
        this.magazynSzablonuBudzetu = magazynSzablonuBudzetu;
        this.magazynUzytkownika = magazynUzytkownika;
        this.magazynKategorii = magazynKategorii;
    }

    private Uzytkownik pobierzBiezacegoUzytkownika(String username) {
        return magazynUzytkownika.findByNazwa(username);
    }

    @Transactional
    public SzablonBudzetuOdpowiedzDTO stworzSzablonUzytkownika(SzablonBudzetuWysylanieDTO dto, String username) {
        Uzytkownik uzytkownik = pobierzBiezacegoUzytkownika(username);
        if (magazynSzablonuBudzetu.existsByNazwaAndUzytkownik(dto.getNazwa(), uzytkownik)) {
            throw new DuplikatException("Szablon o nazwie '" + dto.getNazwa() + "' już istnieje dla tego użytkownika.");
        }

        SzablonBudzetu szablon = new SzablonBudzetu();
        szablon.setNazwa(dto.getNazwa());
        szablon.setOpis(dto.getOpis());
        szablon.setUzytkownik(uzytkownik); // Szablon użytkownika
        szablon.setCzyPubliczny(false); // Szablony użytkownika są prywatne

        szablon.setProcentNaPotrzeby(dto.getProcentNaPotrzeby());
        szablon.setProcentNaZachcianki(dto.getProcentNaZachcianki());
        szablon.setProcentNaInwestycje(dto.getProcentNaInwestycje());

        if (dto.getPozycjeSzablonu() != null) {
            for (PozycjaSzablonuBudzetuWysylanieDTO pozDto : dto.getPozycjeSzablonu()) {
                PozycjaSzablonuBudzetu pozycja = new PozycjaSzablonuBudzetu();
                // Jeśli linkujemy do Kategoria, musimy ją znaleźć
                if (pozDto.getKategoriaId() != null) {
                    Kategoria kategoria = magazynKategorii.findByUzytkownikAndId(uzytkownik, pozDto.getKategoriaId())
                            .orElseThrow(() -> new DaneNieZnalesionoExeption("Kategoria o ID: " + pozDto.getKategoriaId() + " nie znaleziona."));
                    pozycja.setKategoria(kategoria);
                }
                pozycja.setMetaKategoriaNazwa(pozDto.getMetaKategoriaNazwa());
                pozycja.setTypAlokacjiEnum(pozDto.getTypAlokacji());
                pozycja.setProcentAlokowany(pozDto.getProcentAlokowany());
                pozycja.setKwotaAlokowana(pozDto.getKwotaAlokowana());
                szablon.dodajPozycjeSzablonu(pozycja);
            }
        }
        SzablonBudzetu zapisanySzablon = magazynSzablonuBudzetu.save(szablon);
        return mapToSzablonResponseDTO(zapisanySzablon);
    }

    // Metoda do tworzenia szablonów systemowych (np. przez admina)
    @Transactional
    public SzablonBudzetuOdpowiedzDTO stworzSzablonSystemowy(SzablonBudzetuWysylanieDTO dto) {
        if (magazynSzablonuBudzetu.existsByNazwaAndUzytkownikIsNull(dto.getNazwa())) {
            throw new DuplikatException("Systemowy szablon o nazwie '" + dto.getNazwa() + "' już istnieje.");
        }
        SzablonBudzetu szablon = new SzablonBudzetu();
        szablon.setNazwa(dto.getNazwa());
        szablon.setOpis(dto.getOpis());
        szablon.setUzytkownik(null); // Szablon systemowy
        szablon.setCzyPubliczny(true); // Szablony systemowe są publiczne

        szablon.setProcentNaPotrzeby(dto.getProcentNaPotrzeby());
        szablon.setProcentNaZachcianki(dto.getProcentNaZachcianki());
        szablon.setProcentNaInwestycje(dto.getProcentNaInwestycje());

        if (dto.getPozycjeSzablonu() != null) {
            for (PozycjaSzablonuBudzetuWysylanieDTO pozDto : dto.getPozycjeSzablonu()) {
                PozycjaSzablonuBudzetu pozycja = new PozycjaSzablonuBudzetu();
                // Dla szablonów systemowych, kategoriaId może nie być bezpośrednio linkowane,
                // lub linkowane do predefiniowanych globalnych kategorii (jeśli takie istnieją)
                // Na razie zakładamy, że metaKategoriaNazwa jest głównym identyfikatorem pozycji w szablonie systemowym.
                pozycja.setMetaKategoriaNazwa(pozDto.getMetaKategoriaNazwa());
                pozycja.setTypAlokacjiEnum(pozDto.getTypAlokacji());
                pozycja.setProcentAlokowany(pozDto.getProcentAlokowany());
                pozycja.setKwotaAlokowana(pozDto.getKwotaAlokowana());
                szablon.dodajPozycjeSzablonu(pozycja);
            }
        }

        SzablonBudzetu zapisanySzablon = magazynSzablonuBudzetu.save(szablon);
        return mapToSzablonResponseDTO(zapisanySzablon);
    }


    @Transactional(readOnly = true)
    public List<SzablonBudzetuOdpowiedzDTO> pobierzDostepneSzablony(String username) {
        Uzytkownik uzytkownik = pobierzBiezacegoUzytkownika(username);
        List<SzablonBudzetu> szablonySystemowe = magazynSzablonuBudzetu.findByUzytkownikIsNullOrderByNazwaAsc();
        List<SzablonBudzetu> szablonyUzytkownika = magazynSzablonuBudzetu.findByUzytkownikOrderByNazwaAsc(uzytkownik);

        return Stream.concat(szablonySystemowe.stream(), szablonyUzytkownika.stream())
                .map(this::mapToSzablonResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SzablonBudzetuOdpowiedzDTO pobierzSzablonPoId(Long szablonId, String username) {
        Uzytkownik uzytkownik = pobierzBiezacegoUzytkownika(username);
        SzablonBudzetu szablon = magazynSzablonuBudzetu.findById(szablonId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Szablon budżetu o ID: " + szablonId + " nie znaleziony."));

        // Sprawdzenie czy użytkownik ma dostęp (swój szablon lub systemowy)
        if (szablon.getUzytkownik() != null && !szablon.getUzytkownik().getId().equals(uzytkownik.getId())) {
            throw new DaneNieZnalesionoExeption("Nie masz uprawnień do tego szablonu.");
        }
        return mapToSzablonResponseDTO(szablon);
    }

    @Transactional
    public void usunSzablonUzytkownika(Long szablonId, String username) {
        Uzytkownik uzytkownik = pobierzBiezacegoUzytkownika(username);
        SzablonBudzetu szablon = magazynSzablonuBudzetu.findByIdAndUzytkownik(szablonId, uzytkownik)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Szablon o ID: " + szablonId + " nie znaleziony lub nie należy do użytkownika."));
        magazynSzablonuBudzetu.delete(szablon);
    }

    // Aktualizacja szablonu użytkownika - podobna do tworzenia
    // Usuwanie/aktualizacja szablonów systemowych wymagałaby uprawnień admina

    private SzablonBudzetuOdpowiedzDTO mapToSzablonResponseDTO(SzablonBudzetu szablon) {
        List<PozycjaSzablonuBudzetuOdpowiedzDTO> pozycjeDto = szablon.getPozycjeSzablonu().stream()
                .map(p -> new PozycjaSzablonuBudzetuOdpowiedzDTO(
                        p.getId(),
                        p.getKategoria() != null ? p.getKategoria().getId() : null,
                        p.getKategoria() != null ? p.getKategoria().getNazwa() : null,
                        p.getMetaKategoriaNazwa(),
                        p.getTypAlokacjiEnum(),
                        p.getProcentAlokowany(),
                        p.getKwotaAlokowana()
                ))
                .collect(Collectors.toList());

        return new SzablonBudzetuOdpowiedzDTO(
                szablon.getId(),
                szablon.getNazwa(),
                szablon.getOpis(),
                szablon.getUzytkownik() != null ? szablon.getUzytkownik().getId() : null,
                szablon.getUzytkownik() == null, // czySystemowy
                szablon.getProcentNaPotrzeby(),
                szablon.getProcentNaZachcianki(),
                szablon.getProcentNaInwestycje(),
                pozycjeDto
        );
    }
    // W klasie SzablonBudzetuUsługa

    @Transactional
    public SzablonBudzetuOdpowiedzDTO zaktualizujSzablonUzytkownika(Long szablonId, SzablonBudzetuWysylanieDTO dto, String nazwaUzytkownika) {
        // 1. Pobierz użytkownika

        Uzytkownik uzytkownik = magazynUzytkownika.findByNazwa(nazwaUzytkownika);
        //wyżucenie wątku DaneNIeznalezionoExeption
        // 2. Pobierz istniejący szablon budżetu
        SzablonBudzetu szablonDoAktualizacji = magazynSzablonuBudzetu.findById(szablonId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Szablon budżetu o ID " + szablonId + " nie został znaleziony."));

        // 3. Sprawdź, czy użytkownik jest właścicielem szablonu
        if (szablonDoAktualizacji.getUzytkownik() == null || !szablonDoAktualizacji.getUzytkownik().getId().equals(uzytkownik.getId())) {
            throw new ForbiddenAccessException("Brak uprawnień do aktualizacji tego szablonu budżetu.");
        }

        // 4. Zaktualizuj nazwę szablonu, jeśli została podana
        if (dto.getNazwa() != null && !dto.getNazwa().isBlank()) {
            szablonDoAktualizacji.setNazwa(dto.getNazwa());
        }

        // 5. Zaktualizuj pozycje szablonu
        if (dto.getPozycjeSzablonu() != null) {
            // Najpierw usuń istniejące pozycje (lub zaimplementuj bardziej złożoną logikę aktualizacji)
            // Dla uproszczenia, tutaj usuwamy wszystkie i dodajemy nowe.
            // W bardziej zaawansowanym scenariuszu można by porównywać istniejące pozycje z nowymi
            // i aktualizować, dodawać lub usuwać tylko te, które się zmieniły.
            szablonDoAktualizacji.getPozycjeSzablonu().clear(); // Wymaga orphanRemoval=true w relacji @OneToMany

            for (PozycjaSzablonuBudzetuWysylanieDTO pozycjaDto : dto.getPozycjeSzablonu()) {
                PozycjaSzablonuBudzetu nowaPozycja = new PozycjaSzablonuBudzetu();
                nowaPozycja.setSzablonBudzetu(szablonDoAktualizacji); // Ustawienie referencji zwrotnej

                // Mapowanie pól z DTO do encji PozycjaSzablonuBudzetu
                if (pozycjaDto.getKategoriaId() != null) {
                    Kategoria kategoria = magazynKategorii.findById(pozycjaDto.getKategoriaId())
                            .orElseThrow(() -> new DaneNieZnalesionoExeption("Kategoria o ID " + pozycjaDto.getKategoriaId() + " nie została znaleziona."));
                    nowaPozycja.setKategoria(kategoria);
                }
                nowaPozycja.setMetaKategoriaNazwa(pozycjaDto.getMetaKategoriaNazwa());
                nowaPozycja.setTypAlokacjiEnum(pozycjaDto.getTypAlokacji()); // Upewnij się, że nazwa pola w DTO to getTypAlokacji()
                nowaPozycja.setProcentAlokowany(pozycjaDto.getProcentAlokowany());
                nowaPozycja.setKwotaAlokowana(pozycjaDto.getKwotaAlokowana());

                szablonDoAktualizacji.getPozycjeSzablonu().add(nowaPozycja);
            }
        }

        // 6. Zapisz zaktualizowany szablon
        SzablonBudzetu zapisanySzablon = magazynSzablonuBudzetu.save(szablonDoAktualizacji);

        // 7. Zmapuj zaktualizowaną encję na DTO odpowiedzi
        return mapujNaSzablonBudzetuResponseDTO(zapisanySzablon); // Załóżmy, że masz taką metodę mapującą
    }

    // Przykładowa metoda mapująca (powinna być w serwisie lub dedykowanej klasie mappera)
    private SzablonBudzetuOdpowiedzDTO mapujNaSzablonBudzetuResponseDTO(SzablonBudzetu szablon) {
        Long id = szablon.getId();
        String nazwa = szablon.getNazwa();
        String opis = szablon.getOpis();
        Integer uzytkownikId = szablon.getUzytkownik() != null ? szablon.getUzytkownik().getId() : null;
        Uzytkownik uzytkownikEncji = szablon.getUzytkownik();
        boolean czySystemowy = false;

        if (uzytkownikEncji != null) {
            uzytkownikId = uzytkownikEncji.getId();
            // Twoja nowa definicja "systemowego" szablonu - jeśli jest powiązany z adminem
            if (uzytkownikEncji.getRola() == RolaEnum.ADMIN) {
                czySystemowy = true;
            }
        }
        Integer procentNaPotrzeby = szablon.getProcentNaPotrzeby();
        Integer procentNaZachcianki = szablon.getProcentNaZachcianki();
        Integer procentNaInwestycje = szablon.getProcentNaInwestycje();

        // Mapowanie listy pozycji szablonu
        List<PozycjaSzablonuBudzetuOdpowiedzDTO> pozycjeDto = new ArrayList<>(); // Domyślnie pusta lista
        if (szablon.getPozycjeSzablonu() != null) {
            pozycjeDto = szablon.getPozycjeSzablonu().stream()
                    .map(pozycjaEncja -> new PozycjaSzablonuBudzetuOdpowiedzDTO(
                            pozycjaEncja.getId(),
                            pozycjaEncja.getKategoria() != null ? pozycjaEncja.getKategoria().getId() : null,
                            pozycjaEncja.getKategoria() != null ? pozycjaEncja.getKategoria().getNazwa() : null,
                            pozycjaEncja.getMetaKategoriaNazwa(),
                            pozycjaEncja.getTypAlokacjiEnum(),
                            pozycjaEncja.getProcentAlokowany(),
                            pozycjaEncja.getKwotaAlokowana()
                    ))
                    .collect(Collectors.toList());
        }

        return new SzablonBudzetuOdpowiedzDTO(
                id,
                nazwa,
                opis,
                uzytkownikId,
                czySystemowy,
                procentNaPotrzeby,
                procentNaZachcianki,
                procentNaInwestycje,
                pozycjeDto // Przekazanie zmapowanej listy pozycji
        );
    }
}
