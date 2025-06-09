package zarzadzanieFinansami.DTO.konto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class KontoTworzenieDTO {

        @NotBlank(message = "Nazwa konta nie może być pusta")
        @Size(min = 3, max = 100, message = "Nazwa konta musi mieć od 3 do 100 znaków")
        private String nazwa;

        @NotNull(message = "Typ konta nie może być pusty")
        private String typ; // Np. "OSOBISTE", "OSZCZEDNOSCIOWE" - odpowiadające Twoim enumom

        @NotNull(message = "Waluta nie może być pusta")
        private String waluta; // Np. "PLN", "USD" - odpowiadające Twoim enumom

        private BigDecimal bilans;// Opcjonalny

        public KontoTworzenieDTO() {
        }

        public KontoTworzenieDTO(String nazwa, String typ, String waluta, BigDecimal bilans) {
                this.nazwa = nazwa;
                this.typ = typ;
                this.waluta = waluta;
                this.bilans = bilans;
        }

        // Gettery i Settery
        public String getNazwa() { return nazwa; }
        public void setNazwa(String nazwa) { this.nazwa = nazwa; }
        public String getTyp() { return typ; }
        public void setTyp(String typ) { this.typ = typ; }
        public String getWaluta() { return waluta; }
        public void setWaluta(String waluta) { this.waluta = waluta; }
        public BigDecimal getBilans() { return bilans; }
        public void setBilans(BigDecimal bilans) { this.bilans = bilans; }
}

