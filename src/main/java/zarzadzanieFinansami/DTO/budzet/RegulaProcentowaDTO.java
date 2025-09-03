package zarzadzanieFinansami.DTO.budzet;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class RegulaProcentowaDTO {
    @Min(value = 0, message = "Procenty muszą być nieujemne")
    @Max(value = 100, message = "Procenty muszą być mniejsze lub równe 100")
    private Integer procentNaPotrzeby;
    @Min(value = 0, message = "Procenty muszą być nieujemne")
    @Max(value = 100, message = "Procenty muszą być mniejsze lub równe 100")
    private Integer procentNaZachcianki;
    @Min(value = 0, message = "Procenty muszą być nieujemne")
    @Max(value = 100, message = "Procenty muszą być mniejsze lub równe 100")
    private Integer procentNaInwestycje;
    private boolean zastosuj;

    @AssertTrue(message = "Suma procentów na potrzeby, zachcianki i inwestycje musi wynosić 100, jeśli reguła jest stosowana.")
    private boolean isSumaProcentowPrawidlowa() {
        if (!zastosuj) return true; // Jeśli reguła nie jest stosowana, walidacja jest zbędna.
        if (procentNaPotrzeby == null || procentNaZachcianki == null || procentNaInwestycje == null) return false;
        return (procentNaPotrzeby + procentNaZachcianki + procentNaInwestycje) == 100;
    }

    public Integer getProcentNaPotrzeby() { return procentNaPotrzeby; }
    public void setProcentNaPotrzeby(Integer procentNaPotrzeby) { this.procentNaPotrzeby = procentNaPotrzeby; }
    public Integer getProcentNaZachcianki() { return procentNaZachcianki; }
    public void setProcentNaZachcianki(Integer procentNaZachcianki) { this.procentNaZachcianki = procentNaZachcianki; }
    public Integer getProcentNaInwestycje() { return procentNaInwestycje; }
    public void setProcentNaInwestycje(Integer procentNaInwestycje) { this.procentNaInwestycje = procentNaInwestycje; }
    public boolean isZastosuj() { return zastosuj; }
    public void setZastosuj(boolean zastosuj) { this.zastosuj = zastosuj; }

}
