// src/main/java/zarzadzanieFinansami/serwisy/CelUsługa.java
package zarzadzanieFinansami.serwisy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zarzadzanieFinansami.DTO.cel.CelOdpowiedzDTO;
import zarzadzanieFinansami.DTO.cel.CelWysylanieDTO;
import zarzadzanieFinansami.DTO.cel.ZasilenieCeluDTO;
import zarzadzanieFinansami.magazyn.MagazynCelu;
import zarzadzanieFinansami.magazyn.MagazynKonta;
import zarzadzanieFinansami.magazyn.MagazynUzytkownika;
import zarzadzanieFinansami.modele.Cel;
import zarzadzanieFinansami.modele.Konto;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.modele.enumeracje.CelStatusEnum;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;
import zarzadzanieFinansami.wyjątki.DuplikatException;
import zarzadzanieFinansami.wyjątki.ForbiddenAccessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CelUsługa {

    private final MagazynCelu magazynCelu;
    private final MagazynUzytkownika magazynUzytkownika;
    private final MagazynKonta magazynKonta;

    @Autowired
    public CelUsługa(MagazynCelu magazynCelu, MagazynUzytkownika magazynUzytkownika, MagazynKonta magazynKonta) {
        this.magazynCelu = magazynCelu;
        this.magazynUzytkownika = magazynUzytkownika;
        this.magazynKonta = magazynKonta;
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
                cel.getUzytkownik().getId(),
                cel.getKonto().getId(),
                cel.getStatus()
        );
    }

    @Transactional
    public CelOdpowiedzDTO stworzCel(CelWysylanieDTO dto, String emailUzytkownika) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(emailUzytkownika);
        if (magazynCelu.existsByNazwaCeluAndUzytkownikId(dto.getNazwaCelu(), uzytkownik.getId())) {
            throw new DuplikatException("Cel o nazwie '" + dto.getNazwaCelu() + "' już istnieje dla tego użytkownika.");
        }

        Konto konto = magazynKonta.findById(dto.getKontoId())
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Konto o ID: " + dto.getKontoId() + " nie istnieje."));

        // Walidacja, czy konto należy do zalogowanego użytkownika
        if (!Objects.equals(konto.getUzytkownik().getId(), uzytkownik.getId())) {
            throw new ForbiddenAccessException("Nie masz dostępu do konta o ID: " + dto.getKontoId());
        }


        Cel cel = new Cel();
        cel.setNazwaCelu(dto.getNazwaCelu());
        cel.setKwotaDocelowa(dto.getKwotaDocelowa());
        cel.setAktualnaKwota(BigDecimal.ZERO); // Domyślnie 0
        cel.setDataRozpoczecia(LocalDate.now());
        cel.setDataZakonczenia(dto.getDataZakonczenia());
        cel.setOpis(dto.getOpis());
        cel.setUzytkownik(uzytkownik);
        cel.setKonto(konto);


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
    public CelOdpowiedzDTO dodajSrodkiDoCelu(Integer celId, ZasilenieCeluDTO dto, String emailUzytkownika) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(emailUzytkownika);
        Cel cel = magazynCelu.findByUzytkownikIdAndId(uzytkownik.getId(), celId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Cel o ID: " + celId + " nie został znaleziony lub nie należy do użytkownika."));

        BigDecimal kwota = dto.getKwota();
        Konto kontoDocelowe = cel.getKonto();

        if (dto.getKontoZrodloweId() != null) {
            Integer kontoZrodloweId = dto.getKontoZrodloweId();


            if (Objects.equals(kontoZrodloweId, kontoDocelowe.getId())) {
                throw new IllegalArgumentException("Konto źródlowe nie może być takie samo jak konto docelowe");
            }
            Konto kontoZrodlowe = magazynKonta.findById(kontoZrodloweId)
                    .orElseThrow(() -> new DaneNieZnalesionoExeption("Konto o ID: " + kontoZrodloweId + " nie istnieje."));
            if (!Objects.equals(kontoZrodlowe.getUzytkownik().getId(), uzytkownik.getId())) {
                throw new ForbiddenAccessException("Nie masz dostępu do konta o ID: " + kontoZrodloweId);
            }
            if (kontoZrodlowe.getBilans().compareTo(kwota) < 0) {
                throw new IllegalArgumentException("Konto źródlowe nie ma wystarczających środków na koncie.");
            }
            kontoZrodlowe.setBilans(kontoZrodlowe.getBilans().subtract(kwota));
            kontoDocelowe.setBilans(kontoDocelowe.getBilans().add(kwota));

            magazynKonta.save(kontoZrodlowe);
        }
        else {
            //Zwykła wpłata z zewnątrz
            kontoDocelowe.setBilans(kontoDocelowe.getBilans().add(kwota));
        }
        // Aktualizacja aktualnej kwoty w celu
        cel.setAktualnaKwota(cel.getAktualnaKwota().add(kwota));

        if(cel.getAktualnaKwota().compareTo(cel.getKwotaDocelowa()) >= 0){
            cel.setStatus(CelStatusEnum.ZAKOŃCZONY);
        }

        // Zapisanie zmian na obu encjach w ramach jednej transakcji
        magazynKonta.save(kontoDocelowe);
        Cel zapisanyCel = magazynCelu.save(cel);
        return mapToCelResponseDTO(zapisanyCel);
    }
}