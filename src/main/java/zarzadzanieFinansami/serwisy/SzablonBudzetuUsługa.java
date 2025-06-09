// src/main/java/zarzadzanieFinansami/serwisy/SzablonBudzetuUsługa.java
package zarzadzanieFinansami.serwisy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zarzadzanieFinansami.DTO.budzet.szablon.PozycjaSzablonuBudzetuRequestDTO;
import zarzadzanieFinansami.DTO.budzet.szablon.PozycjaSzablonuBudzetuResponseDTO;
import zarzadzanieFinansami.DTO.budzet.szablon.SzablonBudzetuRequestDTO;
import zarzadzanieFinansami.DTO.budzet.szablon.SzablonBudzetuResponseDTO;
import zarzadzanieFinansami.magazyn.MagazynKategorii;
import zarzadzanieFinansami.magazyn.MagazynSzablonBudzetu;
import zarzadzanieFinansami.magazyn.MagazynUzytkownika;
import zarzadzanieFinansami.modele.Kategoria;
import zarzadzanieFinansami.modele.PozycjaSzablonuBudzetu;
import zarzadzanieFinansami.modele.SzablonBudzetu;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;
import zarzadzanieFinansami.wyjątki.DuplikatException;

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
    public SzablonBudzetuResponseDTO stworzSzablonUzytkownika(SzablonBudzetuRequestDTO dto, String username) {
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
            for (PozycjaSzablonuBudzetuRequestDTO pozDto : dto.getPozycjeSzablonu()) {
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
    public SzablonBudzetuResponseDTO stworzSzablonSystemowy(SzablonBudzetuRequestDTO dto) {
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
            for (PozycjaSzablonuBudzetuRequestDTO pozDto : dto.getPozycjeSzablonu()) {
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
    public List<SzablonBudzetuResponseDTO> pobierzDostepneSzablony(String username) {
        Uzytkownik uzytkownik = pobierzBiezacegoUzytkownika(username);
        List<SzablonBudzetu> szablonySystemowe = magazynSzablonuBudzetu.findByUzytkownikIsNullOrderByNazwaAsc();
        List<SzablonBudzetu> szablonyUzytkownika = magazynSzablonuBudzetu.findByUzytkownikOrderByNazwaAsc(uzytkownik);

        return Stream.concat(szablonySystemowe.stream(), szablonyUzytkownika.stream())
                .map(this::mapToSzablonResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SzablonBudzetuResponseDTO pobierzSzablonPoId(Long szablonId, String username) {
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

    private SzablonBudzetuResponseDTO mapToSzablonResponseDTO(SzablonBudzetu szablon) {
        List<PozycjaSzablonuBudzetuResponseDTO> pozycjeDto = szablon.getPozycjeSzablonu().stream()
                .map(p -> new PozycjaSzablonuBudzetuResponseDTO(
                        p.getId(),
                        p.getKategoria() != null ? p.getKategoria().getId() : null,
                        p.getKategoria() != null ? p.getKategoria().getNazwa() : null,
                        p.getMetaKategoriaNazwa(),
                        p.getTypAlokacjiEnum(),
                        p.getProcentAlokowany(),
                        p.getKwotaAlokowana()
                ))
                .collect(Collectors.toList());

        return new SzablonBudzetuResponseDTO(
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
}
