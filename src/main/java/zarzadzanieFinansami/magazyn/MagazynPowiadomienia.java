package zarzadzanieFinansami.magazyn;

import zarzadzanieFinansami.modele.Powiadomienia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MagazynPowiadomienia extends JpaRepository<Powiadomienia, Integer> {

    List<Powiadomienia> findByUzytkownikIdOrderByWygenerowanyCzasDesc(Integer uzytkownikId);

    List<Powiadomienia> findByUzytkownikIdAndCzyPrzeczytaneIsFalseOrderByWygenerowanyCzasDesc(Integer uzytkownikId);

    Optional<Powiadomienia> findByIdAndUzytkownikId(Integer id, Integer uzytkownikId);

    @Modifying
    @Query("UPDATE Powiadomienia p SET p.czyPrzeczytane = true WHERE p.uzytkownik.id = :uzytkownikId AND p.czyPrzeczytane = false")
    void oznaczWszystkieJakoPrzeczytajDlaUzytkownika(@Param("uzytkownikId") Integer uzytkownikId);
}
