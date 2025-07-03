// src/main/java/zarzadzanieFinansami/serwisy/CelUsługa.java
package zarzadzanieFinansami.serwisy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zarzadzanieFinansami.DTO.cel.CelWysylanieDTO;
import zarzadzanieFinansami.DTO.cel.CelOdpowiedzDTO;
import zarzadzanieFinansami.magazyn.MagazynCelu;
import zarzadzanieFinansami.magazyn.MagazynUzytkownika;
import zarzadzanieFinansami.modele.Cel;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;
import zarzadzanieFinansami.wyjątki.DuplikatException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CelUsługa {

    private final MagazynCelu magazynCelu;
    private final MagazynUzytkownika magazynUzytkownika;

    @Autowired
    public CelUsługa(MagazynCelu magazynCelu, MagazynUzytkownika magazynUzytkownika) {
        this.magazynCelu = magazynCelu;
        this.magazynUzytkownika = magazynUzytkownika;
    }

    private Uzytkownik pobierzUzytkownikaPoNazwie(String email) {
        return magazynUzytkownika.findByNazwa(email);
    }

    private CelOdpowiedzDTO mapToCelResponseDTO(Cel cel) {
        return new CelOdpowiedzDTO(
                cel.getId(),
                cel.getNazwaCelu(),
                cel.getKwotaDocelowa(),
                cel.getAktualnaKwota(),
                cel.getDataRozpoczecia(),
                cel.getDataZakonczenia(),
                cel.getOpis(),
                cel.getUzytkownik().getId()
        );
    }

    @Transactional
    public CelOdpowiedzDTO stworzCel(CelWysylanieDTO dto, String emailUzytkownika) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(emailUzytkownika);
        if (magazynCelu.existsByNazwaCeluAndUzytkownikId(dto.getNazwaCelu(), uzytkownik.getId())) {
            throw new DuplikatException("Cel o nazwie '" + dto.getNazwaCelu() + "' już istnieje dla tego użytkownika.");
        }

        Cel cel = new Cel();
        cel.setNazwaCelu(dto.getNazwaCelu());
        cel.setKwotaDocelowa(dto.getKwotaDocelowa());
        cel.setAktualnaKwota(BigDecimal.ZERO); // Domyślnie 0
        cel.setDataRozpoczecia(LocalDate.now());
        cel.setDataZakonczenia(dto.getDataZakonczenia());
        cel.setOpis(dto.getOpis());
        cel.setUzytkownik(uzytkownik);

        Cel zapisanyCel = magazynCelu.save(cel);
        return mapToCelResponseDTO(zapisanyCel);
    }

    @Transactional(readOnly = true)
    public List<CelOdpowiedzDTO> pobierzCeleUzytkownika(String emailUzytkownika) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(emailUzytkownika);
        return magazynCelu.findByUzytkownikId(uzytkownik.getId())
                .stream()
                .map(this::mapToCelResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CelOdpowiedzDTO pobierzCelPoId(Integer celId, String emailUzytkownika) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(emailUzytkownika);
        Cel cel = magazynCelu.findByUzytkownikIdAndId(uzytkownik.getId(), celId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Cel o ID: " + celId + " nie został znaleziony lub nie należy do użytkownika."));
        return mapToCelResponseDTO(cel);
    }

    @Transactional
    public CelOdpowiedzDTO aktualizujCel(Integer celId, CelWysylanieDTO dto, String emailUzytkownika) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(emailUzytkownika);
        Cel cel = magazynCelu.findByUzytkownikIdAndId(uzytkownik.getId(), celId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Cel o ID: " + celId + " nie został znaleziony lub nie należy do użytkownika."));

        // Sprawdzenie czy nowa nazwa celu nie koliduje z innym celem tego użytkownika
        if (!cel.getNazwaCelu().equals(dto.getNazwaCelu()) &&
                magazynCelu.existsByNazwaCeluAndUzytkownikId(dto.getNazwaCelu(), uzytkownik.getId())) {
            throw new DuplikatException("Inny cel o nazwie '" + dto.getNazwaCelu() + "' już istnieje dla tego użytkownika.");
        }

        cel.setNazwaCelu(dto.getNazwaCelu());
        cel.setKwotaDocelowa(dto.getKwotaDocelowa());
        cel.setDataZakonczenia(dto.getDataZakonczenia());
        cel.setOpis(dto.getOpis());
        // aktualnaKwota i dataRozpoczecia nie są tutaj aktualizowane, chyba że jest taka potrzeba biznesowa

        Cel zaktualizowanyCel = magazynCelu.save(cel);
        return mapToCelResponseDTO(zaktualizowanyCel);
    }

    @Transactional
    public void usunCel(Integer celId, String emailUzytkownika) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(emailUzytkownika);
        Cel cel = magazynCelu.findByUzytkownikIdAndId(uzytkownik.getId(), celId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Cel o ID: " + celId + " nie został znaleziony lub nie należy do użytkownika."));
        magazynCelu.delete(cel);
    }

    @Transactional
    public CelOdpowiedzDTO dodajSrodkiDoCelu(Integer celId, BigDecimal kwota, String emailUzytkownika) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(emailUzytkownika);
        Cel cel = magazynCelu.findByUzytkownikIdAndId(uzytkownik.getId(), celId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Cel o ID: " + celId + " nie został znaleziony lub nie należy do użytkownika."));

        if (kwota.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Kwota do dodania musi być dodatnia.");
        }

        cel.setAktualnaKwota(cel.getAktualnaKwota().add(kwota));
        Cel zapisanyCel = magazynCelu.save(cel);
        return mapToCelResponseDTO(zapisanyCel);
    }
}