package zarzadzanieFinansami.DTO.transakcja;

import com.fasterxml.jackson.annotation.JsonFormat;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransakcjaResponseDTO {
    private int id;
    private String opis;
    private BigDecimal kwota;
    private TypTransakcjiEnum typ;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate data;
    private Integer kategoriaId;
    private String kategoriaNazwa; // Dodatkowo nazwa kategorii dla wygody
    private Integer kontoId;

    // Konstruktor, Gettery, Settery
    public TransakcjaResponseDTO(int id, String opis, BigDecimal kwota, TypTransakcjiEnum typ, LocalDate data, Integer kategoriaId, String kategoriaNazwa, Integer kontoId) {
        this.id = id;
        this.opis = opis;
        this.kwota = kwota;
        this.typ = typ;
        this.data = data;
        this.kategoriaId = kategoriaId;
        this.kategoriaNazwa = kategoriaNazwa;
        this.kontoId = kontoId;
    }

    public TransakcjaResponseDTO() {} // Dla Jacksona

    // Gettery i Settery
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
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
    public String getKategoriaNazwa() { return kategoriaNazwa; }
    public void setKategoriaNazwa(String kategoriaNazwa) { this.kategoriaNazwa = kategoriaNazwa; }
    public Integer getKontoId() { return kontoId; }
    public void setKontoId(Integer kontoId) { this.kontoId = kontoId; }
}
