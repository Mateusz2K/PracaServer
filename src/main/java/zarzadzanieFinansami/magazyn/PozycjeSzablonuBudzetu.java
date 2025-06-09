package zarzadzanieFinansami.magazyn;

import org.springframework.data.jpa.repository.JpaRepository;
import zarzadzanieFinansami.modele.PozycjaSzablonuBudzetu;

public interface PozycjeSzablonuBudzetu extends JpaRepository<PozycjaSzablonuBudzetu, Long> {
}
