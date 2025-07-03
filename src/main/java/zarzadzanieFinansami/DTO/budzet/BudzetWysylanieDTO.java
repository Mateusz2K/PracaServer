package zarzadzanieFinansami.DTO.budzet;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import zarzadzanieFinansami.modele.enumeracje.OkresowoscEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BudzetWysylanieDTO {
    @NotBlank(message = "Nazwa budżetu nie może być pusta.")
    @Size(max = 100)
    private String nazwa;

    @NotNull(message = "Data początkowa jest wymagana.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate dataPoczatkowa;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate dataKoncowa; // Może być null, jeśli okresowość jest np. MIESIECZNY

    private OkresowoscEnum okresowosc;

    @NotNull(message = "Przewidywany dochód jest wymagany.")
    @DecimalMin(value = "0.00", message = "Przewidywany dochód musi być nieujemny.")
    private BigDecimal przewidywanyDochod;

    @Valid
    private List<PozycjaBudzetuWysylanieDTO> pozycjeBudzetu;

    private Long szablonId; // Opcjonalne ID szablonu, na którym ma bazować budżet

    // Dla reguły 50/30/20 - użytkownik może je ustawić
    private Integer procentNaPotrzeby;
    private Integer procentNaZachcianki;
    private Integer procentNaInwestycje;
    private boolean zastosujReguleProcentowa; // Flaga czy użyć tych procentów

    // Gettery i Settery
    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }
    public LocalDate getDataPoczatkowa() { return dataPoczatkowa; }
    public void setDataPoczatkowa(LocalDate dataPoczatkowa) { this.dataPoczatkowa = dataPoczatkowa; }
    public LocalDate getDataKoncowa() { return dataKoncowa; }
    public void setDataKoncowa(LocalDate dataKoncowa) { this.dataKoncowa = dataKoncowa; }
    public OkresowoscEnum getOkresowosc() { return okresowosc; }
    public void setOkresowosc(OkresowoscEnum okresowosc) { this.okresowosc = okresowosc; }
    public BigDecimal getPrzewidywanyDochod() { return przewidywanyDochod; }
    public void setPrzewidywanyDochod(BigDecimal przewidywanyDochod) { this.przewidywanyDochod = przewidywanyDochod; }
    public List<PozycjaBudzetuWysylanieDTO> getPozycjeBudzetu() { return pozycjeBudzetu; }
    public void setPozycjeBudzetu(List<PozycjaBudzetuWysylanieDTO> pozycjeBudzetu) { this.pozycjeBudzetu = pozycjeBudzetu; }
    public Long getSzablonId() { return szablonId; }
    public void setSzablonId(Long szablonId) { this.szablonId = szablonId; }
    public Integer getProcentNaPotrzeby() { return procentNaPotrzeby; }
    public void setProcentNaPotrzeby(Integer procentNaPotrzeby) { this.procentNaPotrzeby = procentNaPotrzeby; }
    public Integer getProcentNaZachcianki() { return procentNaZachcianki; }
    public void setProcentNaZachcianki(Integer procentNaZachcianki) { this.procentNaZachcianki = procentNaZachcianki; }
    public Integer getProcentNaInwestycje() { return procentNaInwestycje; }
    public void setProcentNaInwestycje(Integer procentNaInwestycje) { this.procentNaInwestycje = procentNaInwestycje; }
    public boolean isZastosujReguleProcentowa() { return zastosujReguleProcentowa; }
    public void setZastosujReguleProcentowa(boolean zastosujReguleProcentowa) { this.zastosujReguleProcentowa = zastosujReguleProcentowa; }
}
