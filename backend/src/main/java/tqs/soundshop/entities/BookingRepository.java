package tqs.soundshop.entities;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Overlapping bookings for availability check
    List<Booking> findByGearAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
            Gear gear, LocalDate start, LocalDate end);

    // Renter dashboards
    List<Booking> findByRenter_Id(Long renterId);
    List<Booking> findByRenter_Email(String email);

    // Owner dashboards
    List<Booking> findByGear_Owner_Email(String ownerEmail);

    // Per-instrument bookings
    List<Booking> findByGear_Id(Long gearId);
}
