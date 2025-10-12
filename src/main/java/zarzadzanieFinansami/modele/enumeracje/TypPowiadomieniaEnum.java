package zarzadzanieFinansami.modele.enumeracje;

public enum TypPowiadomieniaEnum {
    // Krytyczne / Ostrzeżenia
    PRZEKROCZENIE_BUDZETU("Przekroczenie Budżetu"),
    NISKI_STAN_KONTA("Niski Stan Konta"),

    // Sukces / Pozytywne
    OSIAGNIECIE_CELU("Osiągnięcie Celu"),
    WYSOKI_STAN_KONTA("Wysoki Stan Konta"),

    // Informacyjne
    WIADOMOSC_SYSTEMOWA("Wiadomość Systemowa"),
    PRZYPOMNIENIE("Przypomnienie o dodaniu transakcji z dzisiejszego dnia!");

    private final String nazwaWyswietlana;

    TypPowiadomieniaEnum(String nazwaWyswietlana) {
        this.nazwaWyswietlana = nazwaWyswietlana;
    }

    public String getNazwaWyswietlana() {
        return nazwaWyswietlana;
    }
}