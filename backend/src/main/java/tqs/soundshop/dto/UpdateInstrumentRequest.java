package tqs.soundshop.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.Positive;

public record UpdateInstrumentRequest(
        String name,
        String description,
        String category,
        String brand,
        String conditionGrade,
        @Positive BigDecimal dailyPrice
) {}
