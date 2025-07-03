package zarzadzanieFinansami.serwisy;

import org.springframework.security.crypto.password.PasswordEncoder;
// Usunięto import zarzadzanieFinansami.DTO.UzytkownikDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional; // Dodano dla transakcyjności
import zarzadzanieFinansami.magazyn.MagazynUzytkownika;
import zarzadzanieFinansami.magazyn.MagazynUzytkownikaDodatek;
import zarzadzanieFinansami.modele.Uzytkownik;
// Usunięto import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Niepotrzebny, używamy wstrzykniętego interfejsu
import org.springframework.stereotype.Service;
import zarzadzanieFinansami.modele.enumeracje.RolaEnum;
import zarzadzanieFinansami.wyjątki.DaneNieZnalesionoExeption;

import java.util.List;
import java.util.Optional;

@Service
@Transactional // Dobra praktyka dla serwisów modyfikujących dane
public class UzytkownikUsluga implements MagazynUzytkownikaDodatek {

    private final PasswordEncoder passwordEncoder;
    private final MagazynUzytkownika magazynUzytkownika;

    public UzytkownikUsluga(MagazynUzytkownika magazynUzytkownika, PasswordEncoder passwordEncoder) {
        this.magazynUzytkownika = magazynUzytkownika;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Tworzy nowego użytkownika na podstawie podanych danych.
     * @param nazwa Nazwa użytkownika.
     * @param email Email użytkownika (używany też do logowania).
     * @param suroweHaslo Hasło użytkownika w postaci czystego tekstu (zostanie zakodowane).
     * @return Zapisana encja Uzytkownik.
     */
    public Uzytkownik stworzUzytkownika(String nazwa, String email, String suroweHaslo) {
        // Podstawowa walidacja parametrów wejściowych
        if (nazwa == null || nazwa.isEmpty() ||
                email == null || email.isEmpty() ||
                suroweHaslo == null || suroweHaslo.isEmpty()) {
            throw new IllegalArgumentException("Nazwa, email lub hasło nie mogą być puste.");
            // Sprawdzenie, czy nazwa użytkownika jest już zajęta
        }
        if (magazynUzytkownika.existsByNazwa(nazwa)) {
            throw new IllegalArgumentException("Nazwa użytkownika '" + nazwa + "' jest już zajęta.");
        }
        // Sprawdzenie, czy email jest już zajęty
        if (magazynUzytkownika.existsByEmail(email)) {
            throw new IllegalArgumentException("Adres email '" + email + "' jest już zarejestrowany.");
        }
        // Można dodać sprawdzenie, czy email już istnieje
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Użytkownik z adresem email '" + email + "' już istnieje.");
        }


        Uzytkownik uzytkownik = new Uzytkownik();
        uzytkownik.setName(nazwa);
        uzytkownik.setEmail(email);
        uzytkownik.setHaslo(passwordEncoder.encode(suroweHaslo)); // Kodowanie hasła
        uzytkownik.setRola(RolaEnum.USER); // Ustawienie domyślnej roli

        // Zapisanie użytkownika i zwrócenie zapisanej encji
        return magazynUzytkownika.save(uzytkownik);
        // Nie zwracamy już DTO, a zwłaszcza nie zwracamy hasła!
    }

    /**
     * Usuwa użytkownika o podanym ID.
     * @param id ID użytkownika do usunięcia (typ Long).
     */
    public void usunUzytkownika(Integer id) { // Zmieniono typ ID na Long
        // Sprawdzenie czy użytkownik istnieje przed próbą usunięcia
        if (!magazynUzytkownika.existsById(id)) {
            throw new EntityNotFoundException("Użytkownik o ID " + id + " nie istnieje, nie można usunąć.");
        }
        magazynUzytkownika.deleteById(id); // Użycie deleteById jest często wystarczające
    }

    /**
     * Znajduje wszystkich użytkowników.
     * @return Lista encji Uzytkownik.
     */
    @Transactional(readOnly = true) // Metody tylko do odczytu
    public List<Uzytkownik> znajdzWszystkichUzytkownikow() { // Zmieniono nazwę dla jasności
        return magazynUzytkownika.findAll();
    }

    /**
     * Znajduje użytkownika po adresie email.
     * @param email Email użytkownika.
     * @return Encja Uzytkownik lub null, jeśli nie znaleziono.
     */
    @Override
    @Transactional(readOnly = true)
    public Uzytkownik znajdzUzytkownikPrzezEmail(String email) {
        // findByEmail powinno zwracać Optional<Uzytkownik> w repozytorium
        return magazynUzytkownika.findByEmail(email).orElse(null);
    }

    /**
     * Sprawdza, czy podane hasło pasuje do hasła użytkownika o danym emailu.
     * @param email Email użytkownika.
     * @param suroweHaslo Hasło do sprawdzenia (czysty tekst).
     * @return true, jeśli hasło pasuje, false w przeciwnym razie.
     */
    @Override
    @Transactional(readOnly = true) // Tylko odczyt
    public boolean checkPassword(String email, String suroweHaslo) {
        Optional<Uzytkownik> uzytkownikOpt = magazynUzytkownika.findByEmail(email);
        // Użytkownik nie istnieje
        // Porównuje surowe hasło z zakodowanym hasłem z bazy
        return uzytkownikOpt.filter(uzytkownik -> passwordEncoder.matches(suroweHaslo, uzytkownik.getHaslo())).isPresent();
    }

    /**
     * Sprawdza, czy użytkownik o podanym emailu istnieje.
     * @param email Email do sprawdzenia.
     * @return true, jeśli użytkownik istnieje, false w przeciwnym razie.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        // existsByEmail może być dedykowaną metodą w repozytorium dla wydajności
        return magazynUzytkownika.existsByEmail(email);
        // Alternatywnie, jeśli masz w repo: boolean existsByEmail(String email);
        // return magazynUzytkownika.existsByEmail(email);
    }

    /**
     * Znajduje użytkownika po jego ID.
     * @param id ID użytkownika (typ Long).
     * @return Encja Uzytkownik lub null, jeśli nie znaleziono.
     */
    @Transactional(readOnly = true)
    public Uzytkownik znajdzUzytkownikaPrzezId(Integer id) { // Poprawiono literówkę i typ ID
        return magazynUzytkownika.findById(id).orElse(null);
    }

    /**
     * Zmienia dane istniejącego użytkownika.
     * @param id ID użytkownika do modyfikacji (typ Long).
     * @param nowaNazwa Nowa nazwa użytkownika.
     * @param nowyEmail Nowy email użytkownika.
     * @param noweSuroweHaslo Nowe hasło w postaci czystego tekstu (jeśli null lub puste, hasło nie jest zmieniane).
     * @return Zaktualizowana encja Uzytkownik.
     */
    public Uzytkownik zmienUzytkownika(Integer id, String nowaNazwa, String nowyEmail, String noweSuroweHaslo) { // Zmieniono typ ID i parametry
        // Walidacja podstawowych danych (nazwa, email)
        if (nowaNazwa == null || nowaNazwa.isEmpty() ||
                nowyEmail == null || nowyEmail.isEmpty()) {
            throw new IllegalArgumentException("Nazwa i email nie mogą być puste podczas aktualizacji.");
        }
        // Sprawdzenie, czy nazwa użytkownika jest już zajęta
        if (magazynUzytkownika.existsByNazwa(nowaNazwa)) { // Zakładając, że 'nazwa' to pole unikalnego username
            throw new IllegalArgumentException("Nazwa użytkownika '" + nowaNazwa + "' jest już zajęta.");
        }
        // Sprawdzenie, czy email jest już zajęty (jeśli email też ma być unikalny)
        if (magazynUzytkownika.existsByEmail(nowyEmail)) {
            throw new IllegalArgumentException("Adres email '" + nowyEmail + "' jest już zarejestrowany.");
        }

        // Znajdź istniejącego użytkownika lub rzuć wyjątek
        Uzytkownik uzytkownik = magazynUzytkownika.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Użytkownik o ID " + id + " nie istnieje, nie można zmodyfikować."));

        // Zaktualizuj pola
        uzytkownik.setName(nowaNazwa);
        uzytkownik.setEmail(nowyEmail);

        // Zaktualizuj hasło tylko jeśli zostało podane nowe
        if (noweSuroweHaslo != null && !noweSuroweHaslo.isEmpty()) {
            uzytkownik.setHaslo(passwordEncoder.encode(noweSuroweHaslo)); // Zakoduj nowe hasło
        }
        // Jeśli noweSuroweHaslo jest null/puste, stare hasło pozostaje bez zmian

        // Zapisz zmiany i zwróć zaktualizowaną encję
        return magazynUzytkownika.save(uzytkownik);
    }

    public Uzytkownik znajdzUrzytkownikaPrzezNazwe(String nazwaUzytkownika) {
        return magazynUzytkownika.findByNazwa(nazwaUzytkownika);
    }

    public void zmienDaneLogowaniaPoEmail(String email, String nowaNazwa, String noweHaslo) {
        Uzytkownik uzytkownik = magazynUzytkownika.findByEmail(email) // Załóżmy, że masz metodę findByEmail
                .orElseThrow(() -> new DaneNieZnalesionoExeption("Użytkownik o adresie email '" + email + "' nie został znaleziony."));

        boolean dokonanoZmiany = false;

        if (nowaNazwa != null && !nowaNazwa.isBlank() && !nowaNazwa.equals(uzytkownik.getName())) {
            // Sprawdzenie unikalności nowej nazwy użytkownika
            if (magazynUzytkownika.existsByNazwa(nowaNazwa)) { // Metoda existsByNazwa w repozytorium
                throw new IllegalArgumentException("Nazwa użytkownika '" + nowaNazwa + "' jest już zajęta.");
            }
            uzytkownik.setName(nowaNazwa); // Zakładając, że pole w encji Uzytkownik to 'nazwa'
            dokonanoZmiany = true;
        }

        if (noweHaslo != null && !noweHaslo.isBlank()) {
            uzytkownik.setHaslo(passwordEncoder.encode(noweHaslo));
            dokonanoZmiany = true;
        }

        if (dokonanoZmiany) {
            magazynUzytkownika.save(uzytkownik);
        }
        // Jeśli nie dokonano żadnej zmiany, można rozważyć rzucenie wyjątku lub zwrócenie innego statusu,
        // ale obecna logika po prostu nie zapisze, jeśli nic się nie zmieniło (poza odnalezieniem użytkownika).
    }
}