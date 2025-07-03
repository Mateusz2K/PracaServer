package zarzadzanieFinansami.DTO.logowanie;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ZmianaUzytkownikaWysylanieDTO {
    private String nowaNazwa;
    @NotBlank(message = "Email nie może być pusty")
    @Email
    private String nowyEmail;
    @NotBlank(message = "Hasło nie może być puste")
    @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków")
    private String noweHaslo; // Opcjonalne

    // Gettery i Settery

    public String getNowaNazwa() {
        return nowaNazwa;
    }

    public void setNowaNazwa(String nowaNazwa) {
        this.nowaNazwa = nowaNazwa;
    }

    public String getNowyEmail() {
        return nowyEmail;
    }

    public void setNowyEmail(String nowyEmail) {
        this.nowyEmail = nowyEmail;
    }

    public String getNoweHaslo() {
        return noweHaslo;
    }

    public void setNoweHaslo(String noweHaslo) {
        this.noweHaslo = noweHaslo;
    }

}
