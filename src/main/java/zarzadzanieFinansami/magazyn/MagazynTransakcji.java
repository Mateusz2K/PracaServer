package zarzadzanieFinansami.magazyn;

import zarzadzanieFinansami.modele.Kategoria;
import zarzadzanieFinansami.modele.Konto;
import zarzadzanieFinansami.modele.Transakcja;
import org.springframework.data.jpa.repository.JpaRepository;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MagazynTransakcji extends JpaRepository<Transakcja, Integer> {
    @Override
    <S extends Transakcja> S saveAndFlush(S entity);

    @Override
    void delete(Transakcja entity);

    @Override
    void deleteAll();

    @Override
    void deleteById(Integer integer);

    @Override
    long count();

    @Override
    boolean existsById(Integer integer);

    @Override
    Optional<Transakcja> findById(Integer integer);

    @Override
    List<Transakcja> findAll();

    // Nowa metoda do pobierania transakcji dla użytkownika w danym okresie,
    // posortowane od najnowszej
    // Zakłada, że encja Transakcja ma pole 'konto', a encja Konto ma pole 'uzytkownik'
    List<Transakcja> findByKonto_UzytkownikAndDataBetweenOrderByDataDesc(Uzytkownik uzytkownik, LocalDate dataOd, LocalDate dataDo);
    List<Transakcja> findByKontoAndDataBetweenOrderByDataDesc(Konto konto, LocalDate dataOd, LocalDate dataDo);
    List<Transakcja> findByKontoOrderByDataDesc(Konto konto);
    List<Transakcja> findByKonto_UzytkownikOrderByDataDesc(Uzytkownik uzytkownik);

    List<Transakcja> findByKontoAndDataGreaterThanEqualOrderByDataDesc(Konto konto, LocalDate dataOd);
    List<Transakcja> findByKontoAndDataLessThanEqualOrderByDataDesc(Konto konto, LocalDate dataDo);

    List<Transakcja> findByKonto_UzytkownikAndDataGreaterThanEqualOrderByDataDesc(Uzytkownik uzytkownik, LocalDate dataOd);
    List<Transakcja> findByKonto_UzytkownikAndDataLessThanEqualOrderByDataDesc(Uzytkownik uzytkownik, LocalDate dataDo);

    // Dla konkretnego konta i kategorii
    List<Transakcja> findByKontoAndKategoriaAndDataBetweenOrderByDataDesc(Konto konto, Kategoria kategoria, LocalDate dataOd, LocalDate dataDo);
    List<Transakcja> findByKontoAndKategoriaAndDataGreaterThanEqualOrderByDataDesc(Konto konto, Kategoria kategoria, LocalDate dataOd);
    List<Transakcja> findByKontoAndKategoriaAndDataLessThanEqualOrderByDataDesc(Konto konto, Kategoria kategoria, LocalDate dataDo);
    List<Transakcja> findByKontoAndKategoriaOrderByDataDesc(Konto konto, Kategoria kategoria);

    // Dla wszystkich kont użytkownika i konkretnej kategorii
    List<Transakcja> findByKonto_UzytkownikAndKategoriaAndDataBetweenOrderByDataDesc(Uzytkownik uzytkownik, Kategoria kategoria, LocalDate dataOd, LocalDate dataDo);
    List<Transakcja> findByKonto_UzytkownikAndKategoriaAndDataGreaterThanEqualOrderByDataDesc(Uzytkownik uzytkownik, Kategoria kategoria, LocalDate dataOd);
    List<Transakcja> findByKonto_UzytkownikAndKategoriaAndDataLessThanEqualOrderByDataDesc(Uzytkownik uzytkownik, Kategoria kategoria, LocalDate dataDo);
    List<Transakcja> findByKonto_UzytkownikAndKategoriaOrderByDataDesc(Uzytkownik uzytkownik, Kategoria kategoria);

    List<Transakcja> findByKonto_UzytkownikAndKategoriaAndTypAndDataBetweenOrderByDataDesc(Uzytkownik uzytkownik, Kategoria kategoria, TypTransakcjiEnum typTransakcjiEnum, LocalDate dataOd, LocalDate dataDo);

    List<Transakcja> findByKonto_UzytkownikAndTypAndDataBetweenOrderByDataDesc(Uzytkownik uzytkownik, TypTransakcjiEnum typTransakcjiEnum, LocalDate dataOd, LocalDate dataDo);
}
