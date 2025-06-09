package zarzadzanieFinansami.wyjątki;

public class DaneNieZnalesionoExeption extends RuntimeException{
    public DaneNieZnalesionoExeption(String informacja) {
        super(informacja);
    }
    public DaneNieZnalesionoExeption(String informacja, Throwable przyczyna) {
        super(informacja, przyczyna);
    }
}
