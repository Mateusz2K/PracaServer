package zarzadzanieFinansami.DTO.logowanie;

public class JwtOdpowiedzDTO {
    private String token;
    private String type = "Bearer"; // Standardowy typ tokenu JWT
    private String nazwa; // Lub username, aby klient wiedział, dla kogo jest token
    // Możesz tu dodać inne informacje, np. ID użytkownika, role, jeśli potrzebne

    public JwtOdpowiedzDTO(String accessToken, String nazwa) {
        this.token = accessToken;
        this.nazwa = nazwa;
    }

    // Gettery
    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getNazwa() {
        return nazwa;
    }

    // Settery (opcjonalne)
    public void setToken(String token) {
        this.token = token;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }
}