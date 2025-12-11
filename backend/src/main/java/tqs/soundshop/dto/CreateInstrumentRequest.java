package tqs.soundshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateInstrumentRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String category,
        @NotBlank String brand,
        @NotBlank String conditionGrade,
        @NotNull @Positive BigDecimal dailyPrice
) {}
