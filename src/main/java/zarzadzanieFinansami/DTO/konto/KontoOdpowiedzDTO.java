package zarzadzanieFinansami.DTO.konto;//package zarzadzanieFinansami.DTO;
//
//
//import zarzadzanieFinansami.modele.*;
//import zarzadzanieFinansami.modele.enumeracje.TypKontaEnum;
//import zarzadzanieFinansami.modele.enumeracje.WalutaEnum;
//
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class KontoOdpowiedzDTO {
    private int id;
    private String nazwa;
    private BigDecimal bilans;
    private String typ; // String zamiast enuma dla prostoty w DTO
    private LocalDateTime dataUtworzenia;
    private Integer uzytkownikId; // Tylko ID właściciela

    // Konstruktor
    public KontoOdpowiedzDTO(int id, String nazwa, BigDecimal bilans, String typ, LocalDateTime dataUtworzenia, Integer uzytkownikId) {
        this.id = id;
        this.nazwa = nazwa;
        this.bilans = bilans;
        this.typ = typ;
        this.dataUtworzenia = dataUtworzenia;
        this.uzytkownikId = uzytkownikId;
    }

    // Gettery (Settery zazwyczaj nie są potrzebne dla DTO odpowiedzi)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }
    public BigDecimal getBilans() { return bilans; }
    public void setBilans(BigDecimal bilans) { this.bilans = bilans; }
    public String getTyp() { return typ; }
    public void setTyp(String typ) { this.typ = typ; }
    public LocalDateTime getDataUtworzenia() { return dataUtworzenia; }
    public void setDataUtworzenia(LocalDateTime dataUtworzenia) { this.dataUtworzenia = dataUtworzenia; }
    public Integer getUzytkownikId() { return uzytkownikId; }
    public void setUzytkownikId(Integer uzytkownikId) { this.uzytkownikId = uzytkownikId; }
}