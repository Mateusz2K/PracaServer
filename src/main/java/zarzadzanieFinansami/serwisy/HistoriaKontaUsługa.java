//package zarzadzanieFinansami.serwisy;
//
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
////import zarzadzanieFinansami.magazyn.MagazynHistoriKonta;
//import zarzadzanieFinansami.magazyn.MagazynKonta;
//import zarzadzanieFinansami.magazyn.MagazynTransakcji;
////import zarzadzanieFinansami.modele.HistoriaKonta;
//import zarzadzanieFinansami.modele.Konto;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@Service
//public class HistoriaKontaUsługa {
//    @Autowired
//    private final MagazynHistoriKonta magazynHistoriKonta;
//    @Autowired
//    private final MagazynKonta magazynKonta;
//    @Autowired
//    private final MagazynTransakcji magazynTransakcji;
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    public HistoriaKontaUsługa(MagazynHistoriKonta magazynHistoriKonta, MagazynKonta magazynKonta, MagazynTransakcji magazynTransakcji) {
//        this.magazynHistoriKonta = magazynHistoriKonta;
//        this.magazynKonta = magazynKonta;
//        this.magazynTransakcji = magazynTransakcji;
//    }
//    public HistoriaKonta stworzHistoriaKonta(LocalDateTime dataZmiany, BigDecimal kwotaZmiany, BigDecimal bilans, int idKonto, int idTransakcja){
//        HistoriaKonta historiaKonta = new HistoriaKonta();
//        historiaKonta.setDataZmiany(dataZmiany);
//        historiaKonta.setKwotaZmiany(kwotaZmiany);
//        historiaKonta.setBilans(bilans);
//        historiaKonta.setKonto(magazynKonta.getReferenceById(idKonto));
//        historiaKonta.setTransakcja(magazynTransakcji.getReferenceById(idTransakcja));
//        return magazynHistoriKonta.save(historiaKonta);
//    }
//    public void usunHistorieKonta(int id){
//        HistoriaKonta historiaKonta = magazynHistoriKonta.findById(id).orElseThrow(() -> new IllegalArgumentException("Urzytkownik o id: " + id + " nie istnieje"));
//        magazynHistoriKonta.delete(historiaKonta);
//    }
//    public HistoriaKonta znajdzHistoriaKonta(int idKonto, int id){
//
//        return magazynHistoriKonta.findById(id).orElseThrow(() -> new IllegalArgumentException("Historia Konta o id: " + id + " nie istnieje"));
//    }
//    public HistoriaKonta znajdzHistoriaKonta(int idKonto){
//        Konto konto = magazynKonta.getReferenceById(idKonto);
//        Optional<HistoriaKonta> historiaKonta = magazynHistoriKonta.getHistoriaKontaByKonto(konto);
//        return historiaKonta.orElse(null);
//
//    }
//
////    public HistoriaKonta znajdzHistorieDanegoKonta(int kontoId){
////        HistoriaKonta historiaKonta = magazynHistoriKont
////    }
//
//}
