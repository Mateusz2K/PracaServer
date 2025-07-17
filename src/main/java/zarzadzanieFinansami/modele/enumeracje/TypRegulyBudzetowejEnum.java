package zarzadzanieFinansami.modele.enumeracje;

public enum TypRegulyBudzetowejEnum {
    PROCENTOWA, // Np. 50% na potrzeby, 30% na zachcianki, 20% na inwestycje
    KWOTOWA,    // Np. 3000 zł na potrzeby, 1500 zł na zachcianki, 1000 zł na inwestycje
    BRAK        // Użytkownik sam definiuje wszystkie pozycje budżetu
}