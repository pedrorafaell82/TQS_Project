package tqs.soundshop.entities;

import tqs.soundshop.entities.Booking;
import tqs.soundshop.entities.Gear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // later: method to check overlaps
    List<Booking> findByEquipmentAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
            Gear gear, LocalDate start, LocalDate end);
}