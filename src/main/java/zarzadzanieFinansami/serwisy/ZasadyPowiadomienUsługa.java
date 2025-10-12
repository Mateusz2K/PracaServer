package zarzadzanieFinansami.serwisy;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import zarzadzanieFinansami.DTO.zasady.ZasadyPowiadomienOdpowiedzDTO;
import zarzadzanieFinansami.DTO.zasady.ZasadyPowiadomienWysylanieDTO;
import zarzadzanieFinansami.magazyn.MagazynCelu;
import zarzadzanieFinansami.magazyn.MagazynKonta;
import zarzadzanieFinansami.magazyn.MagazynUzytkownika;
import zarzadzanieFinansami.magazyn.MagazynZasadPowiadomien;
import zarzadzanieFinansami.modele.*;
import org.springframework.stereotype.Service;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;
import zarzadzanieFinansami.wyjątki.ForbiddenAccessException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ZasadyPowiadomienUsługa {
    private final MagazynZasadPowiadomien magazynZasadPowiadomien;
    private final MagazynUzytkownika magazynUzytkownika;
    private final MagazynCelu magazynCelu;
    private final MagazynKonta magazynKonta;

    public ZasadyPowiadomienUsługa(MagazynZasadPowiadomien magazynZasadPowiadomien, MagazynUzytkownika magazynUzytkownika, MagazynCelu magazynCelu, MagazynKonta magazynKonta){
        this.magazynZasadPowiadomien = magazynZasadPowiadomien;
        this.magazynUzytkownika = magazynUzytkownika;
        this.magazynCelu = magazynCelu;
        this.magazynKonta = magazynKonta;
    }

    private Uzytkownik pobierzUzytkownikaPoNazwie(String email) {
        return magazynUzytkownika.findByEmail(email)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Użytkownik o emailu: " + email + " nie został znaleziony."));
    }

    private ZasadyPowiadomienOdpowiedzDTO mapToDto(ZasadyPowiadomien zasada) {
        return new ZasadyPowiadomienOdpowiedzDTO(
                zasada.getId(),
                zasada.getRegula(),
                zasada.getWartoscLimit(),
                zasada.isCzyAktywna(),
                zasada.getKonto() != null ? zasada.getKonto().getId() : null,
                zasada.getCel() != null ? zasada.getCel().getId() : null
        );
    }

    @Transactional
    public ZasadyPowiadomienOdpowiedzDTO stworzZasade(@Valid ZasadyPowiadomienWysylanieDTO dto, String email) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(email);

        Konto konto = magazynKonta.findById(dto.getKontoId())
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Konto o ID: " + dto.getKontoId() + " nie istnieje."));
        if (!Objects.equals(konto.getUzytkownik().getId(), uzytkownik.getId())) {
            throw new ForbiddenAccessException("Nie masz dostępu do konta o ID: " + dto.getKontoId());
        }

        Cel cel = null;
        if (dto.getCelId() != null) {
            cel = magazynCelu.findById(dto.getCelId())
                    .orElseThrow(() -> new DaneNieZnalesionoExeption("Cel o ID: " + dto.getCelId() + " nie istnieje."));
            if (!Objects.equals(cel.getUzytkownik().getId(), uzytkownik.getId())) {
                throw new ForbiddenAccessException("Nie masz dostępu do celu o ID: " + dto.getCelId());
            }
        }

        ZasadyPowiadomien nowaZasada = new ZasadyPowiadomien();
        nowaZasada.setUzytkownik(uzytkownik);
        nowaZasada.setKonto(konto);
        nowaZasada.setCel(cel);
        nowaZasada.setRegula(dto.getRegula());
        nowaZasada.setWartoscLimit(dto.getWartoscLimit());
        nowaZasada.setCzyAktywna(dto.isCzyAktywna());

        ZasadyPowiadomien zapisanaZasada = magazynZasadPowiadomien.save(nowaZasada);
        return mapToDto(zapisanaZasada);
    }

    @Transactional(readOnly = true)
    public List<ZasadyPowiadomienOdpowiedzDTO> pobierzZasadyDlaUzytkownika(String email) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(email);
        return magazynZasadPowiadomien.findByUzytkownikId(uzytkownik.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ZasadyPowiadomienOdpowiedzDTO aktualizujZasade(Integer zasadaId, @Valid ZasadyPowiadomienWysylanieDTO dto, String email) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(email);
        ZasadyPowiadomien zasada = magazynZasadPowiadomien.findById(zasadaId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Zasada o ID: " + zasadaId + " nie istnieje."));

        if (!Objects.equals(zasada.getUzytkownik().getId(), uzytkownik.getId())) {
            throw new ForbiddenAccessException("Nie masz dostępu do zasady o ID: " + zasadaId);
        }

        // Walidacja i aktualizacja powiązanych encji, jeśli się zmieniły
        if (!Objects.equals(zasada.getKonto().getId(), dto.getKontoId())) {
            Konto noweKonto = magazynKonta.findById(dto.getKontoId())
                    .orElseThrow(() -> new DaneNieZnalesionoExeption("Konto o ID: " + dto.getKontoId() + " nie istnieje."));
            if (!Objects.equals(noweKonto.getUzytkownik().getId(), uzytkownik.getId())) {
                throw new ForbiddenAccessException("Nie masz dostępu do konta o ID: " + dto.getKontoId());
            }
            zasada.setKonto(noweKonto);
        }


        zasada.setRegula(dto.getRegula());
        zasada.setWartoscLimit(dto.getWartoscLimit());
        zasada.setCzyAktywna(dto.isCzyAktywna());

        ZasadyPowiadomien zaktualizowanaZasada = magazynZasadPowiadomien.save(zasada);
        return mapToDto(zaktualizowanaZasada);
    }

    @Transactional
    public void usunZasade(Integer zasadaId, String email) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(email);
        ZasadyPowiadomien zasada = magazynZasadPowiadomien.findById(zasadaId)
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Zasada o ID: " + zasadaId + " nie istnieje."));

        if (!Objects.equals(zasada.getUzytkownik().getId(), uzytkownik.getId())) {
            throw new ForbiddenAccessException("Nie masz dostępu do zasady o ID: " + zasadaId);
        }

        magazynZasadPowiadomien.delete(zasada);
    }
}
