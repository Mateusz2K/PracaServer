package zarzadzanieFinansami.DTO.budzet.szablon;



import zarzadzanieFinansami.modele.enumeracje.TypAlokacjiEnum;
import java.math.BigDecimal;

public class PozycjaSzablonuBudzetuResponseDTO {
    private Long id;
    private Integer kategoriaId;
    private String kategoriaNazwa; // Jeśli kategoriaId jest ustawione
    private String metaKategoriaNazwa;
    private TypAlokacjiEnum typAlokacji;
    private BigDecimal procentAlokowany;
    private BigDecimal kwotaAlokowana;

    // Konstruktor, Gettery i Settery
    public PozycjaSzablonuBudzetuResponseDTO(Long id, Integer kategoriaId, String kategoriaNazwa, String metaKategoriaNazwa, TypAlokacjiEnum typAlokacji, BigDecimal procentAlokowany, BigDecimal kwotaAlokowana) {
        this.id = id;
        this.kategoriaId = kategoriaId;
        this.kategoriaNazwa = kategoriaNazwa;
        this.metaKategoriaNazwa = metaKategoriaNazwa;
        this.typAlokacji = typAlokacji;
        this.procentAlokowany = procentAlokowany;
        this.kwotaAlokowana = kwotaAlokowana;
    }

    // Gettery
    public Long getId() { return id; }
    public Integer getKategoriaId() { return kategoriaId; }
    public String getKategoriaNazwa() { return kategoriaNazwa; }
    public String getMetaKategoriaNazwa() { return metaKategoriaNazwa; }
    public TypAlokacjiEnum getTypAlokacji() { return typAlokacji; }
    public BigDecimal getProcentAlokowany() { return procentAlokowany; }
    public BigDecimal getKwotaAlokowana() { return kwotaAlokowana; }
}
