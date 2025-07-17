package zarzadzanieFinansami.DTO.budzet.szablon;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import zarzadzanieFinansami.DTO.budzet.RegulaKwotowaDTO;
import zarzadzanieFinansami.DTO.budzet.RegulaProcentoweDTO;
import zarzadzanieFinansami.modele.enumeracje.TypRegulyBudzetowejEnum;

import java.util.List;

public class SzablonBudzetuWysylanieDTO {
    @NotBlank(message = "Nazwa szablonu nie może być pusta.")
    @Size(max = 100)
    private String nazwa;

    @Size(max = 500)
    private String opis;

    @Valid
    private List<PozycjaSzablonuBudzetuWysylanieDTO> pozycjeSzablonu;

    // Określa, jaki rodzaj domyślnej reguły ma być zapisany w szablonie
    private TypRegulyBudzetowejEnum typReguly = TypRegulyBudzetowejEnum.BRAK;

    // Obiekt z danymi dla reguły procentowej
    @Valid
    private RegulaProcentoweDTO regulaProcentowa;

    // Obiekt z danymi dla reguły kwotowej
    @Valid
    private RegulaKwotowaDTO regulaKwotowa;

    // Gettery i Settery
    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }
    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }
    public List<PozycjaSzablonuBudzetuWysylanieDTO> getPozycjeSzablonu() { return pozycjeSzablonu; }
    public void setPozycjeSzablonu(List<PozycjaSzablonuBudzetuWysylanieDTO> pozycjeSzablonu) { this.pozycjeSzablonu = pozycjeSzablonu; }
    public TypRegulyBudzetowejEnum getTypReguly() { return typReguly; }
    public void setTypReguly(TypRegulyBudzetowejEnum typReguly) { this.typReguly = typReguly; }
    public RegulaProcentoweDTO getRegulaProcentowa() { return regulaProcentowa; }
    public void setRegulaProcentowa(RegulaProcentoweDTO regulaProcentowa) { this.regulaProcentowa = regulaProcentowa; }
    public RegulaKwotowaDTO getRegulaKwotowa() { return regulaKwotowa; }
    public void setRegulaKwotowa(RegulaKwotowaDTO regulaKwotowa) { this.regulaKwotowa = regulaKwotowa; }
}
