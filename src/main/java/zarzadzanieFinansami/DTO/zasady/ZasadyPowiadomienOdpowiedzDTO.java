package zarzadzanieFinansami.DTO.zasady;

import zarzadzanieFinansami.modele.enumeracje.RegulaEnum;

import java.math.BigDecimal;

public class ZasadyPowiadomienOdpowiedzDTO {
    private Integer id;
    private RegulaEnum regula;
    private BigDecimal wartoscLimit;
    private boolean czyAktywna;
    private Integer kontoId;
    private Integer celId;

    public ZasadyPowiadomienOdpowiedzDTO(Integer id, RegulaEnum regula, BigDecimal wartoscLimit, boolean czyAktywna, Integer kontoId, Integer celId) {
        this.id = id;
        this.regula = regula;
        this.wartoscLimit = wartoscLimit;
        this.czyAktywna = czyAktywna;
        this.kontoId = kontoId;
        this.celId = celId;
    }

    // Gettery i Settery

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RegulaEnum getRegula() {
        return regula;
    }

    public void setRegula(RegulaEnum regula) {
        this.regula = regula;
    }

    public BigDecimal getWartoscLimit() {
        return wartoscLimit;
    }

    public void setWartoscLimit(BigDecimal wartoscLimit) {
        this.wartoscLimit = wartoscLimit;
    }

    public boolean isCzyAktywna() {
        return czyAktywna;
    }

    public void setCzyAktywna(boolean czyAktywna) {
        this.czyAktywna = czyAktywna;
    }

    public Integer getKontoId() {
        return kontoId;
    }

    public void setKontoId(Integer kontoId) {
        this.kontoId = kontoId;
    }

    public Integer getCelId() {
        return celId;
    }

    public void setCelId(Integer celId) {
        this.celId = celId;
    }
}