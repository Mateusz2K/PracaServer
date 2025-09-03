package zarzadzanieFinansami.magazyn;

import org.springframework.data.jpa.repository.JpaRepository;
import zarzadzanieFinansami.modele.PozycjaSzablonuBudzetu;

public interface MagazynPozycjeSzablonuBudzetu extends JpaRepository<PozycjaSzablonuBudzetu, Long> {
}
