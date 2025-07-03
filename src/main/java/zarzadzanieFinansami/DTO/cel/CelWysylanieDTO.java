// src/main/java/zarzadzanieFinansami/DTO/cel/CelRequestDTO.java
package zarzadzanieFinansami.DTO.cel;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CelWysylanieDTO {

    @NotBlank(message = "Nazwa celu nie może być pusta.")
    @Size(max = 100, message = "Nazwa celu może mieć maksymalnie 100 znaków.")
    private String nazwaCelu;

    @NotNull(message = "Kwota docelowa nie może być pusta.")
    @DecimalMin(value = "0.01", message = "Kwota docelowa musi być większa niż 0.")
    private BigDecimal kwotaDocelowa;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate dataZakonczenia; // Może być opcjonalna

    @Size(max = 500, message = "Opis może mieć maksymalnie 500 znaków.")
    private String opis;

    // Gettery i Settery
    public String getNazwaCelu() {
        return nazwaCelu;
    }

    public void setNazwaCelu(String nazwaCelu) {
        this.nazwaCelu = nazwaCelu;
    }

    public BigDecimal getKwotaDocelowa() {
        return kwotaDocelowa;
    }

    public void setKwotaDocelowa(BigDecimal kwotaDocelowa) {
        this.kwotaDocelowa = kwotaDocelowa;
    }

    public LocalDate getDataZakonczenia() {
        return dataZakonczenia;
    }

    public void setDataZakonczenia(LocalDate dataZakonczenia) {
        this.dataZakonczenia = dataZakonczenia;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }
}
