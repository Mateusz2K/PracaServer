//package zarzadzanieFinansami.magazyn;
//
//import zarzadzanieFinansami.modele.ZasadyOszczedzania;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface MagazynZasadOszczedzania extends JpaRepository<ZasadyOszczedzania, Integer> {
//    @Override
//    <S extends ZasadyOszczedzania> S saveAndFlush(S entity);
//
//    @Override
//    void delete(ZasadyOszczedzania entity);
//
//    @Override
//    void deleteAll();
//
//    @Override
//    void deleteById(Integer integer);
//
//    @Override
//    long count();
//
//    @Override
//    boolean existsById(Integer integer);
//
//    @Override
//    Optional<ZasadyOszczedzania> findById(Integer integer);
//
//    @Override
//    List<ZasadyOszczedzania> findAll();
//}
