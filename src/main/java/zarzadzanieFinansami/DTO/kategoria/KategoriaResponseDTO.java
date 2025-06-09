package zarzadzanieFinansami.DTO.kategoria;

import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;

public class KategoriaResponseDTO {
    private Integer id;
    private String nazwa;
    private TypTransakcjiEnum typTransakcji;

    public KategoriaResponseDTO(Integer id, String nazwa, TypTransakcjiEnum typTransakcji) {
        this.id = id;
        this.nazwa = nazwa;
        this.typTransakcji = typTransakcji;
    }

    public KategoriaResponseDTO() { // Dla Jacksona
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
}

