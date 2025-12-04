package tqs.soundshop.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingDto(
        Long id,
        Long instrumentId,
        Long renterId,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        BigDecimal totalPrice
) {}
