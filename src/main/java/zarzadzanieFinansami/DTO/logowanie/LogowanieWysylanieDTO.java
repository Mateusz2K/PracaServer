package zarzadzanieFinansami.DTO.logowanie;

import jakarta.validation.constraints.NotBlank;

public class LogowanieWysylanieDTO {

    @NotBlank(message = "Nazwa nie może być pusta")
    private String nazwa;
    @NotBlank(message = "Hasło nie może być puste")
    private String hasło;

    // Gettery

    public String getNazwa() {
        return nazwa;
    }


    public String getHasło() {
        return hasło;
    }

    // Settery (opcjonalne, jeśli używasz konstruktora lub deserializacji przez framework)

    public void setHasło(String password) {
        this.hasło = password;
    }
    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }
}