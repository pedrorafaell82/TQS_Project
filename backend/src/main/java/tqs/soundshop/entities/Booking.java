package tqs.soundshop.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Gear gear;

    @ManyToOne(optional = false)
    private User user;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING_PAYMENT;

    private BigDecimal totalPrice;

    private Instant createdAt = Instant.now();

    public enum Status {
        PENDING_PAYMENT, CONFIRMED, CANCELLED, COMPLETED
    }

    // getters/setters
}
