package tqs.soundshop.dto;

import java.time.LocalDate;

public record CreateBookingRequest(
        Long renterId,
        LocalDate startDate,
        LocalDate endDate
) {}
