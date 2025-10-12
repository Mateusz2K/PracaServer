package zarzadzanieFinansami.magazyn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import zarzadzanieFinansami.modele.Konto;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.modele.enumeracje.RolaEnum;
import zarzadzanieFinansami.modele.enumeracje.TypKontaEnum;
import zarzadzanieFinansami.modele.enumeracje.WalutaEnum;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MagazynKontoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MagazynKonta magazynKonto;

    private Uzytkownik testowyUzytkownik;

    // Metoda @BeforeEach uruchomi się przed każdym testem w tej klasie
    @BeforeEach
    void setUp() {
        // Arrange (Przygotuj) - tworzymy użytkownika, który będzie używany w testach
        Uzytkownik uzytkownik = new Uzytkownik();
        uzytkownik.setEmail("user.for.konto@test.com");
        uzytkownik.setName("UserForKonto");
        uzytkownik.setHaslo("Test1@123");
        uzytkownik.setRola(RolaEnum.USER);
        uzytkownik.setAktywny(true);
        // Zapisujemy użytkownika do testowej bazy danych i przypisujemy do pola w klasie
        testowyUzytkownik = entityManager.persistAndFlush(uzytkownik);
    }

    @Test
    public void kiedyZapisanoKonto_toPowinnoBycMozliweDoZnalezienia() {
        // Arrange
        Konto noweKonto = new Konto();
        noweKonto.setNazwa("Konto Oszczędnościowe");
        noweKonto.setBilans(new BigDecimal("1000.00"));
        noweKonto.setTyp(TypKontaEnum.OSZCZĘDNOŚCIOWE);
        // NAJWAŻNIEJSZE: Przypisujemy wcześniej utworzonego użytkownika do konta
        noweKonto.setUzytkownik(testowyUzytkownik);

        // Act
        Konto zapisaneKonto = magazynKonto.save(noweKonto);

        // Assert
        assertThat(zapisaneKonto).isNotNull();
        assertThat(zapisaneKonto.getId()).isPositive();

        // Sprawdźmy, czy relacja z użytkownikiem została poprawnie zapisana
        assertThat(zapisaneKonto.getUzytkownik()).isNotNull();
        assertThat(zapisaneKonto.getUzytkownik().getId()).isEqualTo(testowyUzytkownik.getId());
        assertThat(zapisaneKonto.getUzytkownik().getName()).isEqualTo("UserForKonto");
    }

    @Test
    public void kiedyUzytkownikMaWieleKont_toFindByUzytkownikPowinnoZwrocicWszystkie() {
        // Arrange
        Konto konto1 = new Konto();
        konto1.setNazwa("Konto Główne");
        konto1.setUzytkownik(testowyUzytkownik);
        konto1.setTyp(TypKontaEnum.OGÓLNE);
        konto1.setBilans(new BigDecimal("5000.00"));

        entityManager.persist(konto1);

        Konto konto2 = new Konto();
        konto2.setNazwa("Konto Walutowe");
        konto2.setUzytkownik(testowyUzytkownik);
        konto2.setTyp(TypKontaEnum.FIRMOWE);
        konto2.setBilans(new BigDecimal("2000.00"));

        entityManager.persist(konto2);

        entityManager.flush();

        // Act
        // Zakładamy, że w MagazynKonto istnieje metoda findByUzytkownik
        List<Konto> znalezioneKonta = magazynKonto.findByUzytkownik(testowyUzytkownik);

        // Assert
        assertThat(znalezioneKonta).isNotNull();
        assertThat(znalezioneKonta).hasSize(2);
        assertThat(znalezioneKonta).contains(konto1, konto2);
    }
}
