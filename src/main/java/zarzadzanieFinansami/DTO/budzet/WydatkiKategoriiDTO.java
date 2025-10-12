package zarzadzanieFinansami.DTO.budzet;

import java.math.BigDecimal;

public class WydatkiKategoriiDTO {
    private final Integer kategoriaId;
    private final BigDecimal sumaWydatkow;

    public WydatkiKategoriiDTO(Integer kategoriaId, BigDecimal sumaWydatkow) {
        this.kategoriaId = kategoriaId;
        // Jeśli suma jest null (bo nie było wydatków), zwróć ZERO
        this.sumaWydatkow = sumaWydatkow != null ? sumaWydatkow : BigDecimal.ZERO;
    }

    public Integer getKategoriaId() {
        return kategoriaId;
    }

    public BigDecimal getSumaWydatkow() {
        return sumaWydatkow;
    }
}