package zarzadzanieFinansami.modele;

import jakarta.persistence.*;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;

import java.util.List;

@Entity
@Table(name = "kategoria")
public class Kategoria {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(nullable = false)
    private String nazwa;
    @Enumerated(EnumType.STRING)
    private TypTransakcjiEnum typTransakcji;

    //połaczenie transakcji
    @OneToMany(mappedBy = "kategoria", cascade = {CascadeType.DETACH,CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST}, orphanRemoval = false)
    private List<Transakcja> transakcje;
    //połaczzenie rejestrów
    @OneToMany(mappedBy = "kategoria", cascade = {CascadeType.DETACH,CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST}, orphanRemoval = false)
    private List<Rejestr> rejestry;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uzytkownik_id", nullable = false)
    private Uzytkownik uzytkownik;

    public Kategoria() {
    }

    public Kategoria(String nazwa, TypTransakcjiEnum typTransakcji, Uzytkownik uzytkownik) {
        this.nazwa = nazwa;
        this.typTransakcji = typTransakcji;
        this.uzytkownik = uzytkownik;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public TypTransakcjiEnum getTypTransakcji() {
        return typTransakcji;
    }

    public void setTypTransakcji(TypTransakcjiEnum typTransakcji) {
        this.typTransakcji = typTransakcji;
    }

    public List<Transakcja> getTransakcje() {
        return transakcje;
    }

    public void setTransakcje(List<Transakcja> transakcje) {
        this.transakcje = transakcje;
    }

    public List<Rejestr> getRejestry() {
        return rejestry;
    }

    public void setRejestry(List<Rejestr> rejestry) {
        this.rejestry = rejestry;
    }
    public Uzytkownik getUzytkownik() { // <-- GETTER DLA UŻYTKOWNIKA
        return uzytkownik;
    }

    public void setUzytkownik(Uzytkownik uzytkownik) { // <-- SETTER DLA UŻYTKOWNIKA
        this.uzytkownik = uzytkownik;
    }
}
