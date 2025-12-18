package tqs.soundshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import tqs.soundshop.config.SecurityConfig;
import tqs.soundshop.dto.BookingDto;
import tqs.soundshop.dto.CreateBookingRequest;
import tqs.soundshop.dto.UpdateBookingStatusRequest;
import tqs.soundshop.entities.Booking;
import tqs.soundshop.service.BookingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @WithMockUser(roles = "RENTER", username = "renter@example.com")
    void createBooking_asRenter_returnsCreated() throws Exception {
        LocalDate start = LocalDate.of(2030, 1, 1);
        LocalDate end = LocalDate.of(2030, 1, 3);

        CreateBookingRequest request = new CreateBookingRequest(start, end);

        BookingDto dto = new BookingDto(
                1L, 10L, 5L, start, end, "PENDING_PAYMENT", new BigDecimal("30.00")
        );

        when(bookingService.createBooking(eq(10L), eq("renter@example.com"), any(CreateBookingRequest.class)))
                .thenReturn(dto);

        mockMvc.perform(post("/api/instruments/{instrumentId}/bookings", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.instrumentId").value(10L));

        verify(bookingService).createBooking(eq(10L), eq("renter@example.com"), any(CreateBookingRequest.class));
    }

    @Test
    void getBooking_returnsBooking() throws Exception {
        LocalDate start = LocalDate.of(2030, 1, 1);
        LocalDate end = LocalDate.of(2030, 1, 3);

        BookingDto dto = new BookingDto(
                2L, 10L, 5L, start, end, "CONFIRMED", new BigDecimal("30.00")
        );

        when(bookingService.getBooking(2L)).thenReturn(dto);

        mockMvc.perform(get("/api/bookings/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).getBooking(2L);
    }

    @Test
    @WithMockUser(roles = "RENTER", username = "renter@example.com")
    void listForCurrentRenter_returnsList() throws Exception {
        BookingDto dto = new BookingDto(
                3L, 10L, 5L,
                LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 1, 3),
                "PENDING_PAYMENT",
                new BigDecimal("30.00")
        );

        when(bookingService.listBookingsForRenterEmail("renter@example.com"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/users/me/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3L));

        verify(bookingService).listBookingsForRenterEmail("renter@example.com");
    }

    @Test
    @WithMockUser(roles = "OWNER", username = "owner@example.com")
    void listForCurrentOwner_returnsList() throws Exception {
        BookingDto dto = new BookingDto(
                4L, 10L, 5L,
                LocalDate.of(2030, 2, 1),
                LocalDate.of(2030, 2, 3),
                "CONFIRMED",
                new BigDecimal("40.00")
        );

        when(bookingService.listBookingsForOwnerEmail("owner@example.com"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/owners/me/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4L));

        verify(bookingService).listBookingsForOwnerEmail("owner@example.com");
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void listForInstrument_returnsList() throws Exception {
        BookingDto dto = new BookingDto(
                5L, 99L, 5L,
                LocalDate.of(2030, 3, 1),
                LocalDate.of(2030, 3, 3),
                "CONFIRMED",
                new BigDecimal("50.00")
        );

        when(bookingService.listBookingsForInstrument(99L))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/instruments/{instrumentId}/bookings", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5L));

        verify(bookingService).listBookingsForInstrument(99L);
    }

    @Test
    @WithMockUser(roles = "OWNER", username = "owner@example.com")
    void updateStatus_asOwner_returnsUpdated() throws Exception {
        UpdateBookingStatusRequest request = new UpdateBookingStatusRequest(Booking.Status.CONFIRMED);

        BookingDto dto = new BookingDto(
                6L, 10L, 5L,
                LocalDate.of(2030, 4, 1),
                LocalDate.of(2030, 4, 3),
                "CONFIRMED",
                new BigDecimal("60.00")
        );

        when(bookingService.updateStatusAsOwner(6L, "owner@example.com", Booking.Status.CONFIRMED))
                .thenReturn(dto);

        mockMvc.perform(patch("/api/bookings/{id}/status", 6L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).updateStatusAsOwner(6L, "owner@example.com", Booking.Status.CONFIRMED);
    }

    @Test
    @WithMockUser(roles = "RENTER", username = "renter@example.com")
    void cancelBooking_asRenter_returnsUpdated() throws Exception {
        BookingDto dto = new BookingDto(
                7L, 10L, 5L,
                LocalDate.of(2030, 5, 1),
                LocalDate.of(2030, 5, 3),
                "CANCELLED",
                new BigDecimal("0.00")
        );

        when(bookingService.cancelAsRenter(7L, "renter@example.com")).thenReturn(dto);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(bookingService).cancelAsRenter(7L, "renter@example.com");
    }

    @Test
    @WithMockUser(roles = "RENTER", username = "renter@example.com")
    void payForBooking_asRenter_returnsUpdated() throws Exception {
        BookingDto dto = new BookingDto(
                8L, 10L, 5L,
                LocalDate.of(2030, 6, 1),
                LocalDate.of(2030, 6, 3),
                "CONFIRMED",
                new BigDecimal("70.00")
        );

        when(bookingService.pay(8L, "renter@example.com")).thenReturn(dto);

        mockMvc.perform(post("/api/bookings/{id}/pay", 8L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).pay(8L, "renter@example.com");
    }

    @Test
    void checkAvailability_returnsAvailableFlag() throws Exception {
        LocalDate start = LocalDate.of(2030, 7, 1);
        LocalDate end = LocalDate.of(2030, 7, 3);

        when(bookingService.isInstrumentAvailable(50L, start, end)).thenReturn(true);

        mockMvc.perform(get("/api/instruments/{instrumentId}/availability", 50L)
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(bookingService).isInstrumentAvailable(50L, start, end);
    }
}
