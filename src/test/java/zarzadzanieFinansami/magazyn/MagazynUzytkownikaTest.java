// Upewnij się, że plik jest w pakiecie: zarzadzanieFinansami.magazyn
package zarzadzanieFinansami.magazyn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import zarzadzanieFinansami.modele.Konto;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.modele.enumeracje.RolaEnum;

// Używamy AssertJ dla bardziej czytelnych asercji
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest automatycznie konfiguruje wszystko dla testów JPA z JUnit 5
@DataJpaTest
public class MagazynUzytkownikaTest {

    // TestEntityManager to świetne narzędzie do zarządzania encjami w testach.
    @Autowired
    private TestEntityManager entityManager;

    // Wstrzykujemy PRAWDZIWE repozytorium, a nie jego atrapę.
    @Autowired
    private MagazynUzytkownika magazynUzytkownika;

    @Test
    public void kiedyZapisanoUzytkownika_toPowinienBycMozliwyDoZnalezieniaPoId() {
        // Arrange (Przygotuj)
        Uzytkownik nowyUzytkownik = new Uzytkownik();
        nowyUzytkownik.setEmail("test@test.com");
        nowyUzytkownik.setName("TestowyUzytkownik"); // Używamy setName, zgodnie z definicją w encji
        nowyUzytkownik.setHaslo("Test1@123");
        nowyUzytkownik.setRola(RolaEnum.USER);
        nowyUzytkownik.setAktywny(true);



        // Act (Działaj)
        Uzytkownik zapisanyUzytkownik = magazynUzytkownika.save(nowyUzytkownik);

        // Assert (Sprawdź)
        assertThat(zapisanyUzytkownik).isNotNull();
        assertThat(zapisanyUzytkownik.getId()).isPositive(); // Sprawdź, czy ID zostało wygenerowane

        Uzytkownik znaleziony = entityManager.find(Uzytkownik.class, zapisanyUzytkownik.getId());
        assertThat(znaleziony.getName()).isEqualTo(nowyUzytkownik.getName());
    }

    @Test
    public void kiedyUzytkownikIstnieje_toExistsByNazwaPowinnoZwrocicPrawde() {
        // Arrange
        Uzytkownik uzytkownik = new Uzytkownik();
        uzytkownik.setEmail("test2@test.com");
        uzytkownik.setName("TestowyUzytkownik2");
        uzytkownik.setHaslo("Test2@123");
        uzytkownik.setRola(RolaEnum.USER);
        uzytkownik.setAktywny(true);
        entityManager.persist(uzytkownik);
        entityManager.flush();

        // Act
        boolean istnieje = magazynUzytkownika.existsByNazwa("TestowyUzytkownik2");

        // Assert
        assertThat(istnieje).isTrue();
    }

    @Test
    public void kiedyUzytkownikNieIstnieje_toExistsByNazwaPowinnoZwrocicFalsz() {
        // Act
        boolean istnieje = magazynUzytkownika.existsByNazwa("NieistniejacyUzytkownik");

        // Assert
        assertThat(istnieje).isFalse();
    }

    @Test
    public void kiedyUzytkownikIstnieje_toExistsByEmailPowinnoZwrocicPrawde() {
        // Arrange
        Uzytkownik uzytkownik = new Uzytkownik();
        uzytkownik.setEmail("test3@test.com");
        uzytkownik.setName("TestowyUzytkownik3");
        uzytkownik.setHaslo("Test3@123");
        uzytkownik.setRola(RolaEnum.USER);
        uzytkownik.setAktywny(true);

        entityManager.persist(uzytkownik);
        entityManager.flush();

        // Act
        boolean istnieje = magazynUzytkownika.existsByEmail("test3@test.com");

        // Assert
        assertThat(istnieje).isTrue();
    }
}