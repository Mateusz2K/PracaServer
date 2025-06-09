package zarzadzanieFinansami.wyjątki;

public class ForbiddenAccessException extends RuntimeException{
    public ForbiddenAccessException(String message) {
        super(message);
    }

}
