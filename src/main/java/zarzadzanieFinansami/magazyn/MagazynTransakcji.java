package zarzadzanieFinansami.magazyn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zarzadzanieFinansami.DTO.budzet.WydatkiKategoriiDTO;
import zarzadzanieFinansami.modele.Transakcja;
import zarzadzanieFinansami.modele.Uzytkownik;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
    // Wszystkie podstawowe metody (save, findById, findAll, delete, etc.) są dziedziczone z JpaRepository.
    // Metoda findAll(Specification) jest dziedziczona z JpaSpecificationExecutor.
    // Dzięki temu nie musimy zaśmiecać interfejsu dziesiątkami metod dla każdej kombinacji filtrów.
    // Cała logika budowania zapytań została przeniesiona do TransakcjaUsługa.

    @Query("SELECT new zarzadzanieFinansami.DTO.budzet.WydatkiKategoriiDTO(t.kategoria.id, SUM(t.kwota)) " +
           "FROM Transakcja t " +
           "WHERE t.konto.uzytkownik = :uzytkownik " +
           "AND t.typ = zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum.KOSZT " +
           "AND t.data BETWEEN :dataOd AND :dataDo " +
           "AND t.kategoria.id IN :kategoriaIds " +
           "GROUP BY t.kategoria.id")
    List<WydatkiKategoriiDTO> obliczSumeWydatkowDlaKategorii(@Param("uzytkownik") Uzytkownik uzytkownik,
                                                            @Param("dataOd") LocalDate dataOd,
                                                            @Param("dataDo") LocalDate dataDo,
                                                            @Param("kategoriaIds") Set<Integer> kategoriaIds);
}
