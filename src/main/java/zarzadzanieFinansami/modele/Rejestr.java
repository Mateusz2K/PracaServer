//package zarzadzanieFinansami.modele;
//
//import jakarta.persistence.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//
//@Entity
//@Table(name = "rejestr")
//public class Rejestr {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private int id;
//    @Column(nullable = false)
//    private LocalDate dataOd = LocalDate.now();
//
//    @Column(nullable = false)
//    private LocalDate dataDo = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
//    @Column(precision = 15, scale = 2)
//    private BigDecimal limitBudzet;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "konto_id", nullable = false)
//    private Konto konto;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "kategoria_id", nullable = false)
//    private Kategoria kategoria;
//
//    public Rejestr( BigDecimal limitBudzet, Konto konto, Kategoria kategoria) {
//        this.dataOd = LocalDate.now(); // Domyślnie bieżąca data
//        this.dataDo = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
//        this.limitBudzet = limitBudzet;
//        this.konto = konto;
//        this.kategoria = kategoria;
//    }
//    public Rejestr() {
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
//    public LocalDate getDataOd() {
//        return dataOd;
//    }
//
//    public void setDataOd(LocalDate dataOd) {
//        this.dataOd = dataOd;
//    }
//
//    public LocalDate getDataDo() {
//        return dataDo;
//    }
//
//    public void setDataDo(LocalDate dataDo) {
//        this.dataDo = dataDo;
//    }
//
//    public BigDecimal getLimitBudzet() {
//        return limitBudzet;
//    }
//
//    public void setLimitBudzet(BigDecimal limitBudzet) {
//        this.limitBudzet = limitBudzet;
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
//    public Kategoria getKategoria() {
//        return kategoria;
//    }
//
//    public void setKategoria(Kategoria kategoria) {
//        this.kategoria = kategoria;
//    }
//
//}
