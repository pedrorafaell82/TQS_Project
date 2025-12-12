package tqs.soundshop.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}
