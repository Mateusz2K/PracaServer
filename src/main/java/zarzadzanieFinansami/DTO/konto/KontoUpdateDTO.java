package zarzadzanieFinansami.DTO.konto;

import java.math.BigDecimal;

// Możesz dodać walidacje, np. @Size dla nazwy
public class KontoUpdateDTO {
    private String nazwa;
    private String typ;
    private BigDecimal bilans;
    // private List<CelUpdateDTO> cele; // Jeśli chcesz aktualizować cele

    // Gettery i Settery
    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }

    public String getTypKonta() {
        return typ;
    }

    public void setTypKonta(String typ) {
        this.typ = typ;
    }

    public BigDecimal getBilans() {
        return bilans;
    }

    public void setBilans(BigDecimal bilans) {
        this.bilans = bilans;
    }
    // public List<CelUpdateDTO> getCele() { return cele; }
    // public void setCele(List<CelUpdateDTO> cele) { this.cele = cele; }
}
