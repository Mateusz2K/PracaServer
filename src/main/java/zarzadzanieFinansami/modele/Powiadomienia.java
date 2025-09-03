package zarzadzanieFinansami.modele;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "powiadomienia")
public class Powiadomienia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String wiadomosc;
    @Column(name = "wygenerowany_czas", nullable = false, updatable = false)
    private LocalDateTime wygenerowanyCzas;
    private boolean czyPrzeczytane = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uzytkownik_id")
    private Uzytkownik uzytkownik;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "typ_powiadomienia_id")
    private TypPowiadomienia typPowiadomienia;

    @PrePersist
    protected void onCreate() {
        this.wygenerowanyCzas = LocalDateTime.now();
    }

    public Powiadomienia(String wiadomosc, boolean czyPrzeczytane, Uzytkownik uzytkownik, TypPowiadomienia typPowiadomienia) {
        this.wiadomosc = wiadomosc;
        this.czyPrzeczytane = czyPrzeczytane;
        this.uzytkownik = uzytkownik;
        this.typPowiadomienia = typPowiadomienia;
    }

    public Powiadomienia() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWiadomosc() {
        return wiadomosc;
    }

    public void setWiadomosc(String wiadomosc) {
        this.wiadomosc = wiadomosc;
    }

    public LocalDateTime getWygenerowanyCzas() {
        return wygenerowanyCzas;
    }

    public void setWygenerowanyCzas(LocalDateTime wygenerowanyCzas) {
        this.wygenerowanyCzas = wygenerowanyCzas;
    }

    public boolean isCzyPrzeczytane() {
        return czyPrzeczytane;
    }

    public void setCzyPrzeczytane(boolean czyPrzeczytane) {
        this.czyPrzeczytane = czyPrzeczytane;
    }

    public Uzytkownik getUzytkownik() {
        return uzytkownik;
    }

    public void setUzytkownik(Uzytkownik uzytkownik) {
        this.uzytkownik = uzytkownik;
    }

    public TypPowiadomienia getTypPowiadomienia() {
        return typPowiadomienia;
    }

    public void setTypPowiadomienia(TypPowiadomienia typPowiadomienia) {
        this.typPowiadomienia = typPowiadomienia;
    }


}
