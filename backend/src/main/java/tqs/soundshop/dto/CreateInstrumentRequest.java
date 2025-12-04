package tqs.soundshop.dto;

import java.math.BigDecimal;

public record CreateInstrumentRequest(
        String name,
        String description,
        String category,
        String brand,
        String conditionGrade,
        BigDecimal dailyPrice,
        Long ownerId 
) {}
