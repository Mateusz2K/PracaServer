// src/main/java/zarzadzanieFinansami/modele/Budzet.java
package zarzadzanieFinansami.modele;

import jakarta.persistence.*;
import zarzadzanieFinansami.modele.enumeracje.TypRegulyBudzetowejEnum;
import zarzadzanieFinansami.modele.enumeracje.OkresowoscEnum; // Zakładając, że chcesz użyć istniejącego enuma

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budzet")
public class Budzet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nazwa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uzytkownik_id", nullable = false)
    private Uzytkownik uzytkownik;

    @Column(name = "data_poczatkowa", nullable = false)
    private LocalDate dataPoczatkowa;

    @Column(name = "data_koncowa")
    private LocalDate dataKoncowa;

    @Enumerated(EnumType.STRING)
    @Column(name = "okresowosc") // Np. MIESIECZNY, ROCZNY - z OkresowoscEnum
    private OkresowoscEnum okresowosc;

    @Column(name = "przewidywany_dochod", precision = 15, scale = 2)
    private BigDecimal przewidywanyDochod;

    // Suma kwot alokowanych we wszystkich pozycjach budżetu
    @Column(name = "suma_alokowana", precision = 15, scale = 2)
    private BigDecimal sumaAlokowana;

    @OneToMany(mappedBy = "budzet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PozycjaBudzetu> pozycjeBudzetu = new ArrayList<>();

    @Column(name = "aktywny", nullable = false)
    private boolean aktywny = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "szablon_id") // Opcjonalne, jeśli budżet bazuje na szablonie
    private SzablonBudzetu opartyNaSzablonie;

    // --- Pola definiujące regułę budżetową ---

    // Określa, jaka metoda została użyta do pierwotnego podziału budżetu.
    @Enumerated(EnumType.STRING)
    @Column(name = "typ_reguly")
    private TypRegulyBudzetowejEnum typReguly;

    // Przechowuje wynikowy podział procentowy. Wypełniane bezpośrednio,
    // gdy typReguly to PROCENTOWA, lub obliczane, gdy typReguly to KWOTOWA.
    @Column(name = "procent_na_potrzeby")
    private Integer procentNaPotrzeby; // Przechowujemy jako Integer (np. 50 dla 50%)

    @Column(name = "procent_na_zachcianki")
    private Integer procentNaZachcianki;

    @Column(name = "procent_na_inwestycje")
    private Integer procentNaInwestycje;


    public Budzet() {
    }

    // Gettery i Settery
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public Uzytkownik getUzytkownik() {
        return uzytkownik;
    }

    public void setUzytkownik(Uzytkownik uzytkownik) {
        this.uzytkownik = uzytkownik;
    }

    public LocalDate getDataPoczatkowa() {
        return dataPoczatkowa;
    }

    public void setDataPoczatkowa(LocalDate dataPoczatkowa) {
        this.dataPoczatkowa = dataPoczatkowa;
    }

    public LocalDate getDataKoncowa() {
        return dataKoncowa;
    }

    public void setDataKoncowa(LocalDate dataKoncowa) {
        this.dataKoncowa = dataKoncowa;
    }

    public OkresowoscEnum getOkresowosc() {
        return okresowosc;
    }

    public void setOkresowosc(OkresowoscEnum okresowosc) {
        this.okresowosc = okresowosc;
    }

    public BigDecimal getPrzewidywanyDochod() {
        return przewidywanyDochod;
    }

    public void setPrzewidywanyDochod(BigDecimal przewidywanyDochod) {
        this.przewidywanyDochod = przewidywanyDochod;
    }

    public BigDecimal getSumaAlokowana() {
        return sumaAlokowana;
    }

    public void setSumaAlokowana(BigDecimal sumaAlokowana) {
        this.sumaAlokowana = sumaAlokowana;
    }

    public List<PozycjaBudzetu> getPozycjeBudzetu() {
        return pozycjeBudzetu;
    }

    public void setPozycjeBudzetu(List<PozycjaBudzetu> pozycjeBudzetu) {
        this.pozycjeBudzetu = pozycjeBudzetu;
        przeliczSumeAlokowana();
    }

    /**
     * Prywatna metoda pomocnicza do obliczania sumy alokowanych kwot.
     * Zapewnia, że pole sumaAlokowana jest zawsze spójne ze stanem listy pozycjeBudzetu.
     */
    private void przeliczSumeAlokowana() {
        this.sumaAlokowana = this.pozycjeBudzetu.stream()
                .map(PozycjaBudzetu::getKwotaAlokowana)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public void dodajPozycjeBudzetu(PozycjaBudzetu pozycja) {
        this.pozycjeBudzetu.add(pozycja);
        pozycja.setBudzet(this);
        przeliczSumeAlokowana();
    }

    public void usunPozycjeBudzetu(PozycjaBudzetu pozycja) {
        this.pozycjeBudzetu.remove(pozycja);
        pozycja.setBudzet(null);
        przeliczSumeAlokowana();
    }

    public boolean isAktywny() {
        return aktywny;
    }

    public void setAktywny(boolean aktywny) {
        this.aktywny = aktywny;
    }

    public SzablonBudzetu getOpartyNaSzablonie() {
        return opartyNaSzablonie;
    }

    public void setOpartyNaSzablonie(SzablonBudzetu opartyNaSzablonie) {
        this.opartyNaSzablonie = opartyNaSzablonie;
    }

    public Integer getProcentNaPotrzeby() {
        return procentNaPotrzeby;
    }

    public void setProcentNaPotrzeby(Integer procentNaPotrzeby) {
        this.procentNaPotrzeby = procentNaPotrzeby;
    }

    public Integer getProcentNaZachcianki() {
        return procentNaZachcianki;
    }

    public void setProcentNaZachcianki(Integer procentNaZachcianki) {
        this.procentNaZachcianki = procentNaZachcianki;
    }

    public Integer getProcentNaInwestycje() {
        return procentNaInwestycje;
    }

    public void setProcentNaInwestycje(Integer procentNaInwestycje) {
        this.procentNaInwestycje = procentNaInwestycje;
    }

    public TypRegulyBudzetowejEnum getTypReguly() {
        return typReguly;
    }

    public void setTypReguly(TypRegulyBudzetowejEnum typReguly) {
        this.typReguly = typReguly;
    }
}