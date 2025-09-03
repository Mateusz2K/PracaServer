package zarzadzanieFinansami.DTO.konto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class KontoWysylanieDTO {

        @NotBlank(message = "Nazwa konta nie może być pusta")
        @Size(min = 3, max = 100, message = "Nazwa konta musi mieć od 3 do 100 znaków")
        private String nazwa;

        @NotNull(message = "Typ konta nie może być pusty")
        private String typ; // Np. "OSOBISTE", "OSZCZEDNOSCIOWE" - odpowiadające Twoim enumom


        private BigDecimal bilans;// Opcjonalny

        public KontoWysylanieDTO() {
        }

        public KontoWysylanieDTO(String nazwa, String typ, String waluta, BigDecimal bilans) {
                this.nazwa = nazwa;
                this.typ = typ;
                this.bilans = bilans;
        }

        // Gettery i Settery
        public String getNazwa() { return nazwa; }
        public void setNazwa(String nazwa) { this.nazwa = nazwa; }
        public String getTyp() { return typ; }
        public void setTyp(String typ) { this.typ = typ; }
        public BigDecimal getBilans() { return bilans; }
        public void setBilans(BigDecimal bilans) { this.bilans = bilans; }
}

