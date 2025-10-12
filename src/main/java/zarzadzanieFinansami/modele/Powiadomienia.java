package zarzadzanieFinansami.modele;

import jakarta.persistence.*;
import zarzadzanieFinansami.modele.enumeracje.TypPowiadomieniaEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "powiadomienia")
public class Powiadomienia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String wiadomosc;
    @Column(name = "wygenerowany_czas", nullable = false, updatable = false)
    private LocalDateTime wygenerowanyCzas;
    private boolean czyPrzeczytane = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uzytkownik_id")
    private Uzytkownik uzytkownik;

    @Enumerated(EnumType.STRING)
    @Column(name = "typ_powiadomienia", length = 50)
    private TypPowiadomieniaEnum typPowiadomienia;

    @PrePersist
    protected void onCreate() {
        this.wygenerowanyCzas = LocalDateTime.now();
    }

    public Powiadomienia(String wiadomosc, boolean czyPrzeczytane, Uzytkownik uzytkownik, TypPowiadomieniaEnum typPowiadomienia) {
        this.wiadomosc = wiadomosc;
        this.czyPrzeczytane = czyPrzeczytane;
        this.uzytkownik = uzytkownik;
        this.typPowiadomienia = (typPowiadomienia != null) ? typPowiadomienia : TypPowiadomieniaEnum.WIADOMOSC_SYSTEMOWA;
    }

    public Powiadomienia() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public TypPowiadomieniaEnum getTypPowiadomienia() {
        return typPowiadomienia;
    }

    public void setTypPowiadomienia(TypPowiadomieniaEnum typPowiadomienia) {
        this.typPowiadomienia = typPowiadomienia;
    }


}
