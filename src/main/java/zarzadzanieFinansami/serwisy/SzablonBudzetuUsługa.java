// src/main/java/zarzadzanieFinansami/serwisy/SzablonBudzetuUsługa.java
package zarzadzanieFinansami.serwisy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zarzadzanieFinansami.DTO.budzet.RegulaProcentoweDTO;
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
import zarzadzanieFinansami.modele.enumeracje.TypRegulyBudzetowejEnum;
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
        Uzytkownik uzytkownik = magazynUzytkownika.findByNazwa(username);
        if (uzytkownik == null) {
            throw new DaneNieZnalesionoExeption("Użytkownik o nazwie '" + username + "' nie znaleziony.");
        }
        return uzytkownik;
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

        // Zastosuj reguły z DTO
        zastosujRegulySzablonu(szablon, dto);
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
        // Zastosuj reguły z DTO
        zastosujRegulySzablonu(szablon, dto);
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
        List<PozycjaSzablonuBudzetuOdpowiedzDTO> pozycjeDto = szablon.getPozycjeSzablonu()
                .stream()
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
                szablon.isCzyPubliczny(), // czySystemowy/czyPubliczny
                szablon.getProcentNaPotrzeby(),
                szablon.getProcentNaZachcianki(),
                szablon.getProcentNaInwestycje(),
                pozycjeDto
        );
    }

    @Transactional
    public SzablonBudzetuOdpowiedzDTO zaktualizujSzablonUzytkownika(Long szablonId, SzablonBudzetuWysylanieDTO dto, String username) {
        // 1. Pobierz użytkownika
        Uzytkownik uzytkownik = pobierzBiezacegoUzytkownika(username);
        // 2. Pobierz istniejący szablon budżetu
        SzablonBudzetu szablonDoAktualizacji = magazynSzablonuBudzetu.findById(szablonId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Szablon budżetu o ID " + szablonId + " nie został znaleziony."));

        // 3. Sprawdź, czy użytkownik jest właścicielem szablonu
        if (szablonDoAktualizacji.getUzytkownik() == null || !szablonDoAktualizacji.getUzytkownik().getId().equals(uzytkownik.getId())) {
            throw new ForbiddenAccessException("Brak uprawnień do aktualizacji tego szablonu budżetu.");
        }

        // 4. Zaktualizuj pola szablonu
        if (dto.getNazwa() != null && !dto.getNazwa().isBlank()) {
            szablonDoAktualizacji.setNazwa(dto.getNazwa());
        }
        szablonDoAktualizacji.setOpis(dto.getOpis());

        // Zastosuj zaktualizowane reguły
        zastosujRegulySzablonu(szablonDoAktualizacji, dto);
        // 5. Zaktualizuj pozycje szablonu (usuń stare i dodaj nowe)
        if (dto.getPozycjeSzablonu() != null) {
            szablonDoAktualizacji.getPozycjeSzablonu().clear(); // Wymaga orphanRemoval=true w relacji @OneToMany

            for (PozycjaSzablonuBudzetuWysylanieDTO pozycjaDto : dto.getPozycjeSzablonu()) {
                PozycjaSzablonuBudzetu nowaPozycja = new PozycjaSzablonuBudzetu();
                nowaPozycja.setSzablonBudzetu(szablonDoAktualizacji); // Ustawienie referencji zwrotnej

                if (pozycjaDto.getKategoriaId() != null) {
                    Kategoria kategoria = magazynKategorii.findByUzytkownikAndId(uzytkownik, pozycjaDto.getKategoriaId())
                            .orElseThrow(() -> new DaneNieZnalesionoExeption("Kategoria o ID " + pozycjaDto.getKategoriaId() + " nie została znaleziona dla tego użytkownika."));
                    nowaPozycja.setKategoria(kategoria);
                }
                nowaPozycja.setMetaKategoriaNazwa(pozycjaDto.getMetaKategoriaNazwa());
                nowaPozycja.setTypAlokacjiEnum(pozycjaDto.getTypAlokacji());
                nowaPozycja.setProcentAlokowany(pozycjaDto.getProcentAlokowany());
                nowaPozycja.setKwotaAlokowana(pozycjaDto.getKwotaAlokowana());

                szablonDoAktualizacji.getPozycjeSzablonu().add(nowaPozycja);
            }
        }

        // 6. Zapisz zaktualizowany szablon
        SzablonBudzetu zapisanySzablon = magazynSzablonuBudzetu.save(szablonDoAktualizacji);

        // 7. Zmapuj zaktualizowaną encję na DTO odpowiedzi
        return mapToSzablonResponseDTO(zapisanySzablon);
    }

    // --- METODY POMOCNICZE ---
    private void zastosujRegulySzablonu(SzablonBudzetu szablon, SzablonBudzetuWysylanieDTO dto) {
        // Szablon może definiować tylko regułę procentową, ponieważ jest niezależny od dochodu.
        // Próba utworzenia szablonu z regułą kwotową jest błędem logicznym.
        if (dto.getTypReguly() == TypRegulyBudzetowejEnum.KWOTOWA) {
            throw new IllegalArgumentException("Szablony budżetu nie mogą być tworzone w oparciu o reguły kwotowe. Użyj reguły procentowej.");
        }

        szablon.setTypReguly(dto.getTypReguly());

        if (dto.getTypReguly() == TypRegulyBudzetowejEnum.PROCENTOWA && dto.getRegulaProcentowa() != null) {
            RegulaProcentoweDTO regula = dto.getRegulaProcentowa();
            if (regula.isZastosuj()) {
                szablon.setProcentNaPotrzeby(regula.getProcentNaPotrzeby());
                szablon.setProcentNaZachcianki(regula.getProcentNaZachcianki());
                szablon.setProcentNaInwestycje(regula.getProcentNaInwestycje());
            } else {
                // Jeśli 'zastosuj' jest false, czyścimy reguły
                wyczyscProcentyRegul(szablon);
            }
        } else {
            // Jeśli typReguly == BRAK lub odpowiedni obiekt DTO jest null, czyścimy reguły
            wyczyscProcentyRegul(szablon);
        }
    }

    private void wyczyscProcentyRegul(SzablonBudzetu szablon) {
        szablon.setProcentNaPotrzeby(null);
        szablon.setProcentNaZachcianki(null);
        szablon.setProcentNaInwestycje(null);
    }
}
