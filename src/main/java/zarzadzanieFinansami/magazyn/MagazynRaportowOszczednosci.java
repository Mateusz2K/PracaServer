//package zarzadzanieFinansami.magazyn;
//
//import zarzadzanieFinansami.modele.RaportOszczednosci;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface MagazynRaportowOszczednosci extends JpaRepository<RaportOszczednosci, Integer> {
//    @Override
//    <S extends RaportOszczednosci> S save(S entity);
//
//    @Override
//    void delete(RaportOszczednosci entity);
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
//    Optional<RaportOszczednosci> findById(Integer integer);
//
//    @Override
//    List<RaportOszczednosci> findAll();
//}
