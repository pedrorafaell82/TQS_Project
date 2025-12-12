package tqs.soundshop.dto;

import org.junit.jupiter.api.Test;
import tqs.soundshop.entities.Booking;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateBookingStatusRequestTest {

    @Test
    void recordStoresStatus() {
        UpdateBookingStatusRequest request = new UpdateBookingStatusRequest(Booking.Status.CONFIRMED);

        assertThat(request.status()).isEqualTo(Booking.Status.CONFIRMED);
    }
}
