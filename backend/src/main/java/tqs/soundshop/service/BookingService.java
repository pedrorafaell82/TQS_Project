package tqs.soundshop.service;

import org.springframework.stereotype.Service;
import tqs.soundshop.dto.BookingDto;
import tqs.soundshop.dto.CreateBookingRequest;
import tqs.soundshop.entities.Booking;
import tqs.soundshop.entities.BookingRepository;
import tqs.soundshop.entities.Gear;
import tqs.soundshop.entities.GearRepository;
import tqs.soundshop.entities.User;
import tqs.soundshop.entities.UserRepository;
import tqs.soundshop.exception.BadRequestException;
import tqs.soundshop.exception.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final GearRepository gearRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          GearRepository gearRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.gearRepository = gearRepository;
        this.userRepository = userRepository;
    }

    // renter-facing booking creation using authenticated renter email
    public BookingDto createBooking(Long gearId, String renterEmail, CreateBookingRequest request) {
        Gear gear = gearRepository.findById(gearId)
                .orElseThrow(() -> new NotFoundException("Instrument " + gearId + " not found"));

        if (!gear.isActive()) {
            throw new BadRequestException("Instrument is not available for booking");
        }

        User renter = userRepository.findByEmail(renterEmail)
                .orElseThrow(() -> new NotFoundException("Renter with email " + renterEmail + " not found"));

        LocalDate start = request.startDate();
        LocalDate end = request.endDate();

        if (start == null || end == null) {
            throw new BadRequestException("Start date and end date are required");
        }

        if (end.isBefore(start)) {
            throw new BadRequestException("End date must be after start date");
        }

        // optional: disallow past start dates
        if (start.isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date must be today or in the future");
        }

        var overlaps = bookingRepository
                .findByGearAndEndDateGreaterThanEqualAndStartDateLessThanEqual(gear, start, end);

        if (!overlaps.isEmpty()) {
            throw new BadRequestException("Instrument is not available in that period");
        }

        long days = ChronoUnit.DAYS.between(start, end) + 1;
        BigDecimal totalPrice = gear.getDailyPrice().multiply(BigDecimal.valueOf(days));

        Booking booking = new Booking();
        booking.setGear(gear);
        booking.setRenter(renter);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setStatus(Booking.Status.PENDING_PAYMENT);
        booking.setTotalPrice(totalPrice);

        Booking saved = bookingRepository.save(booking);
        return toDto(saved);
    }

    public BookingDto getBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking " + id + " not found"));
        return toDto(booking);
    }

    // renter dashboard: bookings for authenticated renter
    public List<BookingDto> listBookingsForRenterEmail(String renterEmail) {
        return bookingRepository.findByRenter_Email(renterEmail).stream()
                .map(this::toDto)
                .toList();
    }

    // owner dashboard: bookings for all instruments owned by this owner
    public List<BookingDto> listBookingsForOwnerEmail(String ownerEmail) {
        return bookingRepository.findByGear_Owner_Email(ownerEmail).stream()
                .map(this::toDto)
                .toList();
    }

    // per-instrument booking list (owner-side)
    public List<BookingDto> listBookingsForInstrument(Long instrumentId) {
        return bookingRepository.findByGear_Id(instrumentId).stream()
                .map(this::toDto)
                .toList();
    }

    // owner/admin status update with basic transition rules
    public BookingDto updateStatusAsOwner(Long id, String ownerEmail, Booking.Status newStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking " + id + " not found"));

        // check ownership
        if (booking.getGear() == null ||
            booking.getGear().getOwner() == null ||
            !ownerEmail.equalsIgnoreCase(booking.getGear().getOwner().getEmail())) {
            throw new BadRequestException("You are not allowed to modify this booking");
        }

        Booking.Status current = booking.getStatus();

        // very simple transition rules
        switch (current) {
            case PENDING_PAYMENT -> {
                if (newStatus != Booking.Status.CONFIRMED && newStatus != Booking.Status.CANCELLED) {
                    throw new BadRequestException("Invalid status transition from " + current);
                }
            }
            case CONFIRMED -> {
                if (newStatus != Booking.Status.COMPLETED && newStatus != Booking.Status.CANCELLED) {
                    throw new BadRequestException("Invalid status transition from " + current);
                }
            }
            case CANCELLED, COMPLETED -> {
                throw new BadRequestException("Cannot change status from " + current);
            }
        }

        booking.setStatus(newStatus);
        return toDto(bookingRepository.save(booking));
    }

    // renter-side cancel
    public BookingDto cancelAsRenter(Long id, String renterEmail) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking " + id + " not found"));

        if (!renterEmail.equalsIgnoreCase(booking.getRenter().getEmail())) {
            throw new BadRequestException("You are not allowed to cancel this booking");
        }

        LocalDate today = LocalDate.now();
        if (!today.isBefore(booking.getStartDate())) {
            throw new BadRequestException("Cannot cancel bookings that have started or passed");
        }

        if (booking.getStatus() == Booking.Status.CANCELLED ||
            booking.getStatus() == Booking.Status.COMPLETED) {
            throw new BadRequestException("Booking is already " + booking.getStatus());
        }

        booking.setStatus(Booking.Status.CANCELLED);
        return toDto(bookingRepository.save(booking));
    }

    // simulated payment: PENDING_PAYMENT -> CONFIRMED
    public BookingDto pay(Long id, String renterEmail) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking " + id + " not found"));

        if (!renterEmail.equalsIgnoreCase(booking.getRenter().getEmail())) {
            throw new BadRequestException("You are not allowed to pay for this booking");
        }

        if (booking.getStatus() != Booking.Status.PENDING_PAYMENT) {
            throw new BadRequestException("Booking is not pending payment");
        }

        booking.setStatus(Booking.Status.CONFIRMED);
        // here you could set a fake paymentReference, timestamp, etc.
        return toDto(bookingRepository.save(booking));
    }

    // availability check method (for instruments/{id}/availability endpoint)
    public boolean isInstrumentAvailable(Long gearId, LocalDate start, LocalDate end) {
        Gear gear = gearRepository.findById(gearId)
                .orElseThrow(() -> new NotFoundException("Instrument " + gearId + " not found"));

        if (!gear.isActive()) {
            return false;
        }

        if (start == null || end == null || end.isBefore(start)) {
            return false;
        }

        var overlaps = bookingRepository
                .findByGearAndEndDateGreaterThanEqualAndStartDateLessThanEqual(gear, start, end);
        return overlaps.isEmpty();
    }

    private BookingDto toDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getGear().getId(),
                booking.getRenter().getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus().name(),
                booking.getTotalPrice()
        );
    }
}
