package zarzadzanieFinansami.modele;

import jakarta.persistence.*;
import zarzadzanieFinansami.modele.enumeracje.RegulaEnum;

@Entity
@Table(name = "zasady_powiadomien")
public class ZasadyPowiadomien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Enumerated(EnumType.STRING)
    private RegulaEnum regula;
    private double wartoscLimit;
    private boolean czyAktywna;

    @JoinColumn(name = "uzytkownik_id", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    private Uzytkownik uzytkownik;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "konto_id", nullable = false)
    private Konto konto;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cel_id")
    private Cel cel;

    public ZasadyPowiadomien(RegulaEnum regula, double wartoscLimit, boolean czyAktywna, Uzytkownik uzytkownik, Konto konto, Cel cel) {
        this.regula = regula;
        this.wartoscLimit = wartoscLimit;
        this.czyAktywna = czyAktywna;
        this.uzytkownik = uzytkownik;
        this.konto = konto;
        this.cel = cel;
    }

    public ZasadyPowiadomien() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public RegulaEnum getRegula() {
        return regula;
    }

    public void setRegula(RegulaEnum regula) {
        this.regula = regula;
    }

    public double getWartoscLimit() {
        return wartoscLimit;
    }

    public void setWartoscLimit(double wartoscLimit) {
        this.wartoscLimit = wartoscLimit;
    }

    public boolean isCzyAktywna() {
        return czyAktywna;
    }

    public void setCzyAktywna(boolean czyAktywna) {
        this.czyAktywna = czyAktywna;
    }

    public Uzytkownik getUzytkownik() {
        return uzytkownik;
    }

    public void setUzytkownik(Uzytkownik uzytkownik) {
        this.uzytkownik = uzytkownik;
    }

    public Konto getKonto() {
        return konto;
    }

    public void setKonto(Konto konto) {
        this.konto = konto;
    }

    public Cel getCel() {
        return cel;
    }

    public void setCel(Cel cel) {
        this.cel = cel;
    }
}
