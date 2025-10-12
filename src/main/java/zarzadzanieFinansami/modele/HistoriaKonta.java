//package zarzadzanieFinansami.modele;
//
//import jakarta.persistence.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "historia_konta")
//public class HistoriaKonta {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//
//    @Column(name = "data_zmiany",nullable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
//    private LocalDateTime dataZmiany;
//    @Column(name = "kwota_zmiany", nullable = false ,precision = 15, scale = 2 )
//    private BigDecimal kwotaZmiany;
//    @Column(name= "bilans",nullable = false, precision = 15, scale = 2)
//    private BigDecimal bilans;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "konto_id")
//    private Konto konto;
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "transakcja_id")
//    private Transakcja transakcja;
//
//    public HistoriaKonta(LocalDateTime dataZmiany, BigDecimal kwotaZmiany, BigDecimal bilans, Konto konto, Transakcja transakcja) {
//        this.dataZmiany = dataZmiany;
//        this.kwotaZmiany = kwotaZmiany;
//        this.bilans = bilans;
//        this.konto = konto;
//        this.transakcja = transakcja;
//    }
//
//    public HistoriaKonta() {
//    }
//
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public LocalDateTime getDataZmiany() {
//        return dataZmiany;
//    }
//
//    public void setDataZmiany(LocalDateTime dataZmiany) {
//        this.dataZmiany = dataZmiany;
//    }
//
//    public BigDecimal getKwotaZmiany() {
//        return kwotaZmiany;
//    }
//
//    public void setKwotaZmiany(BigDecimal kwotaZmiany) {
//        this.kwotaZmiany = kwotaZmiany;
//    }
//
//    public BigDecimal getBilans() {
//        return bilans;
//    }
//
//    public void setBilans(BigDecimal bilans) {
//        this.bilans = bilans;
//    }
//
//    public Konto getKonto() {
//        return konto;
//    }
//
//    public void setKonto(Konto konto) {
//        this.konto = konto;
//    }
//
//    public Transakcja getTransakcja() {
//        return transakcja;
//    }
//
//    public void setTransakcja(Transakcja transakcja) {
//        this.transakcja = transakcja;
//    }
//}
