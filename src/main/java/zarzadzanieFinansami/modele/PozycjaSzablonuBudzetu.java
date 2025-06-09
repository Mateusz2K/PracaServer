// src/main/java/zarzadzanieFinansami/modele/PozycjaSzablonuBudzetu.java
package zarzadzanieFinansami.modele;

import jakarta.persistence.*;
import zarzadzanieFinansami.modele.enumeracje.TypAlokacjiEnum;
import java.math.BigDecimal;

@Entity
@Table(name = "pozycja_szablonu_budzetu")
public class PozycjaSzablonuBudzetu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "szablon_budzetu_id", nullable = false)
    private SzablonBudzetu szablonBudzetu;

    // Zamiast bezpośrednio do Kategoria, możemy tu dać nazwę kategorii,
    // aby szablon był bardziej generyczny, a użytkownik mapował swoje kategorie przy tworzeniu budżetu.
    // Lub, jeśli kategorie są globalne/użytkownika, można linkować bezpośrednio.
    // Dla uproszczenia na razie linkujemy do Kategoria.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kategoria_id") // Może być nullable, jeśli szablon definiuje tylko ogólne procenty
    private Kategoria kategoria;

    // Nazwa meta-kategorii dla reguły 50/30/20 np. "POTRZEBY", "ZACHCIANKI", "INWESTYCJE"
    // Można to też zrobić enumem.
    @Column(name = "meta_kategoria_nazwa", length = 50)
    private String metaKategoriaNazwa;


    @Enumerated(EnumType.STRING)
    @Column(name = "typ_alokacji", nullable = false)
    private TypAlokacjiEnum TypAlokacjiEnum;

    @Column(name = "procent_alokowany")
    private BigDecimal procentAlokowany; // Np. 10 dla 10%

    @Column(name = "kwota_alokowana", precision = 15, scale = 2)
    private BigDecimal kwotaAlokowana;


    // Konstruktory, Gettery, Settery
    public PozycjaSzablonuBudzetu() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SzablonBudzetu getSzablonBudzetu() {
        return szablonBudzetu;
    }

    public void setSzablonBudzetu(SzablonBudzetu SzablonBudzetu) {
        this.szablonBudzetu = SzablonBudzetu;
    }

    public Kategoria getKategoria() {
        return kategoria;
    }

    public void setKategoria(Kategoria kategoria) {
        this.kategoria = kategoria;
    }

    public String getMetaKategoriaNazwa() {
        return metaKategoriaNazwa;
    }

    public void setMetaKategoriaNazwa(String metaKategoriaNazwa) {
        this.metaKategoriaNazwa = metaKategoriaNazwa;
    }

    public TypAlokacjiEnum getTypAlokacjiEnum() {
        return TypAlokacjiEnum;
    }

    public void setTypAlokacjiEnum(TypAlokacjiEnum TypAlokacjiEnum) {
        this.TypAlokacjiEnum = TypAlokacjiEnum;
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