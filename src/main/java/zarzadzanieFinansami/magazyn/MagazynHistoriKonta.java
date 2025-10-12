//package zarzadzanieFinansami.magazyn;
//
//import zarzadzanieFinansami.modele.HistoriaKonta;
//import zarzadzanieFinansami.modele.Konto;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface MagazynHistoriKonta extends JpaRepository<HistoriaKonta, Integer> {
//    @Override
//    <S extends HistoriaKonta> S saveAndFlush(S entity);
//
//
//    @Override
//    void delete(HistoriaKonta entity);
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
//    Optional<HistoriaKonta> findById(Integer integer);
//
//    @Override
//    List<HistoriaKonta> findAll();
//
//    Optional<HistoriaKonta> getHistoriaKontaByKonto(Konto konto);
////    Optional<HistoriaKonta> getHistoriaKontByDataZmianyBetween(LocalDateTime start, LocalDateTime end);
//}
