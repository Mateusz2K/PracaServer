package zarzadzanieFinansami.serwisy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zarzadzanieFinansami.DTO.kategoria.KategoriaWysylanieDTO;
import zarzadzanieFinansami.magazyn.MagazynKategorii;
import zarzadzanieFinansami.modele.Kategoria;
import zarzadzanieFinansami.modele.Uzytkownik; // Import
import zarzadzanieFinansami.modele.enumeracje.KategorieBudzetEnum;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;
import zarzadzanieFinansami.wyjątki.DuplikatException;

import java.util.List;
import java.util.Optional;

@Service
public class KategoriaUsługa {

    private final MagazynKategorii magazynKategorii;

    @Autowired
    public KategoriaUsługa(MagazynKategorii magazynKategorii) {
        this.magazynKategorii = magazynKategorii;
    }

    @Transactional
    public Kategoria stworzKategorie(KategoriaWysylanieDTO dto, Uzytkownik currentUser) { // <-- DODAJ currentUser
        // Sprawdzenie, czy kategoria o tej nazwie już istnieje DLA TEGO UŻYTKOWNIKA
        Optional<Kategoria> istniejacaKategoria = magazynKategorii.findByUzytkownikAndNazwa(currentUser, dto.getNazwa()); // <-- UŻYJ NOWEJ METODY
        if (istniejacaKategoria.isPresent()) {
            throw new DuplikatException("Kategoria o nazwie '" + dto.getNazwa() + "' już istnieje dla tego użytkownika.");
        }

        Kategoria nowaKategoria = new Kategoria();
        nowaKategoria.setNazwa(dto.getNazwa());
        nowaKategoria.setTypTransakcji(dto.getTypTransakcji());
        nowaKategoria.setKategorieBudzet(dto.getKategorieBudzetuEnum());
        nowaKategoria.setUzytkownik(currentUser); // <-- PRZYPISZ UŻYTKOWNIKA
        return magazynKategorii.save(nowaKategoria);
    }

    @Transactional(readOnly = true)
    public List<Kategoria> pobierzWszystkieKategorie(Uzytkownik currentUser) { // <-- DODAJ currentUser
        return magazynKategorii.findByUzytkownik(currentUser); // <-- UŻYJ NOWEJ METODY
    }

    @Transactional(readOnly = true)
    public Kategoria pobierzKategoriePoId(Integer id, Uzytkownik currentUser) { // <-- DODAJ currentUser
        // Znajdź kategorię po ID DLA TEGO UŻYTKOWNIKA
        return magazynKategorii.findByUzytkownikAndId(currentUser, id) // <-- UŻYJ NOWEJ METODY
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Kategoria o ID " + id + " nie została znaleziona dla tego użytkownika."));
    }

    @Transactional
    public Kategoria aktualizujKategorie(Integer id, KategoriaWysylanieDTO dto, Uzytkownik currentUser) { // <-- DODAJ currentUser
        Kategoria kategoriaDoAktualizacji = pobierzKategoriePoId(id, currentUser); // Wykorzystuje metodę z walidacją i filtrowaniem po użytkowniku

        // Sprawdzenie, czy nowa nazwa nie koliduje z inną istniejącą kategorią DLA TEGO UŻYTKOWNIKA
        Optional<Kategoria> istniejacaKategoriaZNazwa = magazynKategorii.findByUzytkownikAndNazwa(currentUser, dto.getNazwa()); // <-- UŻYJ NOWEJ METODY
        if (istniejacaKategoriaZNazwa.isPresent() && istniejacaKategoriaZNazwa.get().getId()!=(id)) { // Porównanie ID obiektów Integer
            // lub if (istniejacaKategoriaZNazwa.isPresent() && istniejacaKategoriaZNazwa.get().getId().intValue() != id.intValue()) { // Porównanie wartości int
            throw new DuplikatException("Kategoria o nazwie '" + dto.getNazwa() + "' już istnieje dla tego użytkownika.");
        }

        kategoriaDoAktualizacji.setNazwa(dto.getNazwa());
        kategoriaDoAktualizacji.setTypTransakcji(dto.getTypTransakcji());
        kategoriaDoAktualizacji.setKategorieBudzet(dto.getKategorieBudzetuEnum());
        // Użytkownik jest już ustawiony i nie powinien być zmieniany
        return magazynKategorii.save(kategoriaDoAktualizacji);
    }

    @Transactional
    public void usunKategorie(Integer id, Uzytkownik currentUser) { // <-- DODAJ currentUser
        // Znajdź kategorię po ID DLA TEGO UŻYTKOWNIKA
        Kategoria kategoria = pobierzKategoriePoId(id, currentUser); // Wykorzystuje metodę z walidacją i filtrowaniem po użytkowniku

        // Dodatkowa logika, jeśli potrzebna przed usunięciem
        // Np. sprawdzenie, czy kategoria nie jest używana w żadnych transakcjach
        // (jeśli nie chcesz usuwać kategorii używanych)
        if (!kategoria.getTransakcje().isEmpty()) {
            throw new IllegalStateException("Nie można usunąć kategorii, która jest przypisana do transakcji.");
        }
        magazynKategorii.delete(kategoria);
    }

    @Transactional(readOnly = true)
    public List<Kategoria> pobierzKategoriePoKategoriiBudzetu(KategorieBudzetEnum kategorieBudzetEnum, Uzytkownik uzytkownik) {
//        List<Kategoria> wszystkieKategorie = this.pobierzWszystkieKategorie(uzytkownik);
//        List<Kategoria> wybraneKategorie = new ArrayList<>();
//        for(Kategoria kategoria : wszystkieKategorie){
//            if(kategoria.getKategorieBudzet().equals(kategorieEnum)){
//                wybraneKategorie.add(kategoria);
//            }
//        }
//        return wybraneKategorie;
        return magazynKategorii.findByUzytkownikAndKategorieBudzet(uzytkownik, kategorieBudzetEnum);
    }


}