package zarzadzanieFinansami.magazyn;

import zarzadzanieFinansami.modele.Kategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.modele.enumeracje.KategorieBudzetEnum;

import java.util.List;
import java.util.Optional;

public interface MagazynKategorii extends JpaRepository<Kategoria, Integer> {
    @Override
    <S extends Kategoria> S saveAndFlush(S entity);

    @Override
    void delete(Kategoria entity);

    @Override
    void deleteAll();

    @Override
    void deleteById(Integer integer);

    @Override
    long count();

    @Override
    boolean existsById(Integer integer);

    @Override
    Optional<Kategoria> findById(Integer integer);

    @Override
    List<Kategoria> findAll();

    Optional<Kategoria> findByNazwa(String nazwa);
    Optional<Kategoria> findByTypTransakcji(String typTransakcji);
    // Znajdź kategorię po nazwie DLA KONKRETNEGO UŻYTKOWNIKA
    Optional<Kategoria> findByUzytkownikAndNazwa(Uzytkownik uzytkownik, String nazwa); // <-- DODAJ

    // Znajdź kategorię po ID DLA KONKRETNEGO UŻYTKOWNIKA
    Optional<Kategoria> findByUzytkownikAndId(Uzytkownik uzytkownik, Integer id); // <-- DODAJ

    // Znajdź wszystkie kategorie DLA KONKRETNEGO UŻYTKOWNIKA
    List<Kategoria> findByUzytkownik(Uzytkownik uzytkownik); // <-- DODAJ

    // Możesz też potrzebować znaleźć po typie transakcji dla użytkownika
    List<Kategoria> findByUzytkownikAndTypTransakcji(Uzytkownik uzytkownik, String typTransakcji); // <-- DODAJ (opcjonalne)

    List<Kategoria> findByUzytkownikAndKategorieBudzet(Uzytkownik uzytkownik, KategorieBudzetEnum kategorieBudzet);


}
