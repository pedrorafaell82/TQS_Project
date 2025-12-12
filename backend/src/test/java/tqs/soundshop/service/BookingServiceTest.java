package tqs.soundshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.soundshop.dto.BookingDto;
import tqs.soundshop.dto.CreateBookingRequest;
import tqs.soundshop.entities.*;
import tqs.soundshop.exception.BadRequestException;
import tqs.soundshop.exception.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private GearRepository gearRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private Gear activeGear;
    private User renter;

    @BeforeEach
    void setUp() {
        activeGear = new Gear();
        activeGear.setId(1L);
        activeGear.setName("Guitar");
        activeGear.setDailyPrice(new BigDecimal("10.00"));
        activeGear.setActive(true);

        renter = new User();
        renter.setId(5L);
        renter.setEmail("renter@example.com");
    }

    // ---------- createBooking ----------

    @Test
    void createBooking_whenDataIsValid_createsBookingAndReturnsDto() {
        Long gearId = 1L;
        String renterEmail = renter.getEmail();
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);

        CreateBookingRequest request = new CreateBookingRequest(start, end);

        when(gearRepository.findById(gearId)).thenReturn(Optional.of(activeGear));
        when(userRepository.findByEmail(renterEmail)).thenReturn(Optional.of(renter));
        when(bookingRepository
                .findByGearAndEndDateGreaterThanEqualAndStartDateLessThanEqual(activeGear, start, end))
                .thenReturn(List.of());

        // we want to return the same booking but with an id
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(42L);
            return b;
        });

        BookingDto result = bookingService.createBooking(gearId, renterEmail, request);

        // verify that repository.save was called with correct data
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        Booking saved = bookingCaptor.getValue();

        long expectedDays = 3; // start, start+1, start+2
        BigDecimal expectedTotal = activeGear.getDailyPrice().multiply(BigDecimal.valueOf(expectedDays));

        assertThat(saved.getGear()).isEqualTo(activeGear);
        assertThat(saved.getRenter()).isEqualTo(renter);
        assertThat(saved.getStartDate()).isEqualTo(start);
        assertThat(saved.getEndDate()).isEqualTo(end);
        assertThat(saved.getStatus()).isEqualTo(Booking.Status.PENDING_PAYMENT);
        assertThat(saved.getTotalPrice()).isEqualByComparingTo(expectedTotal);

        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.instrumentId()).isEqualTo(activeGear.getId());
        assertThat(result.renterId()).isEqualTo(renter.getId());
        assertThat(result.totalPrice()).isEqualByComparingTo(expectedTotal);
    }

    @Test
    void createBooking_whenGearNotFound_throwsNotFoundException() {
        Long gearId = 99L;
        String renterEmail = renter.getEmail();
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(1);

        when(gearRepository.findById(gearId)).thenReturn(Optional.empty());
        
        CreateBookingRequest request = new CreateBookingRequest(start, end);

        assertThatThrownBy(() ->
                bookingService.createBooking(gearId, renterEmail, request)
        )
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Instrument " + gearId + " not found");
    }

    @Test
    void createBooking_whenGearInactive_throwsBadRequest() {
        Long gearId = 1L;
        String renterEmail = renter.getEmail();
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(1);

        activeGear.setActive(false);

        when(gearRepository.findById(gearId)).thenReturn(Optional.of(activeGear));

        CreateBookingRequest request = new CreateBookingRequest(start, end);

        assertThatThrownBy(() ->
                bookingService.createBooking(gearId, renterEmail, request)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Instrument is not available for booking");
    }

    @Test
    void createBooking_whenRenterNotFound_throwsNotFoundException() {
        Long gearId = 1L;
        String renterEmail = renter.getEmail();
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(1);

        when(gearRepository.findById(gearId)).thenReturn(Optional.of(activeGear));
        when(userRepository.findByEmail(renterEmail)).thenReturn(Optional.empty());

        CreateBookingRequest request = new CreateBookingRequest(start, end);

        assertThatThrownBy(() ->
                bookingService.createBooking(gearId, renterEmail, request)
        )
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Renter with email " + renterEmail + " not found");
    }

    @Test
    void createBooking_whenEndBeforeStart_throwsBadRequest() {
        Long gearId = 1L;
        String renterEmail = renter.getEmail();
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = start.minusDays(1);

        when(gearRepository.findById(gearId)).thenReturn(Optional.of(activeGear));
        when(userRepository.findByEmail(renterEmail)).thenReturn(Optional.of(renter));

        CreateBookingRequest request = new CreateBookingRequest(start, end);

        assertThatThrownBy(() ->
                bookingService.createBooking(gearId, renterEmail, request)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("End date must be after start date");
    }

    @Test
    void createBooking_whenStartInPast_throwsBadRequest() {
        Long gearId = 1L;
        String renterEmail = renter.getEmail();
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(1);

        when(gearRepository.findById(gearId)).thenReturn(Optional.of(activeGear));
        when(userRepository.findByEmail(renterEmail)).thenReturn(Optional.of(renter));

        CreateBookingRequest request = new CreateBookingRequest(start, end);

        assertThatThrownBy(() ->
                bookingService.createBooking(gearId, renterEmail, request)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Start date must be today or in the future");
    }

    @Test
    void createBooking_whenOverlapsExistingBooking_throwsBadRequest() {
        Long gearId = 1L;
        String renterEmail = renter.getEmail();
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);

        when(gearRepository.findById(gearId)).thenReturn(Optional.of(activeGear));
        when(userRepository.findByEmail(renterEmail)).thenReturn(Optional.of(renter));

        Booking existing = new Booking();
        existing.setGear(activeGear);
        existing.setStartDate(start.plusDays(1));
        existing.setEndDate(end.plusDays(1));

        when(bookingRepository
                .findByGearAndEndDateGreaterThanEqualAndStartDateLessThanEqual(activeGear, start, end))
                .thenReturn(List.of(existing));

        CreateBookingRequest request = new CreateBookingRequest(start, end);
        
        assertThatThrownBy(() ->
                bookingService.createBooking(gearId, renterEmail, request)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Instrument is not available in that period");
    }

    // ---------- isInstrumentAvailable ----------

    @Test
    void isInstrumentAvailable_whenNoOverlapsAndActive_returnsTrue() {
        Long gearId = 1L;
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);

        when(gearRepository.findById(gearId)).thenReturn(Optional.of(activeGear));
        when(bookingRepository
                .findByGearAndEndDateGreaterThanEqualAndStartDateLessThanEqual(activeGear, start, end))
                .thenReturn(List.of());

        boolean available = bookingService.isInstrumentAvailable(gearId, start, end);

        assertThat(available).isTrue();
    }

    @Test
    void isInstrumentAvailable_whenGearInactive_returnsFalse() {
        Long gearId = 1L;
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);

        activeGear.setActive(false);
        when(gearRepository.findById(gearId)).thenReturn(Optional.of(activeGear));

        boolean available = bookingService.isInstrumentAvailable(gearId, start, end);

        assertThat(available).isFalse();
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void isInstrumentAvailable_whenInvalidDates_returnsFalse() {
        Long gearId = 1L;
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = start.minusDays(1);

        when(gearRepository.findById(gearId)).thenReturn(Optional.of(activeGear));

        boolean available = bookingService.isInstrumentAvailable(gearId, start, end);

        assertThat(available).isFalse();
        verifyNoInteractions(bookingRepository);
    }

        // ---------- getBooking ----------

    @Test
    void getBooking_whenExists_returnsDto() {
        Long bookingId = 10L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setGear(activeGear);
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.of(2030, 1, 1));
        booking.setEndDate(LocalDate.of(2030, 1, 3));
        booking.setStatus(Booking.Status.CONFIRMED);
        booking.setTotalPrice(new BigDecimal("30.00"));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingDto dto = bookingService.getBooking(bookingId);

        assertThat(dto.id()).isEqualTo(bookingId);
        assertThat(dto.instrumentId()).isEqualTo(activeGear.getId());
        assertThat(dto.renterId()).isEqualTo(renter.getId());
        assertThat(dto.startDate()).isEqualTo(booking.getStartDate());
        assertThat(dto.endDate()).isEqualTo(booking.getEndDate());
        assertThat(dto.status()).isEqualTo("CONFIRMED");
        assertThat(dto.totalPrice()).isEqualByComparingTo("30.00");
    }

    @Test
    void getBooking_whenNotFound_throwsNotFound() {
        Long bookingId = 999L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBooking(bookingId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking " + bookingId + " not found");
    }

        // ---------- listing methods ----------

    @Test
    void listBookingsForRenterEmail_returnsMappedDtos() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setGear(activeGear);
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.of(2030, 1, 1));
        booking.setEndDate(LocalDate.of(2030, 1, 2));
        booking.setStatus(Booking.Status.PENDING_PAYMENT);
        booking.setTotalPrice(new BigDecimal("20.00"));

        when(bookingRepository.findByRenter_Email(renter.getEmail()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.listBookingsForRenterEmail(renter.getEmail());

        assertThat(result).hasSize(1);
        BookingDto dto = result.get(0);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.instrumentId()).isEqualTo(activeGear.getId());
        assertThat(dto.renterId()).isEqualTo(renter.getId());
    }

    @Test
    void listBookingsForOwnerEmail_returnsMappedDtos() {
        User owner = new User();
        owner.setId(99L);
        owner.setEmail("owner@example.com");
        activeGear.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(2L);
        booking.setGear(activeGear);
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.of(2030, 2, 1));
        booking.setEndDate(LocalDate.of(2030, 2, 3));
        booking.setStatus(Booking.Status.CONFIRMED);
        booking.setTotalPrice(new BigDecimal("30.00"));

        when(bookingRepository.findByGear_Owner_Email(owner.getEmail()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.listBookingsForOwnerEmail(owner.getEmail());

        assertThat(result).hasSize(1);
        BookingDto dto = result.get(0);
        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.instrumentId()).isEqualTo(activeGear.getId());
    }

    @Test
    void listBookingsForInstrument_returnsMappedDtos() {
        Long instrumentId = activeGear.getId();

        Booking booking = new Booking();
        booking.setId(3L);
        booking.setGear(activeGear);
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.of(2030, 3, 1));
        booking.setEndDate(LocalDate.of(2030, 3, 2));
        booking.setStatus(Booking.Status.PENDING_PAYMENT);
        booking.setTotalPrice(new BigDecimal("20.00"));

        when(bookingRepository.findByGear_Id(instrumentId))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.listBookingsForInstrument(instrumentId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(3L);
    }

        // ---------- updateStatusAsOwner ----------

    @Test
    void updateStatusAsOwner_whenOwnerMatchesAndTransitionValid_updatesStatus() {
        String ownerEmail = "owner@example.com";

        User owner = new User();
        owner.setId(100L);
        owner.setEmail(ownerEmail);

        Gear gear = new Gear();
        gear.setId(10L);
        gear.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(5L);
        booking.setGear(gear);
        booking.setRenter(renter);
        booking.setStatus(Booking.Status.PENDING_PAYMENT);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingDto dto = bookingService.updateStatusAsOwner(5L, ownerEmail, Booking.Status.CONFIRMED);

        assertThat(dto.status()).isEqualTo("CONFIRMED");
        assertThat(booking.getStatus()).isEqualTo(Booking.Status.CONFIRMED);
    }

    @Test
    void updateStatusAsOwner_whenOwnerDoesNotMatch_throwsBadRequest() {
        String ownerEmail = "correct-owner@example.com";

        User owner = new User();
        owner.setEmail(ownerEmail);

        Gear gear = new Gear();
        gear.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(6L);
        booking.setGear(gear);
        booking.setRenter(renter);
        booking.setStatus(Booking.Status.PENDING_PAYMENT);

        when(bookingRepository.findById(6L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() ->
                bookingService.updateStatusAsOwner(6L, "other-owner@example.com", Booking.Status.CONFIRMED)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("You are not allowed to modify this booking");
    }

    @Test
    void updateStatusAsOwner_whenTransitionInvalid_throwsBadRequest() {
        String ownerEmail = "owner@example.com";

        User owner = new User();
        owner.setEmail(ownerEmail);

        Gear gear = new Gear();
        gear.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(7L);
        booking.setGear(gear);
        booking.setRenter(renter);
        booking.setStatus(Booking.Status.PENDING_PAYMENT);

        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() ->
                bookingService.updateStatusAsOwner(7L, ownerEmail, Booking.Status.COMPLETED)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid status transition from " + Booking.Status.PENDING_PAYMENT);
    }

    @Test
    void updateStatusAsOwner_whenBookingNotFound_throwsNotFound() {
        Long bookingId = 123L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                bookingService.updateStatusAsOwner(bookingId, "owner@example.com", Booking.Status.CONFIRMED)
        )
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking " + bookingId + " not found");
    }

        // ---------- cancelAsRenter ----------

    @Test
    void cancelAsRenter_whenDataValid_setsStatusCancelled() {
        Long bookingId = 20L;
        String renterEmail = renter.getEmail();

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setGear(activeGear);
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.now().plusDays(5));
        booking.setEndDate(LocalDate.now().plusDays(7));
        booking.setStatus(Booking.Status.PENDING_PAYMENT);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingDto dto = bookingService.cancelAsRenter(bookingId, renterEmail);

        assertThat(dto.status()).isEqualTo("CANCELLED");
        assertThat(booking.getStatus()).isEqualTo(Booking.Status.CANCELLED);
    }

    @Test
    void cancelAsRenter_whenDifferentRenter_throwsBadRequest() {
        Long bookingId = 21L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setGear(activeGear);
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.now().plusDays(5));
        booking.setEndDate(LocalDate.now().plusDays(7));
        booking.setStatus(Booking.Status.PENDING_PAYMENT);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() ->
                bookingService.cancelAsRenter(bookingId, "other@example.com")
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("You are not allowed to cancel this booking");
    }

    @Test
    void cancelAsRenter_whenBookingStartedOrPassed_throwsBadRequest() {
        Long bookingId = 22L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setGear(activeGear);
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.now()); // today or earlier => cannot cancel
        booking.setEndDate(LocalDate.now().plusDays(2));
        booking.setStatus(Booking.Status.PENDING_PAYMENT);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        String email = renter.getEmail();

        assertThatThrownBy(() ->
                bookingService.cancelAsRenter(bookingId, email)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot cancel bookings that have started or passed");
    }

    @Test
    void cancelAsRenter_whenAlreadyCancelled_throwsBadRequest() {
        Long bookingId = 23L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setGear(activeGear);
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.now().plusDays(5));
        booking.setEndDate(LocalDate.now().plusDays(7));
        booking.setStatus(Booking.Status.CANCELLED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        String email = renter.getEmail();

        assertThatThrownBy(() ->
                bookingService.cancelAsRenter(bookingId, email)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Booking is already CANCELLED");
    }

    @Test
    void cancelAsRenter_whenNotFound_throwsNotFound() {
        Long bookingId = 999L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        String email = renter.getEmail();

        assertThatThrownBy(() ->
                bookingService.cancelAsRenter(bookingId, email)
        )
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking " + bookingId + " not found");
    }

        // ---------- pay ----------

    @Test
    void pay_whenRenterMatchesAndStatusPending_setsStatusConfirmed() {
        Long bookingId = 30L;
        String renterEmail = renter.getEmail();

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setGear(activeGear);
        booking.setRenter(renter);
        booking.setStatus(Booking.Status.PENDING_PAYMENT);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingDto dto = bookingService.pay(bookingId, renterEmail);

        assertThat(dto.status()).isEqualTo("CONFIRMED");
        assertThat(booking.getStatus()).isEqualTo(Booking.Status.CONFIRMED);
    }

    @Test
    void pay_whenDifferentRenter_throwsBadRequest() {
        Long bookingId = 31L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setGear(activeGear);
        booking.setRenter(renter);
        booking.setStatus(Booking.Status.PENDING_PAYMENT);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() ->
                bookingService.pay(bookingId, "other@example.com")
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("You are not allowed to pay for this booking");
    }

    @Test
    void pay_whenStatusNotPendingPayment_throwsBadRequest() {
        Long bookingId = 32L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setGear(activeGear);
        booking.setRenter(renter);
        booking.setStatus(Booking.Status.CONFIRMED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        String email = renter.getEmail();

        assertThatThrownBy(() ->
                bookingService.pay(bookingId, email)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Booking is not pending payment");
    }

    @Test
    void pay_whenBookingNotFound_throwsNotFound() {
        Long bookingId = 333L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        String email = renter.getEmail();

        assertThatThrownBy(() ->
                bookingService.pay(bookingId, email)
        )
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking " + bookingId + " not found");
    }

}
