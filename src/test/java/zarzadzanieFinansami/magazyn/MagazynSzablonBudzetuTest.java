package zarzadzanieFinansami.magazyn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import zarzadzanieFinansami.modele.SzablonBudzetu;
import zarzadzanieFinansami.modele.Uzytkownik;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MagazynSzablonBudzetuTest {

    @Autowired
    private TestEntityManager entityManager; // Narzędzie do przygotowania danych w bazie

    @Autowired
    private MagazynSzablonBudzetu magazynSzablonBudzetu; // Testowane repozytorium

    private Uzytkownik testowyUzytkownik;

    @BeforeEach
    void setUp() {//TODO: poprawić zapisywanie uzytkownika zgodnie z regułami
        // Arrange: Tworzymy użytkownika przed każdym testem, bo jest on często wymaganą zależnością.
        testowyUzytkownik = new Uzytkownik();
        testowyUzytkownik.setName("testuser");
        testowyUzytkownik.setEmail("test@user.com");
        testowyUzytkownik.setHaslo("secure_password");
        entityManager.persistAndFlush(testowyUzytkownik);
    }

    @Test
    public void kiedyZapisanoSzablonUzytkownika_toPowinienBycPoprawnieZnaleziony() {
        // Arrange
        SzablonBudzetu szablonPrywatny = new SzablonBudzetu();
        szablonPrywatny.setOpis("Opis prywatnego szablonu");
        szablonPrywatny.setNazwa("Prywatny szablon");
        szablonPrywatny.setUzytkownik(testowyUzytkownik);

        // Act
        SzablonBudzetu zapisanySzablon = magazynSzablonBudzetu.save(szablonPrywatny);

        // Assert
        assertThat(zapisanySzablon).isNotNull();
        assertThat(zapisanySzablon.getId()).isPositive();
        assertThat(zapisanySzablon.getUzytkownik()).isEqualTo(testowyUzytkownik);
    }

    @Test
    public void kiedyZapisanoSzablonSystemowy_toPowinienBycBezUzytkownika() {
        // Arrange
        SzablonBudzetu szablonSystemowy = new SzablonBudzetu();
        szablonSystemowy.setNazwa("Szablon systemowy");
        szablonSystemowy.setOpis("Opis systemowy");

        // Act
        SzablonBudzetu zapisanySzablon = magazynSzablonBudzetu.save(szablonSystemowy);

        // Assert
        assertThat(zapisanySzablon).isNotNull();
        assertThat(zapisanySzablon.getUzytkownik()).isNull();
    }

    @Test
    public void kiedyPobieraneSaSzablonySystemowe_toPowinnyBycTylkoTeBezUzytkownika() {
        // Arrange
        SzablonBudzetu szablonSystemowy = new SzablonBudzetu();
        szablonSystemowy.setNazwa("Szablon systemowy A");
        szablonSystemowy.setOpis("Opis");
        SzablonBudzetu szablonPrywatny = new SzablonBudzetu();
        szablonPrywatny.setNazwa("Szablon prywatny B");
        szablonPrywatny.setUzytkownik(testowyUzytkownik);
        entityManager.persist(szablonSystemowy);
        entityManager.persist(szablonPrywatny);
        entityManager.flush();

        // Act
        List<SzablonBudzetu> szablonySystemowe = magazynSzablonBudzetu.findByUzytkownikIsNullOrderByNazwaAsc();

        // Assert
        assertThat(szablonySystemowe).hasSize(1);
        assertThat(szablonySystemowe.get(0).getNazwa()).isEqualTo("Szablon systemowy A");
        assertThat(szablonySystemowe.get(0).getUzytkownik()).isNull();
    }

    @Test
    public void kiedyPobieraneSaSzablonyUzytkownika_toPowinnyBycTylkoJegoWlasne() {
        // Arrange
        SzablonBudzetu szablonSystemowy = new SzablonBudzetu();
        szablonSystemowy.setNazwa("Szablon systemowy C");
        szablonSystemowy.setOpis("Opis");
        SzablonBudzetu szablonPrywatny = new SzablonBudzetu();
        szablonPrywatny.setNazwa("Szablon prywatny D");
        szablonPrywatny.setUzytkownik(testowyUzytkownik);
        entityManager.persist(szablonSystemowy);
        entityManager.persist(szablonPrywatny);
        entityManager.flush();

        // Act
        List<SzablonBudzetu> szablonyUzytkownika = magazynSzablonBudzetu.findByUzytkownikOrderByNazwaAsc(testowyUzytkownik);

        // Assert
        assertThat(szablonyUzytkownika).hasSize(1);
        assertThat(szablonyUzytkownika.get(0).getNazwa()).isEqualTo("Szablon prywatny D");
        assertThat(szablonyUzytkownika.get(0).getUzytkownik()).isEqualTo(testowyUzytkownik);
    }

    @Test
    public void kiedySprawdzanaJestUnikalnoscNazwy_toExistsPowinnoDzialacPoprawnie() {
        // Arrange
        SzablonBudzetu szablonSystemowy = new SzablonBudzetu();
        szablonSystemowy.setNazwa("UnikalnySystemowy");
        szablonSystemowy.setOpis("Opis");
        SzablonBudzetu szablonPrywatny = new SzablonBudzetu();
        szablonPrywatny.setNazwa("UnikalnyPrywatny");
        szablonPrywatny.setUzytkownik(testowyUzytkownik);
        entityManager.persist(szablonSystemowy);
        entityManager.persist(szablonPrywatny);
        entityManager.flush();

        // Act & Assert
        // Sprawdzenie dla szablonów systemowych
        assertThat(magazynSzablonBudzetu.existsByNazwaAndUzytkownikIsNull("UnikalnySystemowy")).isTrue();
        assertThat(magazynSzablonBudzetu.existsByNazwaAndUzytkownikIsNull("NieistniejacySystemowy")).isFalse();

        // Sprawdzenie dla szablonów użytkownika
        assertThat(magazynSzablonBudzetu.existsByNazwaAndUzytkownik("UnikalnyPrywatny", testowyUzytkownik)).isTrue();
        assertThat(magazynSzablonBudzetu.existsByNazwaAndUzytkownik("NieistniejacyPrywatny", testowyUzytkownik)).isFalse();

        // Sprawdzenie, czy nazwa szablonu systemowego nie koliduje z prywatnym
        assertThat(magazynSzablonBudzetu.existsByNazwaAndUzytkownik("UnikalnySystemowy", testowyUzytkownik)).isFalse();
    }
}