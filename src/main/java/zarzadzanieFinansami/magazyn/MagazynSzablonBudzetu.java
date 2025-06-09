package zarzadzanieFinansami.magazyn;

import org.springframework.data.jpa.repository.JpaRepository;
import zarzadzanieFinansami.modele.SzablonBudzetu;
import zarzadzanieFinansami.modele.Uzytkownik;

import java.util.List;
import java.util.Optional;

public interface MagazynSzablonBudzetu extends JpaRepository<SzablonBudzetu, Long> {
    List<SzablonBudzetu> findByUzytkownikIsNullOrderByNazwaAsc();
    List<SzablonBudzetu> findByUzytkownikOrderByNazwaAsc(Uzytkownik uzytkownik);
    List<SzablonBudzetu> findByUzytkownikOrUzytkownikIsNull(Uzytkownik uzytkownik);

    Optional<SzablonBudzetu> findByIdAndUzytkownik(Long id, Uzytkownik uzytkownik);
    Optional<SzablonBudzetu> findByIdAndUzytkownikIsNull(Long id); // Dla szablonów systemowych

    boolean existsByNazwaAndUzytkownik(String nazwa, Uzytkownik uzytkownik);
    boolean existsByNazwaAndUzytkownikIsNull(String nazwa); // Dla unikalności nazw szablonów systemowych
}
