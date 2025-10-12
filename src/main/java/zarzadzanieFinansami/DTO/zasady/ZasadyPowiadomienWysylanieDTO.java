package zarzadzanieFinansami.DTO.zasady;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import zarzadzanieFinansami.modele.enumeracje.RegulaEnum;

import java.math.BigDecimal;

public class ZasadyPowiadomienWysylanieDTO {
    @NotNull(message = "Reguła nie może być pusta.")
    private RegulaEnum regula;

    @NotNull(message = "Wartość limitu nie może być pusta.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Wartość limitu musi być dodatnia.")
    private BigDecimal wartoscLimit;

    private boolean czyAktywna = true;

    @NotNull(message = "ID konta nie może być puste.")
    private Integer kontoId;

    private Integer celId; // Opcjonalne

    // Gettery i Settery

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