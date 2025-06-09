// src/main/java/zarzadzanieFinansami/modele/PozycjaBudzetu.java
package zarzadzanieFinansami.modele;

import jakarta.persistence.*;
import zarzadzanieFinansami.modele.enumeracje.TypAlokacjiEnum;

import java.math.BigDecimal;

@Entity
@Table(name = "pozycja_budzetu")
public class PozycjaBudzetu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budzet_id", nullable = false)
    private Budzet budzet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kategoria_id", nullable = false)
    private Kategoria kategoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "typ_alokacji", nullable = false)
    private TypAlokacjiEnum typAlokacji;

    // Używane jeśli typAlokacji = PROCENTOWA (np. 10 dla 10%)
    @Column(name = "procent_alokowany")
    private BigDecimal procentAlokowany;

    // Używane jeśli typAlokacji = KWOTOWA, lub obliczone z procentu i dochodu budżetu
    @Column(name = "kwota_alokowana", precision = 15, scale = 2)
    private BigDecimal kwotaAlokowana;

    // Można dodać pole na rzeczywiste wydatki dla tej pozycji, aktualizowane na podstawie transakcji
    // @Transient // lub @Column jeśli chcesz persystować, ale wymagałoby to aktualizacji
    // private BigDecimal rzeczywisteWydatki;


    // Konstruktory, Gettery, Settery
    public PozycjaBudzetu() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Budzet getBudzet() {
        return budzet;
    }

    public void setBudzet(Budzet budzet) {
        this.budzet = budzet;
    }

    public Kategoria getKategoria() {
        return kategoria;
    }

    public void setKategoria(Kategoria kategoria) {
        this.kategoria = kategoria;
    }

    public TypAlokacjiEnum getTypAlokacji() {
        return typAlokacji;
    }

    public void setTypAlokacji(TypAlokacjiEnum typAlokacji) {
        this.typAlokacji = typAlokacji;
    }

    public BigDecimal getProcentAlokowany() {
        return procentAlokowany;
    }

    public void setProcentAlokowany(BigDecimal procentAlokowany) {
        this.procentAlokowany = procentAlokowany;
    }

    public BigDecimal getKwotaAlokowana() {
        return kwotaAlokowana;
    }

    public void setKwotaAlokowana(BigDecimal kwotaAlokowana) {
        this.kwotaAlokowana = kwotaAlokowana;
    }
}