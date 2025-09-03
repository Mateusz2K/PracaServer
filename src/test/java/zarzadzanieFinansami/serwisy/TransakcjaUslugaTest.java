package zarzadzanieFinansami.serwisy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zarzadzanieFinansami.modele.Kategoria;
import zarzadzanieFinansami.modele.Konto;
import zarzadzanieFinansami.modele.Transakcja;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.modele.enumeracje.RolaEnum;
import zarzadzanieFinansami.modele.enumeracje.TypKontaEnum;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

class TransakcjaUslugaTest {

    @BeforeEach
    void setUp() {
        Uzytkownik uzytkownik = new Uzytkownik();
        uzytkownik.setName("TestUser");
        uzytkownik.setEmail("random@email.com");
        uzytkownik.setHaslo("testpassword");
        uzytkownik.setRola(RolaEnum.USER);
        Konto konto = new Konto();
        konto.setNazwa("TestKonto");
        konto.setBilans(BigDecimal.ZERO);
        konto.setUzytkownik(uzytkownik);
        konto.setTyp(TypKontaEnum.OGÓLNE);
        Kategoria koszt = new Kategoria("kosztKategoria", TypTransakcjiEnum.KOSZT, uzytkownik);
        Kategoria przychod = new Kategoria("przychodKategoria", TypTransakcjiEnum.PRZYCHÓD, uzytkownik);
        Transakcja transakcjaKoszt = new Transakcja();
        transakcjaKoszt.setId(1);
        transakcjaKoszt.setOpis("TestOpis");
        transakcjaKoszt.setKwota(BigDecimal.TEN);
        transakcjaKoszt.setTyp(TypTransakcjiEnum.KOSZT);
        transakcjaKoszt.setData(LocalDate.now());
        transakcjaKoszt.setKategoria(koszt);
        transakcjaKoszt.setKonto(konto);
        Transakcja transakcjaPrzychod = new Transakcja("transakcjaPrzychod", BigDecimal.valueOf(20), TypTransakcjiEnum.PRZYCHÓD, LocalDate.parse("2023-01-01"), przychod, konto);
        transakcjaPrzychod.setId(2);
    }

    @Test
    void pobierzTransakcjePoId() {

    }

    @Test
    void dodajTransakcjeDoKonta() {
    }

    @Test
    void usunTransakcje() {
    }

    @Test
    void pobierzTransakcjeWedlugKryteriow() {
    }
}