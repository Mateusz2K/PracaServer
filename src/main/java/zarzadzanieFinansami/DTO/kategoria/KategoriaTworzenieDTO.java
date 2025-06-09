package zarzadzanieFinansami.DTO.kategoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import zarzadzanieFinansami.modele.enumeracje.TypTransakcjiEnum;

public class KategoriaTworzenieDTO {

    @NotBlank(message = "Nazwa kategorii nie może być pusta.")
    private String nazwa;

    @NotNull(message = "Typ transakcji dla kategorii jest wymagany (KOSZT/PRZYCHÓD).")
    private TypTransakcjiEnum typTransakcji;

    // Gettery i Settery
    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public TypTransakcjiEnum getTypTransakcji() {
        return typTransakcji;
    }

    public void setTypTransakcji(TypTransakcjiEnum typTransakcji) {
        this.typTransakcji = typTransakcji;
    }
}