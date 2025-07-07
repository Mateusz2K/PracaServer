package zarzadzanieFinansami.konfikuracja;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import zarzadzanieFinansami.magazyn.MagazynUzytkownika;
import zarzadzanieFinansami.modele.Uzytkownik;
import zarzadzanieFinansami.modele.enumeracje.RolaEnum;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final MagazynUzytkownika magazynUzytkownika;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(MagazynUzytkownika magazynUzytkownika, PasswordEncoder passwordEncoder) {
        this.magazynUzytkownika = magazynUzytkownika;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        String adminUsername = "mat"; // Nazwa admina
        if (magazynUzytkownika.findByNazwa(adminUsername) == null) { // Zakładając, że masz metodę findByName
            Uzytkownik admin = new Uzytkownik();
            admin.setName(adminUsername);
            admin.setHaslo(passwordEncoder.encode("Lucy")); // Ustaw silne hasło!
            admin.setRola(RolaEnum.ADMIN);
            magazynUzytkownika.save(admin);
            System.out.println("Utworzono konto administratora.");
        }
    }
}
