package zarzadzanieFinansami.DTO.logowanie;

public class RejestrowanieOdpowiedzDTO {
    private String message;
    private String nazwa; // lub inne pola, które chcesz zwrócić

    public RejestrowanieOdpowiedzDTO(String message, String nazwa) {
        this.message = message;
        this.nazwa = nazwa;
    }

    public String getMessage() {
        return message;
    }

    public String getNazwa() {
        return nazwa;
    }
}
