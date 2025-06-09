package zarzadzanieFinansami.DTO.transakcja;

// Możesz potrzebować importu dla @JsonFormat, jeśli chcesz wymusić konkretny format daty od klienta
// import com.fasterxml.jackson.annotation.JsonFormat;

public class TransakcjaPobieranieDTO {
    private Integer kategoriaId; // Opcjonalne ID kategorii
    private Integer kontoId; // Opcjonalne ID konta
    private String dataOd;   // Opcjonalna data początkowa ( "dd.mm.yyyy")
    private String dataDo;   // Opcjonalna data końcowa ("dd.mm.yyyy")

    // Gettery i Settery
    public Integer getKontoId() {
        return kontoId;
    }

    public void setKontoId(Integer kontoId) {
        this.kontoId = kontoId;
    }

    public String getDataOd() {
        return dataOd;
    }

    public void setDataOd(String dataOd) {
        this.dataOd = dataOd;
    }

    public String getDataDo() {
        return dataDo;
    }

    public void setDataDo(String dataDo) {
        this.dataDo = dataDo;
    }

    public Integer getKategoriaId() {
        return kategoriaId;
    }

    public void setKategoriaId(Integer kategoriaId) {
        this.kategoriaId = kategoriaId;
    }
}
