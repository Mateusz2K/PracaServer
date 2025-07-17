package zarzadzanieFinansami.DTO.budzet;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import zarzadzanieFinansami.modele.enumeracje.OkresowoscEnum;
import zarzadzanieFinansami.modele.enumeracje.TypAlokacjiEnum;
import zarzadzanieFinansami.modele.enumeracje.TypRegulyBudzetowejEnum;

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

    // Określa, jaki rodzaj reguły jest stosowany do automatycznego podziału budżetu
    private TypRegulyBudzetowejEnum typReguly = TypRegulyBudzetowejEnum.BRAK; // Domyślnie brak reguły

    // Obiekt z danymi dla reguły procentowej (jeśli typReguly == PROCENTOWA)
    @Valid
    private RegulaProcentoweDTO regulaProcentowa;

    // Obiekt z danymi dla reguły kwotowej (jeśli typReguly == KWOTOWA)
    @Valid
    private RegulaKwotowaDTO regulaKwotowa;

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

    public TypRegulyBudzetowejEnum getTypReguly() { return typReguly; }
    public void setTypReguly(TypRegulyBudzetowejEnum typReguly) { this.typReguly = typReguly; }
    public RegulaProcentoweDTO getRegulaProcentowa() { return regulaProcentowa; }
    public void setRegulaProcentowa(RegulaProcentoweDTO regulaProcentowa) { this.regulaProcentowa = regulaProcentowa; }
    public RegulaKwotowaDTO getRegulaKwotowa() { return regulaKwotowa; }
    public void setRegulaKwotowa(RegulaKwotowaDTO regulaKwotowa) { this.regulaKwotowa = regulaKwotowa; }

    @AssertTrue(message = "Jeśli wybrano regułę procentową, jej dane muszą być dostarczone.")
    private boolean isRegulaProcentowaValid() {
        if (typReguly == TypRegulyBudzetowejEnum.PROCENTOWA) {
            return regulaProcentowa != null;
        }
        return true;
    }

    @AssertTrue(message = "Jeśli wybrano regułę kwotową, jej dane muszą być dostarczone.")
    private boolean isRegulaKwotowaValid() {
        if (typReguly == TypRegulyBudzetowejEnum.KWOTOWA) {
            return regulaKwotowa != null;
        }
        return true;
    }

    @AssertTrue(message = "Suma kwot w regule kwotowej nie może przekraczać przewidywanego dochodu.")
    private boolean isSumaKwotValid() {
        // Ta walidacja ma sens tylko, gdy użytkownik wybrał regułę kwotową.
        if (typReguly != TypRegulyBudzetowejEnum.KWOTOWA) {
            return true; // Ignorujemy walidację dla innych typów reguł.
        }
        // Jeśli regulaKwotowa jest null, inna walidacja (`isRegulaKwotowaValid`) to wychwyci.
        // Tutaj zakładamy, że obiekt istnieje, aby uniknąć NullPointerException.
        if (regulaKwotowa == null || przewidywanyDochod == null) {
            return true; // Nie walidujemy, jeśli brakuje kluczowych danych.
        }

        BigDecimal kwotaPotrzeby = regulaKwotowa.getKwotaNaPotrzeby() != null ? regulaKwotowa.getKwotaNaPotrzeby() : BigDecimal.ZERO;
        BigDecimal kwotaZachcianki = regulaKwotowa.getKwotaNaZachcianki() != null ? regulaKwotowa.getKwotaNaZachcianki() : BigDecimal.ZERO;
        BigDecimal kwotaInwestycje = regulaKwotowa.getKwotaNaInwestycje() != null ? regulaKwotowa.getKwotaNaInwestycje() : BigDecimal.ZERO;
        BigDecimal sumaKwot = kwotaPotrzeby.add(kwotaZachcianki).add(kwotaInwestycje);

        // .compareTo() zwraca -1 (mniejsza), 0 (równa), lub 1 (większa).
        // Chcemy, aby suma była mniejsza lub równa dochodowi.
        return sumaKwot.compareTo(przewidywanyDochod) <= 0;
    }

    @AssertTrue(message = "Suma kwot zdefiniowanych w pozycjach budżetu nie może przekraczać przewidywanego dochodu.")
    private boolean isSumaPozycjiValid() {
        // Ta walidacja ma sens tylko, gdy użytkownik ręcznie definiuje pozycje.
        if (typReguly != TypRegulyBudzetowejEnum.BRAK) {
            return true;
        }
        if (pozycjeBudzetu == null || pozycjeBudzetu.isEmpty() || przewidywanyDochod == null) {
            return true; // Nie ma czego walidować.
        }
        BigDecimal sumaPozycji = pozycjeBudzetu.stream()
                .map(pozycja -> pozycja.getTypAlokacji() == TypAlokacjiEnum.KWOTOWA
                        ? (pozycja.getKwotaAlokowana() != null ? pozycja.getKwotaAlokowana() : BigDecimal.ZERO)
                        : (pozycja.getProcentAlokowany() != null ? przewidywanyDochod.multiply(pozycja.getProcentAlokowany()).divide(new BigDecimal("100")) : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sumaPozycji.compareTo(przewidywanyDochod) <= 0;
    }
}
