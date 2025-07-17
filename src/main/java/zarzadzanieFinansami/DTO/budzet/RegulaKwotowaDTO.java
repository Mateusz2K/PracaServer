package zarzadzanieFinansami.DTO.budzet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RegulaKwotowaDTO {

    @NotNull(message = "Kwota na potrzeby jest wymagana dla reguły kwotowej.")
    @DecimalMin(value = "0.0", message = "Kwota musi być nieujemna.")
    private BigDecimal kwotaNaPotrzeby;

    @NotNull(message = "Kwota na zachcianki jest wymagana dla reguły kwotowej.")
    @DecimalMin(value = "0.0", message = "Kwota musi być nieujemna.")
    private BigDecimal kwotaNaZachcianki;

    @NotNull(message = "Kwota na inwestycje jest wymagana dla reguły kwotowej.")
    @DecimalMin(value = "0.0", message = "Kwota musi być nieujemna.")
    private BigDecimal kwotaNaInwestycje;


    // Gettery i Settery
    public BigDecimal getKwotaNaPotrzeby() { return kwotaNaPotrzeby; }
    public void setKwotaNaPotrzeby(BigDecimal kwotaNaPotrzeby) { this.kwotaNaPotrzeby = kwotaNaPotrzeby; }
    public BigDecimal getKwotaNaZachcianki() { return kwotaNaZachcianki; }
    public void setKwotaNaZachcianki(BigDecimal kwotaNaZachcianki) { this.kwotaNaZachcianki = kwotaNaZachcianki; }
    public BigDecimal getKwotaNaInwestycje() { return kwotaNaInwestycje; }
    public void setKwotaNaInwestycje(BigDecimal kwotaNaInwestycje) { this.kwotaNaInwestycje = kwotaNaInwestycje; }
}