package zarzadzanieFinansami.wyjątki;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict
public class DuplikatException extends RuntimeException {
    public DuplikatException(String message) {
        super(message);
    }
}
