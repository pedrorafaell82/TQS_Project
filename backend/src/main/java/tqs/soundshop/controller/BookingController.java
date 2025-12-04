package tqs.soundshop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.soundshop.dto.BookingDto;
import tqs.soundshop.dto.CreateBookingRequest;
import tqs.soundshop.dto.UpdateBookingStatusRequest;
import tqs.soundshop.entities.Booking;
import tqs.soundshop.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/instruments/{instrumentId}/bookings")
    public ResponseEntity<BookingDto> createBooking(
            @PathVariable Long instrumentId,
            @RequestBody CreateBookingRequest request
    ) {
        BookingDto created = bookingService.createBooking(instrumentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/bookings/{id}")
    public BookingDto getBooking(@PathVariable Long id) {
        return bookingService.getBooking(id);
    }

    @GetMapping("/users/{renterId}/bookings")
    public List<BookingDto> listForRenter(@PathVariable Long renterId) {
        return bookingService.listBookingsForRenter(renterId);
    }

    @PatchMapping("/bookings/{id}/status")
    public BookingDto updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateBookingStatusRequest request
    ) {
        Booking.Status status = Booking.Status.valueOf(request.status());
        return bookingService.updateStatus(id, status);
    }
}
