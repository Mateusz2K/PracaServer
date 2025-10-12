package zarzadzanieFinansami.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class PowiadomienieOdpowiedzDTO {

    private Integer id;
    private String wiadomosc;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime wygenerowanyCzas;

    private boolean czyPrzeczytane;
    private String typPowiadomienia; // Pozostaje jako String, bo to tylko nazwa do wyświetlenia

    public PowiadomienieOdpowiedzDTO(Integer id, String wiadomosc, LocalDateTime wygenerowanyCzas, boolean czyPrzeczytane, String nazwaWyswietlanaTypu) {
        this.id = id;
        this.wiadomosc = wiadomosc;
        this.wygenerowanyCzas = wygenerowanyCzas;
        this.czyPrzeczytane = czyPrzeczytane;
        this.typPowiadomienia = nazwaWyswietlanaTypu;
    }

    // Gettery i Settery

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

    public String getTypPowiadomienia() {
        return typPowiadomienia;
    }

    public void setTypPowiadomienia(String typPowiadomienia) {
        this.typPowiadomienia = typPowiadomienia;
    }
}