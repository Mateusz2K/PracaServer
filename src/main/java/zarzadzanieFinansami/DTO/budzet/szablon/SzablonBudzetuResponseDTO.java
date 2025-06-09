package zarzadzanieFinansami.DTO.budzet.szablon;


import java.util.List;

public class SzablonBudzetuResponseDTO {
    private Long id;
    private String nazwa;
    private String opis;
    private Integer uzytkownikId; // ID twórcy, null dla systemowych
    private boolean czySystemowy; // Flaga wskazująca, czy szablon jest systemowy
    private Integer procentNaPotrzeby;
    private Integer procentNaZachcianki;
    private Integer procentNaInwestycje;
    private List<PozycjaSzablonuBudzetuResponseDTO> pozycjeSzablonu;

    // Konstruktor, Gettery i Settery
    public SzablonBudzetuResponseDTO(Long id, String nazwa, String opis, Integer uzytkownikId, boolean czySystemowy, Integer procentNaPotrzeby, Integer procentNaZachcianki, Integer procentNaInwestycje, List<PozycjaSzablonuBudzetuResponseDTO> pozycjeSzablonu) {
        this.id = id;
        this.nazwa = nazwa;
        this.opis = opis;
        this.uzytkownikId = uzytkownikId;
        this.czySystemowy = czySystemowy;
        this.procentNaPotrzeby = procentNaPotrzeby;
        this.procentNaZachcianki = procentNaZachcianki;
        this.procentNaInwestycje = procentNaInwestycje;
        this.pozycjeSzablonu = pozycjeSzablonu;
    }

    // Gettery
    public Long getId() { return id; }
    public String getNazwa() { return nazwa; }
    public String getOpis() { return opis; }
    public Integer getUzytkownikId() { return uzytkownikId; }
    public boolean isCzySystemowy() { return czySystemowy; }
    public Integer getProcentNaPotrzeby() { return procentNaPotrzeby; }
    public Integer getProcentNaZachcianki() { return procentNaZachcianki; }
    public Integer getProcentNaInwestycje() { return procentNaInwestycje; }
    public List<PozycjaSzablonuBudzetuResponseDTO> getPozycjeSzablonu() { return pozycjeSzablonu; }
}
