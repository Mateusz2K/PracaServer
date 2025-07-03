package zarzadzanieFinansami.DTO.logowanie;

public class UzytkownikOdpowiedzDTO {
        private Integer id;
        private String name;
        private String email;
        private String rola;

        // Konstruktor, Gettery (Settery niekoniecznie potrzebne)

        public UzytkownikOdpowiedzDTO(Integer id, String name, String email, String rola) {
                this.id = id;
                this.name = name;
                this.email = email;
                this.rola = rola;
        }

        public Integer getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRola() { return rola; }
}
