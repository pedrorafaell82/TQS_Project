package tqs.soundshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import tqs.soundshop.config.SecurityConfig;
import tqs.soundshop.dto.CreateInstrumentRequest;
import tqs.soundshop.dto.InstrumentDto;
import tqs.soundshop.dto.UpdateInstrumentRequest;
import tqs.soundshop.service.InstrumentService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(InstrumentController.class)
@Import(SecurityConfig.class)
class InstrumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstrumentService instrumentService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void list_withoutFilters_callsListAll() throws Exception {
        InstrumentDto dto = new InstrumentDto(
                1L, "Guitar", "Desc", "GUITAR", "Fender", "A", new BigDecimal("10.00"), true
        );

        when(instrumentService.listAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/instruments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Guitar"));

        verify(instrumentService).listAll();
    }

    @Test
    void list_withFilters_callsSearch() throws Exception {
        InstrumentDto dto = new InstrumentDto(
                2L, "Bass", "Desc", "BASS", "Fender", "A", new BigDecimal("15.00"), true
        );

        when(instrumentService.search(eq("BASS"), eq("Fender"), any(), any()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/instruments")
                        .param("category", "BASS")
                        .param("brand", "Fender")
                        .param("minPrice", "10")
                        .param("maxPrice", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].category").value("BASS"));

        verify(instrumentService).search(
            "BASS",
            "Fender",
            new BigDecimal("10"),
            new BigDecimal("20")
        );

    }

    @Test
    void getById_returnsInstrument() throws Exception {
        InstrumentDto dto = new InstrumentDto(
                3L, "Piano", "Desc", "PIANO", "Yamaha", "B", new BigDecimal("30.00"), true
        );

        when(instrumentService.getById(3L)).thenReturn(dto);

        mockMvc.perform(get("/api/instruments/{id}", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("Piano"));

        verify(instrumentService).getById(3L);
    }

    @Test
    @WithMockUser(roles = "OWNER", username = "owner@example.com")
    void create_asOwner_returnsCreated() throws Exception {
        CreateInstrumentRequest request = new CreateInstrumentRequest(
                "Guitar", "Desc", "GUITAR", "Fender", "A", new BigDecimal("12.00")
        );

        InstrumentDto dto = new InstrumentDto(
                4L, "Guitar", "Desc", "GUITAR", "Fender", "A", new BigDecimal("12.00"), true
        );

        when(instrumentService.create(eq("owner@example.com"), any(CreateInstrumentRequest.class)))
                .thenReturn(dto);

        mockMvc.perform(post("/api/instruments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4L))
                .andExpect(jsonPath("$.name").value("Guitar"));

        verify(instrumentService).create(eq("owner@example.com"), any(CreateInstrumentRequest.class));
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void update_asOwner_returnsUpdated() throws Exception {
        UpdateInstrumentRequest request = new UpdateInstrumentRequest(
                "NewName", "NewDesc", "BASS", "Yamaha", "B", new BigDecimal("20.00")
        );

        InstrumentDto dto = new InstrumentDto(
                5L, "NewName", "NewDesc", "BASS", "Yamaha", "B", new BigDecimal("20.00"), true
        );

        when(instrumentService.update(eq(5L), any(UpdateInstrumentRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/instruments/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.name").value("NewName"));

        verify(instrumentService).update(eq(5L), any(UpdateInstrumentRequest.class));
    }

    @Test
    @WithMockUser(roles = "OWNER", username = "owner@example.com")
    void listMyInstruments_returnsOwnerInstruments() throws Exception {
        InstrumentDto dto = new InstrumentDto(
                6L, "MyGear", "Desc", "GUITAR", "Fender", "A", new BigDecimal("10.00"), true
        );

        when(instrumentService.listByOwnerEmail("owner@example.com"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/instruments/owner/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(6L));

        verify(instrumentService).listByOwnerEmail("owner@example.com");
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void delete_asOwner_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/instruments/{id}", 7L))
                .andExpect(status().isNoContent());

        verify(instrumentService).delete(7L);
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void activate_asOwner_returnsUpdated() throws Exception {
        InstrumentDto dto = new InstrumentDto(
                8L, "ActiveGear", "Desc", "GUITAR", "Fender", "A", new BigDecimal("10.00"), true
        );

        when(instrumentService.setActive(8L, true)).thenReturn(dto);

        mockMvc.perform(patch("/api/instruments/{id}/activate", 8L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        verify(instrumentService).setActive(8L, true);
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void deactivate_asOwner_returnsUpdated() throws Exception {
        InstrumentDto dto = new InstrumentDto(
                9L, "InactiveGear", "Desc", "GUITAR", "Fender", "A", new BigDecimal("10.00"), false
        );

        when(instrumentService.setActive(9L, false)).thenReturn(dto);

        mockMvc.perform(patch("/api/instruments/{id}/deactivate", 9L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(instrumentService).setActive(9L, false);
    }
}
