package zarzadzanieFinansami.DTO.cel;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ZasilenieCeluDTO {

    @NotNull(message = "Kwota nie może być pusta.")
    @DecimalMin(value = "0.01", message = "Kwota musi być większa niż 0.")
    private BigDecimal kwota;

    // Opcjonalne ID konta, z którego mają zostać pobrane środki.
    // Jeśli null, środki są dodawane "z zewnątrz".
    private Integer kontoZrodloweId;

    public BigDecimal getKwota() {
        return kwota;
    }

    public void setKwota(BigDecimal kwota) {
        this.kwota = kwota;
    }

    public Integer getKontoZrodloweId() {
        return kontoZrodloweId;
    }

    public void setKontoZrodloweId(Integer kontoZrodloweId) {
        this.kontoZrodloweId = kontoZrodloweId;
    }
}