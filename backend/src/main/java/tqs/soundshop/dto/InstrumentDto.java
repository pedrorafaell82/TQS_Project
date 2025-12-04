package tqs.soundshop.dto;

import java.math.BigDecimal;

public record InstrumentDto(
        Long id,
        String name,
        String description,
        String category,
        String brand,
        String conditionGrade,
        BigDecimal dailyPrice
) {}
