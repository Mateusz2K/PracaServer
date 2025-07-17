package zarzadzanieFinansami.JWT; // Lub zarzadzanieFinansami.konfiguracja

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component // Ważne, aby Spring mógł to wstrzyknąć
public class JwtAutoryzacjaEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAutoryzacjaEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        logger.error("Unauthorized error: {}", authException.getMessage());
        // Możesz tu wysłać bardziej szczegółową odpowiedź JSON
        // np. response.setContentType("application/json");
        // response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized - " + authException.getMessage());
    }
}