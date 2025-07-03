package zarzadzanieFinansami.DTO.logowanie;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejestrowanieUzytkownikaWysylanieDTO {
    String imie;
    @NotBlank(message = "Nazwa użytkownika nie może być pusta.")
    String nazwa;
    @NotBlank(message = "Email nie może być pusty.")
    @Email(message = "Email musi być poprawnym adresem email.")
    String email;
    @NotBlank(message = "Hasło nie może być puste.")
    @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków.")
    String hasło;



    public RejestrowanieUzytkownikaWysylanieDTO(String imie, String nazwa, String email, String hasło) {
        this.imie = imie;
        this.nazwa = nazwa;
        this.email = email;
        this.hasło = hasło;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHasło() {
        return hasło;
    }

    public void setHasło(String hasło) {
        this.hasło = hasło;
    }
    public String getImie() {
        return imie;
    }

    public void setImie(String imie) {
        this.imie = imie;
    }
}
