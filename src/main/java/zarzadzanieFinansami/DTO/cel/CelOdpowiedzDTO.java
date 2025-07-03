package zarzadzanieFinansami.DTO.cel;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CelOdpowiedzDTO {
    private Integer id;
    private String nazwaCelu;
    private BigDecimal kwotaDocelowa;
    private BigDecimal aktualnaKwota;
    @JsonFormat(shape = JsonFormat.Shape.STRING ,pattern = "dd.MM.yyyy")
    private LocalDate dataRozpoczecia;
    @JsonFormat(shape = JsonFormat.Shape.STRING ,pattern = "dd.MM.yyyy")
    private LocalDate dataZakonczenia;
    private String opis;
    private Integer uzytkownikId;
    private double procentOsiagniety;// Dodatkowe pola dla wygody

    public CelOdpowiedzDTO(Integer id, String nazwaCelu, BigDecimal kwotaDocelowa, BigDecimal aktualnaKwota,
                           LocalDate dataRozpoczecia, LocalDate dataZakonczenia, String opis, Integer uzytkownikId) {
        this.id = id;
        this.nazwaCelu = nazwaCelu;
        this.kwotaDocelowa = kwotaDocelowa;
        this.aktualnaKwota = aktualnaKwota;
        this.dataRozpoczecia = dataRozpoczecia;
        this.dataZakonczenia = dataZakonczenia;
        this.opis = opis;
        this.uzytkownikId = uzytkownikId;
        if (kwotaDocelowa != null && kwotaDocelowa.compareTo(BigDecimal.ZERO) > 0 && aktualnaKwota != null) {
            this.procentOsiagniety = aktualnaKwota.multiply(BigDecimal.valueOf(100)).divide(kwotaDocelowa, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        } else {
            this.procentOsiagniety = 0.0;
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public BigDecimal getAktualnaKwota() {
        return aktualnaKwota;
    }

    public void setAktualnaKwota(BigDecimal aktualnaKwota) {
        this.aktualnaKwota = aktualnaKwota;
    }

    public LocalDate getDataRozpoczecia() {
        return dataRozpoczecia;
    }

    public void setDataRozpoczecia(LocalDate dataRozpoczecia) {
        this.dataRozpoczecia = dataRozpoczecia;
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

    public Integer getUzytkownikId() {
        return uzytkownikId;
    }

    public void setUzytkownikId(Integer uzytkownikId) {
        this.uzytkownikId = uzytkownikId;
    }

    public double getProcentOsiagniety() {
        return procentOsiagniety;
    }

    public void setProcentOsiagniety(double procentOsiagniety) {
        this.procentOsiagniety = procentOsiagniety;
    }
}
