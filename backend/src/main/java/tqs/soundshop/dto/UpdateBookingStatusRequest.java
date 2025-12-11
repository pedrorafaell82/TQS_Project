package tqs.soundshop.dto;

import jakarta.validation.constraints.NotNull;
import tqs.soundshop.entities.Booking;

public record UpdateBookingStatusRequest(
        @NotNull Booking.Status status
) {}
