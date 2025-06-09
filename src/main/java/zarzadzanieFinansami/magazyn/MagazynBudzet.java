package zarzadzanieFinansami.magazyn;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zarzadzanieFinansami.modele.Budzet;
import zarzadzanieFinansami.modele.Uzytkownik;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MagazynBudzet extends JpaRepository<Budzet, Long> {
    List<Budzet> findByUzytkownikOrderByDataPoczatkowaDesc(Uzytkownik uzytkownik);
    Optional<Budzet> findByIdAndUzytkownik(Long id, Uzytkownik uzytkownik);
    boolean existsByNazwaAndUzytkownikAndDataPoczatkowaBetween(String nazwa, Uzytkownik uzytkownik, LocalDate start, LocalDate end);

    // Znajduje aktywny budżet użytkownika, który obejmuje podaną datę
    Optional<Budzet> findByUzytkownikAndAktywnyTrueAndDataPoczatkowaLessThanEqualAndDataKoncowaGreaterThanEqual(
            Uzytkownik uzytkownik, LocalDate data, LocalDate data2); // data2 to ta sama data, dla spójności z nazwami
}
