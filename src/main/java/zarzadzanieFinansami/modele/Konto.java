package zarzadzanieFinansami.modele;

import jakarta.persistence.*;
import zarzadzanieFinansami.konwertery.TypKontaConverter;
import zarzadzanieFinansami.modele.enumeracje.TypKontaEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;



@Entity
@Table(name = "konto")
public class Konto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nazwa;
    @Column(precision = 15, scale = 2, nullable = false, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal bilans;
    @Convert(converter = TypKontaConverter.class)
    @Column(nullable = false, name = "typ")
//    @Enumerated(EnumType.STRING) // Przechowywanie wartości jako tekst w bazie
    private TypKontaEnum typ;

    @Column(updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime dataUtworzenia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uzytkownik_id", nullable = false)
    private Uzytkownik uzytkownik;

    //cele
    @OneToMany(mappedBy = "konto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cel> cele;
    //@XzasadyOczcdzędzania
    @OneToMany(mappedBy = "odbiorca", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ZasadyOszczedzania> zasadyOszczedzaniaOdbiorca;
    @OneToMany(mappedBy = "nadawca", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ZasadyOszczedzania> zasadyOszczedzaniaNadawca;
    //zasadyPowiadomien
    @OneToMany(mappedBy = "konto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ZasadyPowiadomien> zasadyPowiadomien;
    //transakcja
    @OneToMany(mappedBy = "konto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transakcja> transakcje;
    //historia Konta
    @OneToMany(mappedBy = "konto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoriaKonta> historieKont;



    public Konto(String nazwa, BigDecimal bilans, TypKontaEnum typ, LocalDateTime dataUtworzenia, Uzytkownik uzytkownik) {
        this.nazwa = nazwa;
        this.bilans = bilans;
        this.typ = typ;
        this.dataUtworzenia = dataUtworzenia;
        this.uzytkownik = uzytkownik;
    }

    public Konto() {
    }
    @PrePersist
    protected void onCreate() {
        this.dataUtworzenia = LocalDateTime.now();
        if(this.bilans == null) {
            this.bilans = BigDecimal.ZERO;
        }
    }

    // Gettery i Settery
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Uzytkownik getUzytkownik() {
        return uzytkownik;
    }

    public void setUzytkownik(Uzytkownik uzytkownik) {
        this.uzytkownik = uzytkownik;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public BigDecimal getBilans() {
        return bilans;
    }

    public void setBilans(BigDecimal bilans) {
        this.bilans = bilans;
    }

    public TypKontaEnum getTyp() {
        return typ;
    }

    public void setTyp(TypKontaEnum typ) {
        this.typ = typ;
    }

    public LocalDateTime getDataUtworzenia() {
        return dataUtworzenia;
    }


    public List<Cel> getCele() {
        return cele;
    }

    public void setCele(List<Cel> cele) {
        this.cele = cele;
    }

    public List<ZasadyOszczedzania> getZasadyOszczedzaniaOdbiorca() {
        return zasadyOszczedzaniaOdbiorca;
    }

    public void setZasadyOszczedzaniaOdbiorca(List<ZasadyOszczedzania> zasadyOszczedzaniaOdbiorca) {
        this.zasadyOszczedzaniaOdbiorca = zasadyOszczedzaniaOdbiorca;
    }

    public List<ZasadyOszczedzania> getZasadyOszczedzaniaNadawca() {
        return zasadyOszczedzaniaNadawca;
    }

    public void setZasadyOszczedzaniaNadawca(List<ZasadyOszczedzania> zasadyOszczedzaniaNadawca) {
        this.zasadyOszczedzaniaNadawca = zasadyOszczedzaniaNadawca;
    }

    public List<ZasadyPowiadomien> getZasadyPowiadomien() {
        return zasadyPowiadomien;
    }

    public void setZasadyPowiadomien(List<ZasadyPowiadomien> zasadyPowiadomien) {
        this.zasadyPowiadomien = zasadyPowiadomien;
    }


    public List<Transakcja> getTransakcje() {
        return transakcje;
    }

    public void setTransakcje(List<Transakcja> transakcje) {
        this.transakcje = transakcje;
    }

    public List<HistoriaKonta> getHistorieKont() {
        return historieKont;
    }

    public void setHistorieKont(List<HistoriaKonta> historieKont) {
        this.historieKont = historieKont;
    }

    @Override
    public String toString() {
        return "Konto{" +
                "id=" + id +
                ", nazwa='" + nazwa + '\'' +
                ", bilans=" + bilans +
                ", typ=" + (typ != null ? typ.name() : "null") + // Bezpieczne dla null
                ", dataUtworzenia=" + dataUtworzenia +
                ", uzytkownikId=" + (uzytkownik != null ? uzytkownik.getId() : "null") + // Loguj ID użytkownika, aby uniknąć problemów z toString() użytkownika
                // Możesz dodać więcej pól, jeśli potrzebujesz, np. liczbę transakcji
                // ", liczbaTransakcji=" + (transakcje != null ? transakcje.size() : 0) +
                '}';
    }
}
