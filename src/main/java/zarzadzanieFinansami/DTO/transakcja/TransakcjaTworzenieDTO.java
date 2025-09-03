package zarzadzanieFinansami.DTO.transakcja;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransakcjaTworzenieDTO {
    @NotBlank(message = "Opis transakcji nie może być pusty.")
    private String opis;

    @NotNull(message = "Kwota transakcji nie może być pusta.")
    @DecimalMin(value = "0.01", message = "Kwota transakcji musi być większa niż 0.")
    private BigDecimal kwota;

    @NotNull(message = "Typ transakcji (KOSZT/PRZYCHÓD) jest wymagany.")
    private TypTransakcjiEnum typ; // Klient prześle "KOSZT" lub "PRZYCHÓD"

    @NotNull(message = "Data transakcji jest wymagana.")
    @PastOrPresent(message = "Data transakcji nie może być z przyszłości.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate data;

    @NotNull(message = "ID kategorii jest wymagane.")
    private Integer kategoriaId;

    @NotNull(message = "ID konta jest wymagane.")//konto jest pozykiwane ze ścieżki HTTP
    private Integer kontoId;

    // Gettery i Settery
    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }
    public BigDecimal getKwota() { return kwota; }
    public void setKwota(BigDecimal kwota) { this.kwota = kwota; }
    public TypTransakcjiEnum getTyp() { return typ; }
    public void setTyp(TypTransakcjiEnum typ) { this.typ = typ; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public Integer getKategoriaId() { return kategoriaId; }
    public void setKategoriaId(Integer kategoriaId) { this.kategoriaId = kategoriaId; }

    public Integer getKontoId() {
        return kontoId;
    }

    public void setKontoId(Integer konto) {
        this.kontoId = konto;
    }
}
