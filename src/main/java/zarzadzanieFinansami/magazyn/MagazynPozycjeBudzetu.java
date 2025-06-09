package zarzadzanieFinansami.magazyn;

import org.springframework.data.jpa.repository.JpaRepository;
import zarzadzanieFinansami.modele.PozycjaBudzetu;

public interface MagazynPozycjeBudzetu extends JpaRepository<PozycjaBudzetu, Long> {
}
