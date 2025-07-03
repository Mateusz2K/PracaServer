package zarzadzanieFinansami.DTO.budzet.szablon;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class SzablonBudzetuWysylanieDTO {
    @NotBlank(message = "Nazwa szablonu nie może być pusta.")
    @Size(max = 100)
    private String nazwa;

    @Size(max = 500)
    private String opis;

    @Valid
    private List<PozycjaSzablonuBudzetuWysylanieDTO> pozycjeSzablonu;

    // Dla reguły 50/30/20 jako domyślne
    private Integer procentNaPotrzeby;
    private Integer procentNaZachcianki;
    private Integer procentNaInwestycje;

    // Gettery i Settery
    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }
    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }
    public List<PozycjaSzablonuBudzetuWysylanieDTO> getPozycjeSzablonu() { return pozycjeSzablonu; }
    public void setPozycjeSzablonu(List<PozycjaSzablonuBudzetuWysylanieDTO> pozycjeSzablonu) { this.pozycjeSzablonu = pozycjeSzablonu; }
    public Integer getProcentNaPotrzeby() { return procentNaPotrzeby; }
    public void setProcentNaPotrzeby(Integer procentNaPotrzeby) { this.procentNaPotrzeby = procentNaPotrzeby; }
    public Integer getProcentNaZachcianki() { return procentNaZachcianki; }
    public void setProcentNaZachcianki(Integer procentNaZachcianki) { this.procentNaZachcianki = procentNaZachcianki; }
    public Integer getProcentNaInwestycje() { return procentNaInwestycje; }
    public void setProcentNaInwestycje(Integer procentNaInwestycje) { this.procentNaInwestycje = procentNaInwestycje; }
}
