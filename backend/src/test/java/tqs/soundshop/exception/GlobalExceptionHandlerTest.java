package tqs.soundshop.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404AndBody() {
        NotFoundException ex = new NotFoundException("Not here");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        Map<String, Object> body = response.getBody();

        assertThat(body)
                .isNotNull()
                .containsEntry("status", 404)
                .containsEntry("error", "Not Found")
                .containsEntry("message", "Not here");

        assertThat(body.get("timestamp")).isNotNull();
    }

    @Test
    void handleBadRequest_returns400AndBody() {
        BadRequestException ex = new BadRequestException("Bad stuff");

        ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        Map<String, Object> body = response.getBody();

        assertThat(body)
                .isNotNull()
                .containsEntry("status", 400)
                .containsEntry("error", "Bad Request")
                .containsEntry("message", "Bad stuff");

        assertThat(body.get("timestamp")).isNotNull();
    }
}
