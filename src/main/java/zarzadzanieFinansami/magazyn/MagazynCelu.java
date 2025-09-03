package zarzadzanieFinansami.magazyn;

import zarzadzanieFinansami.modele.Cel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MagazynCelu extends JpaRepository<Cel, Integer> {
    @Override
    <S extends Cel> S saveAndFlush(S entity);

    @Override
    void delete(Cel entity);

    @Override
    void deleteAll();

    @Override
    void deleteById(Integer integer);

    @Override
    long count();

    @Override
    boolean existsById(Integer integer);

    @Override
    Optional<Cel> findById(Integer integer);

    @Override
    List<Cel> findAll();

    List<Cel> findByUzytkownikId(Integer uzytkownikId);
    Optional<Cel> findByUzytkownikIdAndId(Integer uzytkownikId, Integer id);
    boolean existsByNazwaCeluAndUzytkownikId(String nazwaCelu, Integer uzytkownikId); // Nowa metoda


}
