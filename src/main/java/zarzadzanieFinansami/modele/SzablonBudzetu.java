// src/main/java/zarzadzanieFinansami/modele/SzablonBudzetu.java
package zarzadzanieFinansami.modele;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "szablon_budzetu")
public class SzablonBudzetu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100) // Nazwa szablonu powinna być unikalna
    private String nazwa;

    @Column(length = 500)
    private String opis;

    // Jeśli null, to szablon systemowy. W przeciwnym razie szablon użytkownika.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uzytkownik_id")
    private Uzytkownik uzytkownik; // Twórca szablonu

    @Column(name = "czy_publiczny") // Czy szablon systemowy/użytkownika jest widoczny dla innych
    private boolean czyPubliczny = false;

    @OneToMany(mappedBy = "szablonBudzetu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PozycjaSzablonuBudzetu> pozycjeSzablonu = new ArrayList<>();

    // Pola dla reguły procentowej (np. 50/30/20) jako domyślne dla szablonu
    @Column(name = "procent_na_potrzeby")
    private Integer procentNaPotrzeby;

    @Column(name = "procent_na_zachcianki")
    private Integer procentNaZachcianki;

    @Column(name = "procent_na_inwestycje")
    private Integer procentNaInwestycje;

    // Konstruktory, Gettery, Settery
    public SzablonBudzetu() {
    }

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

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public Uzytkownik getUzytkownik() {
        return uzytkownik;
    }

    public void setUzytkownik(Uzytkownik uzytkownik) {
        this.uzytkownik = uzytkownik;
    }

    public boolean isCzyPubliczny() {
        return czyPubliczny;
    }

    public void setCzyPubliczny(boolean czyPubliczny) {
        this.czyPubliczny = czyPubliczny;
    }

    public List<PozycjaSzablonuBudzetu> getPozycjeSzablonu() {
        return pozycjeSzablonu;
    }

    public void setPozycjeSzablonu(List<PozycjaSzablonuBudzetu> pozycjeSzablonu) {
        this.pozycjeSzablonu = pozycjeSzablonu;
    }

    public void dodajPozycjeSzablonu(PozycjaSzablonuBudzetu pozycja) {
        this.pozycjeSzablonu.add(pozycja);
        pozycja.setSzablonBudzetu(this);
    }

    public void usunPozycjeSzablonu(PozycjaSzablonuBudzetu pozycja) {
        this.pozycjeSzablonu.remove(pozycja);
        pozycja.setSzablonBudzetu(null);
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
}