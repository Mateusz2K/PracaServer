package zarzadzanieFinansami.magazyn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import zarzadzanieFinansami.modele.Transakcja;

import java.util.List;

/**
 * Magazyn (Repozytorium) dla encji Transakcja.
 * Rozszerza JpaRepository, aby zapewnić standardowe operacje CRUD.
 * Rozszerza JpaSpecificationExecutor, aby umożliwić dynamiczne budowanie zapytań
 * za pomocą kryteriów (Specification), co jest wykorzystywane w TransakcjaUsługa.
 */
public interface MagazynTransakcji extends JpaRepository<Transakcja, Integer>, JpaSpecificationExecutor<Transakcja> {
    Logger log = LoggerFactory.getLogger(MagazynTransakcji.class);

    @Override
    List<Transakcja> findAll();
    // Celowo pozostawione puste.
    // Wszystkie podstawowe metody (save, findById, findAll, delete, etc.) są dziedziczone z JpaRepository.
    // Metoda findAll(Specification) jest dziedziczona z JpaSpecificationExecutor.
    // Dzięki temu nie musimy zaśmiecać interfejsu dziesiątkami metod dla każdej kombinacji filtrów.
    // Cała logika budowania zapytań została przeniesiona do TransakcjaUsługa.
}
