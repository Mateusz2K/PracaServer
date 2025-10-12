package zarzadzanieFinansami.magazyn;

import zarzadzanieFinansami.modele.ZasadyPowiadomien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MagazynZasadPowiadomien extends JpaRepository<ZasadyPowiadomien, Integer> {
    /**
     * Znajduje wszystkie zasady powiadomień dla danego użytkownika.
     */
    List<ZasadyPowiadomien> findByUzytkownikId(Integer uzytkownikId);

    /**
     * Znajduje wszystkie aktywne zasady powiadomień dla danego konta.
     */
    List<ZasadyPowiadomien> findByKontoIdAndCzyAktywnaIsTrue(Integer kontoId);
}
