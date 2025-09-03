// src/main/java/zarzadzanieFinansami/DTO/budzet/PozycjaBudzetuResponseDTO.java
package zarzadzanieFinansami.DTO.budzet;

import zarzadzanieFinansami.modele.enumeracje.TypAlokacjiEnum;

import java.math.BigDecimal;

public class PozycjaBudzetuOdpowiedzDTO {
    private Long id;
    private Integer kategoriaId;
    private String kategoriaNazwa;
    private TypAlokacjiEnum typAlokacji;
    private BigDecimal procentAlokowany; // np. 10.00 dla 10%
    private BigDecimal kwotaAlokowana;   // Planowana kwota
    private BigDecimal rzeczywisteWydatki; // Rzeczywiste wydatki w okresie budżetu
    private BigDecimal pozostalo;          // kwotaAlokowana - rzeczywisteWydatki
    private Double procentWykorzystania; // (rzeczywisteWydatki / kwotaAlokowana) * 100

    public PozycjaBudzetuOdpowiedzDTO(Long id, Integer kategoriaId, String kategoriaNazwa, TypAlokacjiEnum typAlokacji, BigDecimal procentAlokowany, BigDecimal kwotaAlokowana, BigDecimal rzeczywisteWydatki) {
        this.id = id;
        this.kategoriaId = kategoriaId;
        this.kategoriaNazwa = kategoriaNazwa;
        this.typAlokacji = typAlokacji;
        this.procentAlokowany = procentAlokowany;
        this.kwotaAlokowana = kwotaAlokowana;
        this.rzeczywisteWydatki = (rzeczywisteWydatki != null) ? rzeczywisteWydatki : BigDecimal.ZERO;

        if (this.kwotaAlokowana != null && this.kwotaAlokowana.compareTo(BigDecimal.ZERO) != 0) {
            this.pozostalo = this.kwotaAlokowana.subtract(this.rzeczywisteWydatki);
            this.procentWykorzystania = this.rzeczywisteWydatki
                    .multiply(new BigDecimal("100.00"))
                    .divide(this.kwotaAlokowana, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        } else {
            this.pozostalo = BigDecimal.ZERO.subtract(this.rzeczywisteWydatki); // Jeśli nie alokowano, a są wydatki
            this.procentWykorzystania = (this.rzeczywisteWydatki.compareTo(BigDecimal.ZERO) == 0) ? 0.0 : 100.0; // lub inna logika
        }
    }

    // Gettery
    public Long getId() { return id; }
    public Integer getKategoriaId() { return kategoriaId; }
    public String getKategoriaNazwa() { return kategoriaNazwa; }
    public TypAlokacjiEnum getTypAlokacji() { return typAlokacji; }
    public BigDecimal getProcentAlokowany() { return procentAlokowany; }
    public BigDecimal getKwotaAlokowana() { return kwotaAlokowana; }
    public BigDecimal getRzeczywisteWydatki() { return rzeczywisteWydatki; }
    public BigDecimal getPozostalo() { return pozostalo; }
    public Double getProcentWykorzystania() { return procentWykorzystania; }
}
