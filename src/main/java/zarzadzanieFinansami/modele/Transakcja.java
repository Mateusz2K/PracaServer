package zarzadzanieFinansami.modele;

import jakarta.persistence.*;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "transakcja")
public class Transakcja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String opis;
    @Column(precision = 15,scale = 2)
    private BigDecimal kwota;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypTransakcjiEnum typ;
    @Column(nullable = false)
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kategoria_id", nullable = false) // poprawiona literówka
    private Kategoria kategoria;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "konto_id", nullable = false) // poprawiona literówka
    private Konto konto;

    //historie Kont połączenie forgein
//    @OneToMany(mappedBy = "transakcja", cascade = {CascadeType.DETACH,CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST}, orphanRemoval = false)
//    private List<HistoriaKonta> historieKonta;


    public Transakcja(String opis, BigDecimal kwota, TypTransakcjiEnum typ, LocalDate data, Kategoria kategoria, Konto konto) {
        this.opis = opis;
        this.kwota = kwota;
        this.typ = typ;
        this.data = data;
        this.kategoria = kategoria;
        this.konto = konto;
    }

    public Transakcja() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public BigDecimal getKwota() {
        return kwota;
    }

    public void setKwota(BigDecimal kwota) {
        this.kwota = kwota;
    }

    public TypTransakcjiEnum getTyp() {
        return typ;
    }

    public void setTyp(TypTransakcjiEnum typ) {
        this.typ = typ;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Kategoria getKategoria() {
        return kategoria;
    }

    public void setKategoria(Kategoria kategoria) {
        this.kategoria = kategoria;
    }

    public Konto getKonto() {
        return konto;
    }

    public void setKonto(Konto konto) {
        this.konto = konto;
    }

//    public List<HistoriaKonta> getHistorieKonta() {
//        return historieKonta;
//    }
//
//    public void setHistorieKonta(List<HistoriaKonta> historieKonta) {
//        this.historieKonta = historieKonta;
//    }
}
