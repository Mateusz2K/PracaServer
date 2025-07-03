package zarzadzanieFinansami.magazyn;

import zarzadzanieFinansami.modele.Uzytkownik;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface MagazynUzytkownika extends JpaRepository<Uzytkownik, Integer> {
    Optional<Uzytkownik> findByEmail(String email);

    @Override
    void deleteAll();

    @Override
    void deleteById(Integer integer);

    @Override
    long count();

    @Override
    boolean existsById(Integer integer);

    @Override
    Optional<Uzytkownik> findById(Integer integer);

    @Override
    List<Uzytkownik> findAll();


    Uzytkownik findByNazwa(String nazwa);

    Uzytkownik findUzytkownikByEmail(String email);

    boolean existsByNazwa(String nazwa);

    boolean existsByEmail(String email);
}