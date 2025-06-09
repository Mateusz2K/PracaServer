package zarzadzanieFinansami.DTO.budzet;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import zarzadzanieFinansami.modele.enumeracje.TypAlokacjiEnum;
import java.math.BigDecimal;

public class PozycjaBudzetuRequestDTO {
    @NotNull(message = "ID kategorii jest wymagane.")
    private Integer kategoriaId;

    @NotNull(message = "Typ alokacji jest wymagany.")
    private TypAlokacjiEnum typAlokacji;

    @DecimalMin(value = "0.0", message = "Procent alokowany musi być nieujemny.")
    private BigDecimal procentAlokowany; // Np. 10 dla 10%, jeśli typAlokacji = PROCENTOWA

    @DecimalMin(value = "0.00", message = "Kwota alokowana musi być nieujemna.")
    private BigDecimal kwotaAlokowana;   // Jeśli typAlokacji = KWOTOWA

    // Gettery i Settery
    public Integer getKategoriaId() { return kategoriaId; }
    public void setKategoriaId(Integer kategoriaId) { this.kategoriaId = kategoriaId; }
    public TypAlokacjiEnum getTypAlokacji() { return typAlokacji; }
    public void setTypAlokacji(TypAlokacjiEnum typAlokacji) { this.typAlokacji = typAlokacji; }
    public BigDecimal getProcentAlokowany() { return procentAlokowany; }
    public void setProcentAlokowany(BigDecimal procentAlokowany) { this.procentAlokowany = procentAlokowany; }
    public BigDecimal getKwotaAlokowana() { return kwotaAlokowana; }
    public void setKwotaAlokowana(BigDecimal kwotaAlokowana) { this.kwotaAlokowana = kwotaAlokowana; }
}