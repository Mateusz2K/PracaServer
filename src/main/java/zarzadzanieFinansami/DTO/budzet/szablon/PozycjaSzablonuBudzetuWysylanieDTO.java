package zarzadzanieFinansami.DTO.budzet.szablon;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import zarzadzanieFinansami.modele.enumeracje.TypAlokacjiEnum;
import java.math.BigDecimal;

public class PozycjaSzablonuBudzetuWysylanieDTO {
    private Integer kategoriaId; // Opcjonalne, jeśli używamy metaKategorii

    @Size(max = 50)
    private String metaKategoriaNazwa; // Np. "POTRZEBY", "ZACHCIANKI"

    @NotNull(message = "Typ alokacji jest wymagany.")
    private TypAlokacjiEnum typAlokacji;

    private BigDecimal procentAlokowany;
    private BigDecimal kwotaAlokowana;

    // Gettery i Settery
    public Integer getKategoriaId() { return kategoriaId; }
    public void setKategoriaId(Integer kategoriaId) { this.kategoriaId = kategoriaId; }
    public String getMetaKategoriaNazwa() { return metaKategoriaNazwa; }
    public void setMetaKategoriaNazwa(String metaKategoriaNazwa) { this.metaKategoriaNazwa = metaKategoriaNazwa; }
    public TypAlokacjiEnum getTypAlokacji() { return typAlokacji; }
    public void setTypAlokacji(TypAlokacjiEnum typAlokacji) { this.typAlokacji = typAlokacji; }
    public BigDecimal getProcentAlokowany() { return procentAlokowany; }
    public void setProcentAlokowany(BigDecimal procentAlokowany) { this.procentAlokowany = procentAlokowany; }
    public BigDecimal getKwotaAlokowana() { return kwotaAlokowana; }
    public void setKwotaAlokowana(BigDecimal kwotaAlokowana) { this.kwotaAlokowana = kwotaAlokowana; }
}