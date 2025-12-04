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

    public BookingDto createBooking(Long gearId, CreateBookingRequest request) {
        Gear gear = gearRepository.findById(gearId)
                .orElseThrow(() -> new NotFoundException("Instrument " + gearId + " not found"));

        User renter = userRepository.findById(request.renterId())
                .orElseThrow(() -> new NotFoundException("Renter " + request.renterId() + " not found"));

        LocalDate start = request.startDate();
        LocalDate end = request.endDate();

        if (end.isBefore(start)) {
            throw new BadRequestException("End date must be after start date");
        }

        var overlaps = bookingRepository
                .findByGearAndEndDateGreaterThanEqualAndStartDateLessThanEqual(gear, start, end);

        if (!overlaps.isEmpty()) {
            throw new BadRequestException("Instrument is not available in that period");
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
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

    public List<BookingDto> listBookingsForRenter(Long renterId) {
        return bookingRepository.findByRenter_Id(renterId).stream()
                .map(this::toDto)
                .toList();
    }

    public BookingDto updateStatus(Long id, Booking.Status status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking " + id + " not found"));
        booking.setStatus(status);
        return toDto(bookingRepository.save(booking));
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
