package zarzadzanieFinansami.DTO.transakcja;

import com.fasterxml.jackson.annotation.JsonFormat;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;

import java.time.LocalDate;

public class TransakcjaPobieranieDTO {
    private Integer kategoriaId; // Opcjonalne ID kategorii
    private Integer kontoId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate dataOd;   // Opcjonalna data początkowa
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate dataDo;   // Opcjonalna data końcowa
    private TypTransakcjiEnum typTransakcji;
    // Gettery i Settery
    public Integer getKontoId() {
        return kontoId;
    }

    public void setKontoId(Integer kontoId) {
        this.kontoId = kontoId;
    }

    public LocalDate getDataOd() {
        return dataOd;
    }

    public void setDataOd(LocalDate dataOd) {
        this.dataOd = dataOd;
    }

    public LocalDate getDataDo() {
        return dataDo;
    }

    public void setDataDo(LocalDate dataDo) {
        this.dataDo = dataDo;
    }

    public Integer getKategoriaId() {
        return kategoriaId;
    }

    public void setKategoriaId(Integer kategoriaId) {
        this.kategoriaId = kategoriaId;
    }

    public TypTransakcjiEnum getTypTransakcji() {
        return typTransakcji;
    }

    public void setTypTransakcji(TypTransakcjiEnum typTransakcji) {
        this.typTransakcji = typTransakcji;
    }
}
