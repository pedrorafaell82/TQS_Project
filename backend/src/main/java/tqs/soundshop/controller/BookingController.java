package tqs.soundshop.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tqs.soundshop.dto.BookingDto;
import tqs.soundshop.dto.CreateBookingRequest;
import tqs.soundshop.dto.UpdateBookingStatusRequest;
import tqs.soundshop.entities.Booking;
import tqs.soundshop.service.BookingService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // renter-facing: create booking for an instrument
    @PostMapping("/instruments/{instrumentId}/bookings")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<BookingDto> createBooking(
            @PathVariable Long instrumentId,
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        String renterEmail = user.getUsername();
        BookingDto created = bookingService.createBooking(instrumentId, renterEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/bookings/{id}")
    public BookingDto getBooking(@PathVariable Long id) {
        return bookingService.getBooking(id);
    }

    // renter dashboard: bookings for current user
    @GetMapping("/users/me/bookings")
    @PreAuthorize("hasRole('RENTER')")
    public List<BookingDto> listForCurrentRenter(@AuthenticationPrincipal UserDetails user) {
        String renterEmail = user.getUsername();
        return bookingService.listBookingsForRenterEmail(renterEmail);
    }

    // owner dashboard: bookings for all instruments owned by current owner
    @GetMapping("/owners/me/bookings")
    @PreAuthorize("hasRole('OWNER')")
    public List<BookingDto> listForCurrentOwner(@AuthenticationPrincipal UserDetails user) {
        String ownerEmail = user.getUsername();
        return bookingService.listBookingsForOwnerEmail(ownerEmail);
    }

    // owner: bookings for a single instrument
    @GetMapping("/instruments/{instrumentId}/bookings")
    @PreAuthorize("hasRole('OWNER')")
    public List<BookingDto> listForInstrument(@PathVariable Long instrumentId) {
        return bookingService.listBookingsForInstrument(instrumentId);
    }

    // owner/admin: update booking status (confirm, complete, etc.)
    @PatchMapping("/bookings/{id}/status")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public BookingDto updateStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody UpdateBookingStatusRequest request
    ) {
        String ownerEmail = user.getUsername();
        Booking.Status status = request.status();
        return bookingService.updateStatusAsOwner(id, ownerEmail, status);
    }

    // renter: cancel own booking
    @PatchMapping("/bookings/{id}/cancel")
    @PreAuthorize("hasRole('RENTER')")
    public BookingDto cancelBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        String renterEmail = user.getUsername();
        return bookingService.cancelAsRenter(id, renterEmail);
    }

    // renter: simulate payment
    @PostMapping("/bookings/{id}/pay")
    @PreAuthorize("hasRole('RENTER')")
    public BookingDto payForBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        String renterEmail = user.getUsername();
        return bookingService.pay(id, renterEmail);
    }

    // availability check for an instrument in a date range
    @GetMapping("/instruments/{instrumentId}/availability")
    public Map<String, Boolean> checkAvailability(
            @PathVariable Long instrumentId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        boolean available = bookingService.isInstrumentAvailable(instrumentId, startDate, endDate);
        return Map.of("available", available);
    }
}
