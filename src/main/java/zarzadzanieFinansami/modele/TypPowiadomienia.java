package zarzadzanieFinansami.modele;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "typ_powiadomienia")
public class TypPowiadomienia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(length = 100)
    private String nazwa;
    @Column(length = 100)
    private String opis;

    //połaczenie forgein powiadomienia
    @OneToMany(mappedBy = "typPowiadomienia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Powiadomienia> powiadomienia;

    public TypPowiadomienia(String nazwa, String opis) {
        this.nazwa = nazwa;
        this.opis = opis;
    }

    public TypPowiadomienia() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public List<Powiadomienia> getPowiadomienia() {
        return powiadomienia;
    }

    public void setPowiadomienia(List<Powiadomienia> powiadomienia) {
        this.powiadomienia = powiadomienia;
    }
}
