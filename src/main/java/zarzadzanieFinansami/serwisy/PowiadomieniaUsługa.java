package zarzadzanieFinansami.serwisy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zarzadzanieFinansami.DTO.PowiadomienieOdpowiedzDTO;
import zarzadzanieFinansami.magazyn.MagazynPowiadomienia;
import zarzadzanieFinansami.magazyn.MagazynUzytkownika;
import zarzadzanieFinansami.modele.enumeracje.TypPowiadomieniaEnum;
import zarzadzanieFinansami.modele.Powiadomienia;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PowiadomieniaUsługa {

    private final MagazynPowiadomienia magazynPowiadomienia;
    private final MagazynUzytkownika magazynUzytkownika;

    // Konstruktor jest teraz czystszy - nie potrzebujemy już MagazynTypPowiadomienia
    public PowiadomieniaUsługa(MagazynPowiadomienia magazynPowiadomienia, MagazynUzytkownika magazynUzytkownika) {
        this.magazynPowiadomienia = magazynPowiadomienia;
        this.magazynUzytkownika = magazynUzytkownika;
    }

    private Uzytkownik pobierzUzytkownikaPoNazwie(String nazwa) {
        return magazynUzytkownika.findByNazwa(nazwa);
    }

    private PowiadomienieOdpowiedzDTO mapToDto(Powiadomienia powiadomienie) {
        return new PowiadomienieOdpowiedzDTO(
                powiadomienie.getId(),
                powiadomienie.getWiadomosc(),
                powiadomienie.getWygenerowanyCzas(),
                powiadomienie.isCzyPrzeczytane(),
                // Używamy enuma jako źródła prawdy, unikając "magicznych stringów"
                powiadomienie.getTypPowiadomienia() != null ? powiadomienie.getTypPowiadomienia().getNazwaWyswietlana() :
                        TypPowiadomieniaEnum.WIADOMOSC_SYSTEMOWA.getNazwaWyswietlana()
        );
    }

    @Transactional(readOnly = true)
    public List<PowiadomienieOdpowiedzDTO> pobierzPowiadomieniaDlaUzytkownika(String email) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(email);
        return magazynPowiadomienia.findByUzytkownikIdOrderByWygenerowanyCzasDesc(uzytkownik.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PowiadomienieOdpowiedzDTO> pobierzNieprzeczytanePowiadomieniaDlaUzytkownika(String email) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(email);
        return magazynPowiadomienia.findByUzytkownikIdAndCzyPrzeczytaneIsFalseOrderByWygenerowanyCzasDesc(uzytkownik.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PowiadomienieOdpowiedzDTO oznaczJakoPrzeczytaj(Integer powiadomienieId, String email) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(email);
        Powiadomienia powiadomienie = magazynPowiadomienia.findByIdAndUzytkownikId(powiadomienieId, uzytkownik.getId())
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Powiadomienie o ID: " + powiadomienieId + " nie istnieje lub nie należy do Ciebie."));

        powiadomienie.setCzyPrzeczytane(true);
        Powiadomienia zapisanePowiadomienie = magazynPowiadomienia.save(powiadomienie);
        return mapToDto(zapisanePowiadomienie);
    }

    @Transactional
    public void oznaczWszystkieJakoPrzeczytaj(String email) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(email);
        magazynPowiadomienia.oznaczWszystkieJakoPrzeczytajDlaUzytkownika(uzytkownik.getId());
    }

    @Transactional
    public void usunPowiadomienie(Integer powiadomienieId, String email) {
        Uzytkownik uzytkownik = pobierzUzytkownikaPoNazwie(email);
        Powiadomienia powiadomienie = magazynPowiadomienia.findByIdAndUzytkownikId(powiadomienieId, uzytkownik.getId())
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Powiadomienie o ID: " + powiadomienieId + " nie istnieje lub nie należy do Ciebie."));
        magazynPowiadomienia.delete(powiadomienie);
    }

    /**
     * Tworzy nowe powiadomienie dla użytkownika.
     * Ta metoda będzie wywoływana przez inne serwisy (np. TransakcjaUsługa, BudzetUsługa).
     */
    @Transactional
    public void stworzPowiadomienie(Uzytkownik uzytkownik, String wiadomosc, TypPowiadomieniaEnum typ) {
        // Jeśli typ nie zostanie podany, domyślnie ustawiamy go na WIADOMOSC_SYSTEMOWA
        TypPowiadomieniaEnum typPowiadomienia = (typ != null) ? typ : TypPowiadomieniaEnum.WIADOMOSC_SYSTEMOWA;
        Powiadomienia powiadomienie = new Powiadomienia(wiadomosc, false, uzytkownik, typPowiadomienia);
        magazynPowiadomienia.save(powiadomienie);
    }
}
