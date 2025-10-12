//package zarzadzanieFinansami.modele;
//
//import jakarta.persistence.*;
//
//import java.math.BigDecimal;
//import java.sql.Timestamp;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "raport_oszczednosci")
//public class RaportOszczednosci {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private int id;
//    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
//    private Timestamp terminRaportu;
//    @Column(precision = 15, scale = 2)
//    private BigDecimal zaosczedzonaKwota;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "uzytkownik_id", nullable = false)
//    private Uzytkownik uzytkownik;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "cel_id" , nullable = false)
//    private Cel cel;
//
//    public RaportOszczednosci( BigDecimal zaosczedzonaKwota, Uzytkownik uzytkownik, Cel cel) {
//        this.terminRaportu = Timestamp.valueOf(LocalDateTime.now());
//        this.zaosczedzonaKwota = zaosczedzonaKwota;
//        this.uzytkownik = uzytkownik;
//        this.cel = cel;
//    }
//
//    public RaportOszczednosci() {
//    }
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public Timestamp getTerminRaportu() {
//        return terminRaportu;
//    }
//
//    public void setTerminRaportu(LocalDateTime terminRaportu) {
//        this.terminRaportu = Timestamp.valueOf(LocalDateTime.now());
//    }
//
//    public BigDecimal getZaosczedzonaKwota() {
//        return zaosczedzonaKwota;
//    }
//
//    public void setZaosczedzonaKwota(BigDecimal zaosczedzonaKwota) {
//        this.zaosczedzonaKwota = zaosczedzonaKwota;
//    }
//
//    public Uzytkownik getUzytkownik() {
//        return uzytkownik;
//    }
//
//    public void setUzytkownik(Uzytkownik uzytkownik) {
//        this.uzytkownik = uzytkownik;
//    }
//
//    public Cel getCel() {
//        return cel;
//    }
//
//    public void setCel(Cel cel) {
//        this.cel = cel;
//    }
//}
