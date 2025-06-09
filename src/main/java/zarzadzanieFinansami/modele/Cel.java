// src/main/java/zarzadzanieFinansami/modele/Cel.java
package zarzadzanieFinansami.modele;

import jakarta.persistence.*;
import zarzadzanieFinansami.modele.enumeracje.CelStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate; // Zmieniono z LocalDateTime na LocalDate dla daty rozpoczęcia i zakończenia
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cel")
public class Cel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Sugeruję GenerationType.IDENTITY dla autoinkrementacji przez bazę
    @Column(name = "id", nullable = false)
    private Integer id; // Zmieniono na Long, co jest częstszą praktyką dla ID

    @Column(name = "nazwa_celu", nullable = false, length = 100) // Dodano length, warto ujednolicić nazewnictwo
    private String nazwaCelu; // Zmieniono z "nazwa" dla spójności z DTO

    @Column(name = "kwota_docelowa", nullable = false, precision = 15, scale = 2)
    private BigDecimal kwotaDocelowa; // Zmieniono z "okreslonaKwota"

    @Column(name = "aktualna_kwota", precision = 15, scale = 2)
    private BigDecimal aktualnaKwota = BigDecimal.ZERO; // Zmieniono z "zebranaKwota" i zainicjowano

    @Column(name = "data_rozpoczecia", nullable = false)
    private LocalDate dataRozpoczecia; // NOWE POLE

    @Column(name = "data_zakonczenia") // Było "termin" typu LocalDateTime, zmieniono na LocalDate
    private LocalDate dataZakonczenia;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CelStatusEnum status = CelStatusEnum.AKTYWNY;

    @Column(name = "opis", length = 500) // Dodano length
    private String opis; // NOWE POLE

    @ManyToOne(fetch = FetchType.LAZY) // Zmieniono na LAZY, EAGER było przy koncie
    @JoinColumn(name = "konto_id", nullable = false)
    private Konto konto;

    @ManyToOne(fetch = FetchType.LAZY) // Dodano relację do użytkownika
    @JoinColumn(name = "uzytkownik_id", nullable = false)
    private Uzytkownik uzytkownik;


    // Relacje (pozostawiam jak były, ale warto przemyśleć CascadeTypes)
    @OneToMany(mappedBy = "cel", cascade = {CascadeType.DETACH,CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST}, orphanRemoval = false)
    private List<RaportOszczednosci> raportyOszczednosci = new ArrayList<>();

    @OneToMany(mappedBy = "cel", cascade = {CascadeType.DETACH,CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST}, orphanRemoval = false)
    private List<ZasadyOszczedzania> zasadyOszczedzania = new ArrayList<>();

    @OneToMany(mappedBy = "cel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ZasadyPowiadomien> zasadyPowiadomien = new ArrayList<>();

    // Dodajemy pustą listę dla transakcji, jeśli chcemy je tu mapować
    // @OneToMany(mappedBy = "cel", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Transakcja> transakcje = new ArrayList<>();


    // Konstruktor domyślny
    public Cel() {
    }

    // Gettery i Settery (dla nowych i zmodyfikowanych pól)

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNazwaCelu() {
        return nazwaCelu;
    }

    public void setNazwaCelu(String nazwaCelu) {
        this.nazwaCelu = nazwaCelu;
    }

    public BigDecimal getKwotaDocelowa() {
        return kwotaDocelowa;
    }

    public void setKwotaDocelowa(BigDecimal kwotaDocelowa) {
        this.kwotaDocelowa = kwotaDocelowa;
    }

    public BigDecimal getAktualnaKwota() {
        return aktualnaKwota;
    }

    public void setAktualnaKwota(BigDecimal aktualnaKwota) {
        this.aktualnaKwota = aktualnaKwota;
    }

    public LocalDate getDataRozpoczecia() {
        return dataRozpoczecia;
    }

    public void setDataRozpoczecia(LocalDate dataRozpoczecia) {
        this.dataRozpoczecia = dataRozpoczecia;
    }

    public LocalDate getDataZakonczenia() {
        return dataZakonczenia;
    }

    public void setDataZakonczenia(LocalDate dataZakonczenia) {
        this.dataZakonczenia = dataZakonczenia;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public Konto getKonto() {
        return konto;
    }

    public void setKonto(Konto konto) {
        this.konto = konto;
    }

    public Uzytkownik getUzytkownik() {
        return uzytkownik;
    }

    public void setUzytkownik(Uzytkownik uzytkownik) {
        this.uzytkownik = uzytkownik;
    }
    public CelStatusEnum getStatus() {
        return status;
    }
    public void setStatus(CelStatusEnum status) {
        this.status = status;
    }
    public


     List<RaportOszczednosci> getRaportyOszczednosci() {
        return raportyOszczednosci;
    }

    public void setRaportyOszczednosci(List<RaportOszczednosci> raportyOszczednosci) {
        this.raportyOszczednosci = raportyOszczednosci;
    }

    public List<ZasadyOszczedzania> getZasadyOszczedzania() {
        return zasadyOszczedzania;
    }

    public void setZasadyOszczedzania(List<ZasadyOszczedzania> zasadyOszczedzania) {
        this.zasadyOszczedzania = zasadyOszczedzania;
    }

    public List<ZasadyPowiadomien> getZasadyPowiadomien() {
        return zasadyPowiadomien;
    }

    public void setZasadyPowiadomien(List<ZasadyPowiadomien> zasadyPowiadomien) {
        this.zasadyPowiadomien = zasadyPowiadomien;
    }
}