package zarzadzanieFinansami.modele;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class SzczegolyUzytkownika implements UserDetails {


    private final Uzytkownik uzytkownik;

    public SzczegolyUzytkownika(Uzytkownik uzytkownik) {
        this.uzytkownik = Objects.requireNonNull(uzytkownik, "Użytkownik nie może być null");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Pobierz rolę z obiektu Uzytkownik
        String rola = uzytkownik.getRola().name();

        // Sprawdzenie, czy rola nie jest pusta (dobra praktyka)
        if (rola == null || rola.trim().isEmpty()) {
            // Możesz tu rzucić wyjątek lub zwrócić pustą kolekcję,
            // ale jeśli zakładasz, że każdy użytkownik MUSI mieć rolę,
            // lepiej upewnić się, że jest ona ustawiana przy tworzeniu.
            // Na razie zwrócimy pustą kolekcję dla bezpieczeństwa.
            System.err.println("OSTRZEŻENIE: Użytkownik " + getUsername() + " nie ma przypisanej roli!");
            return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
            // Alternatywnie, jeśli chcesz domyślną rolę w razie braku:
            // return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // Utwórz GrantedAuthority na podstawie roli z prefiksem ROLE_
        // Spring Security często oczekuje tego prefiksu.
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rola.toUpperCase()); // Dobrze jest używać wielkich liter dla ról

        // Zwróć kolekcję zawierającą tę jedną rolę
        return Collections.singleton(authority);
    }


    @Override
    public String getPassword() {
        return uzytkownik.getHaslo();
    }

    @Override
    public String getUsername() {
        return uzytkownik.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
