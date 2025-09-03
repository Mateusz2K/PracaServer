package zarzadzanieFinansami.modele;

import jakarta.persistence.*;
import zarzadzanieFinansami.modele.enumeracje.OkresowoscEnum;

import java.math.BigDecimal;

@Entity
@Table(name = "zasady_oszczedzania")
public class ZasadyOszczedzania {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private double procent;
    @Enumerated(EnumType.STRING)
    private OkresowoscEnum okresowosc;
    @Column(precision = 15, scale = 2)
    private BigDecimal kwota;
    @Column(precision = 15, scale = 2)
    private BigDecimal kwotaOdProcenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cel_id",nullable = false)
    private Cel cel;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "odbiorca_id")
    private Konto odbiorca;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nadawca_id")
    private Konto nadawca;

    public ZasadyOszczedzania(double procent, OkresowoscEnum okresowosc, BigDecimal kwota, BigDecimal kwotaOdProcenta, Cel cel, Konto odbiorca, Konto nadawca) {
        this.procent = procent;
        this.okresowosc = okresowosc;
        this.kwota = kwota;
        this.kwotaOdProcenta = kwotaOdProcenta;
        this.cel = cel;
        this.odbiorca = odbiorca;
        this.nadawca = nadawca;
    }

    public ZasadyOszczedzania() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getProcent() {
        return procent;
    }

    public void setProcent(double procent) {
        this.procent = procent;
    }

    public OkresowoscEnum getOkresowosc() {
        return okresowosc;
    }

    public void setOkresowosc(OkresowoscEnum okresowosc) {
        this.okresowosc = okresowosc;
    }

    public BigDecimal getKwota() {
        return kwota;
    }

    public void setKwota(BigDecimal kwota) {
        this.kwota = kwota;
    }

    public BigDecimal getKwotaOdProcenta() {
        return kwotaOdProcenta;
    }

    public void setKwotaOdProcenta(BigDecimal kwotaOdProcenta) {
        this.kwotaOdProcenta = kwotaOdProcenta;
    }

    public Cel getCel() {
        return cel;
    }

    public void setCel(Cel cel) {
        this.cel = cel;
    }

    public Konto getOdbiorca() {
        return odbiorca;
    }

    public void setOdbiorca(Konto odbiorca) {
        this.odbiorca = odbiorca;
    }

    public Konto getNadawca() {
        return nadawca;
    }

    public void setNadawca(Konto nadawca) {
        this.nadawca = nadawca;
    }
}
