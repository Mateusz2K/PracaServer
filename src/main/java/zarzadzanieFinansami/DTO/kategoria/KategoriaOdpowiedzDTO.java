package zarzadzanieFinansami.DTO.kategoria;

import zarzadzanieFinansami.modele.enumeracje.KategorieBudzetEnum;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;

public class KategoriaOdpowiedzDTO {
    private Integer id;
    private String nazwa;
    private TypTransakcjiEnum typTransakcji;
    private KategorieBudzetEnum kategorieBudzetuEnum;

    public KategoriaOdpowiedzDTO(Integer id, String nazwa, TypTransakcjiEnum typTransakcji, KategorieBudzetEnum kategorieBudzetuEnum) {
        this.id = id;
        this.nazwa = nazwa;
        this.typTransakcji = typTransakcji;
        this.kategorieBudzetuEnum = kategorieBudzetuEnum;
    }

    public KategoriaOdpowiedzDTO() { // Dla Jacksona
    }

    // Gettery i Settery
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public KategorieBudzetEnum getKategorieBudzetuEnum() {
        return kategorieBudzetuEnum;
    }

    public void setKategorieBudzetuEnum(KategorieBudzetEnum kategorieBudzetuEnum) {
        this.kategorieBudzetuEnum = kategorieBudzetuEnum;
    }
}

