package zarzadzanieFinansami.DTO.budzet;

import zarzadzanieFinansami.modele.enumeracje.OkresowoscEnum;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BudzetOdpowiedzDTO {
    private Long id;
    private String nazwa;
    private Integer uzytkownikId;
    private LocalDate dataPoczatkowa;
    private LocalDate dataKoncowa;
    private OkresowoscEnum okresowosc;
    private BigDecimal przewidywanyDochod;
    private BigDecimal sumaAlokowana; // Suma zaplanowanych wydatków
    private BigDecimal sumaRzeczywistychWydatkow; // Suma faktycznych wydatków
    private BigDecimal saldoBudzetu; // dochod - sumaRzeczywistychWydatkow LUB sumaAlokowana - sumaRzeczywistychWydatkow
    private boolean aktywny;
    private Long szablonId;
    private Integer procentNaPotrzeby;
    private Integer procentNaZachcianki;
    private Integer procentNaInwestycje;
    private List<PozycjaBudzetuOdpowiedzDTO> pozycjeBudzetu;

    // Konstruktor, Gettery i Settery
    public BudzetOdpowiedzDTO(Long id, String nazwa, Integer uzytkownikId, LocalDate dataPoczatkowa, LocalDate dataKoncowa, OkresowoscEnum okresowosc, BigDecimal przewidywanyDochod, BigDecimal sumaAlokowana, BigDecimal sumaRzeczywistychWydatkow, boolean aktywny, Long szablonId, Integer procentNaPotrzeby, Integer procentNaZachcianki, Integer procentNaInwestycje, List<PozycjaBudzetuOdpowiedzDTO> pozycjeBudzetu) {
        this.id = id;
        this.nazwa = nazwa;
        this.uzytkownikId = uzytkownikId;
        this.dataPoczatkowa = dataPoczatkowa;
        this.dataKoncowa = dataKoncowa;
        this.okresowosc = okresowosc;
        this.przewidywanyDochod = przewidywanyDochod;
        this.sumaAlokowana = sumaAlokowana != null ? sumaAlokowana : BigDecimal.ZERO;
        this.sumaRzeczywistychWydatkow = sumaRzeczywistychWydatkow != null ? sumaRzeczywistychWydatkow : BigDecimal.ZERO;
        this.aktywny = aktywny;
        this.szablonId = szablonId;
        this.procentNaPotrzeby = procentNaPotrzeby;
        this.procentNaZachcianki = procentNaZachcianki;
        this.procentNaInwestycje = procentNaInwestycje;
        this.pozycjeBudzetu = pozycjeBudzetu;

        // Obliczanie salda - np. ile z zaplanowanych alokacji zostało
        this.saldoBudzetu = this.sumaAlokowana.subtract(this.sumaRzeczywistychWydatkow);
    }

    // Gettery
    public Long getId() { return id; }
    public String getNazwa() { return nazwa; }
    public Integer getUzytkownikId() { return uzytkownikId; }
    public LocalDate getDataPoczatkowa() { return dataPoczatkowa; }
    public LocalDate getDataKoncowa() { return dataKoncowa; }
    public OkresowoscEnum getOkresowosc() { return okresowosc; }
    public BigDecimal getPrzewidywanyDochod() { return przewidywanyDochod; }
    public BigDecimal getSumaAlokowana() { return sumaAlokowana; }
    public BigDecimal getSumaRzeczywistychWydatkow() { return sumaRzeczywistychWydatkow; }
    public BigDecimal getSaldoBudzetu() { return saldoBudzetu; }
    public boolean isAktywny() { return aktywny; }
    public Long getSzablonId() { return szablonId; }
    public Integer getProcentNaPotrzeby() { return procentNaPotrzeby; }
    public Integer getProcentNaZachcianki() { return procentNaZachcianki; }
    public Integer getProcentNaInwestycje() { return procentNaInwestycje; }
    public List<PozycjaBudzetuOdpowiedzDTO> getPozycjeBudzetu() { return pozycjeBudzetu; }
}
