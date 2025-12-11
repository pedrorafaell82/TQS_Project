package tqs.soundshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.soundshop.dto.CreateInstrumentRequest;
import tqs.soundshop.dto.InstrumentDto;
import tqs.soundshop.dto.UpdateInstrumentRequest;
import tqs.soundshop.entities.Gear;
import tqs.soundshop.entities.GearRepository;
import tqs.soundshop.entities.User;
import tqs.soundshop.entities.UserRepository;
import tqs.soundshop.exception.NotFoundException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstrumentServiceTest {

    @Mock
    private GearRepository gearRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InstrumentService instrumentService;

    private User owner;
    private Gear activeGear;
    private Gear inactiveGear;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");
        owner.setName("Owner");

        activeGear = new Gear();
        activeGear.setId(10L);
        activeGear.setName("Stratocaster");
        activeGear.setDescription("Electric guitar");
        activeGear.setCategory("GUITAR");
        activeGear.setBrand("Fender");
        activeGear.setConditionGrade("A");
        activeGear.setDailyPrice(new BigDecimal("20.00"));
        activeGear.setOwner(owner);
        activeGear.setActive(true);
        activeGear.setCreatedAt(Instant.now());

        inactiveGear = new Gear();
        inactiveGear.setId(11L);
        inactiveGear.setName("Old Piano");
        inactiveGear.setDescription("Upright piano");
        inactiveGear.setCategory("PIANO");
        inactiveGear.setBrand("Yamaha");
        inactiveGear.setConditionGrade("B");
        inactiveGear.setDailyPrice(new BigDecimal("15.00"));
        inactiveGear.setOwner(owner);
        inactiveGear.setActive(false);
        inactiveGear.setCreatedAt(Instant.now());
    }

    // ---------- listAll & search ----------

    @Test
    void listAll_onlyReturnsActiveInstruments() {
        when(gearRepository.findAll()).thenReturn(List.of(activeGear, inactiveGear));

        List<InstrumentDto> result = instrumentService.listAll();

        assertThat(result).hasSize(1);
        InstrumentDto dto = result.get(0);
        assertThat(dto.id()).isEqualTo(activeGear.getId());
        assertThat(dto.active()).isTrue();
    }

    @Test
    void search_filtersByCategoryBrandAndPriceRange_andOnlyActive() {
        when(gearRepository.findAll()).thenReturn(List.of(activeGear, inactiveGear));

        BigDecimal minPrice = new BigDecimal("10.00");
        BigDecimal maxPrice = new BigDecimal("25.00");
        String category = "GUITAR";
        String brand = "Fender";

        List<InstrumentDto> result = instrumentService.search(category, brand, minPrice, maxPrice);

        assertThat(result).hasSize(1);
        InstrumentDto dto = result.get(0);
        assertThat(dto.category()).isEqualTo("GUITAR");
        assertThat(dto.brand()).isEqualTo("Fender");
        assertThat(dto.dailyPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void search_whenNoFilters_returnsAllActive() {
        when(gearRepository.findAll()).thenReturn(List.of(activeGear, inactiveGear));

        List<InstrumentDto> result = instrumentService.search(null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(activeGear.getId());
    }

    // ---------- getById ----------

    @Test
    void getById_whenInstrumentExists_returnsDto() {
        Long id = activeGear.getId();
        when(gearRepository.findById(id)).thenReturn(Optional.of(activeGear));

        InstrumentDto dto = instrumentService.getById(id);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo(activeGear.getName());
        assertThat(dto.category()).isEqualTo(activeGear.getCategory());
    }

    @Test
    void getById_whenInstrumentNotFound_throwsNotFound() {
        Long id = 999L;
        when(gearRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> instrumentService.getById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Instrument " + id + " not found");
    }

    // ---------- create ----------

    @Test
    void create_whenOwnerExists_savesAndReturnsDto() {
        String ownerEmail = owner.getEmail();

        CreateInstrumentRequest request = new CreateInstrumentRequest(
                "Bass",
                "4-string bass",
                "BASS",
                "Fender",
                "A",
                new BigDecimal("18.00")
        );

        when(userRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(owner));
        when(gearRepository.save(any(Gear.class))).thenAnswer(invocation -> {
            Gear g = invocation.getArgument(0);
            g.setId(100L);
            return g;
        });

        InstrumentDto dto = instrumentService.create(ownerEmail, request);

        assertThat(dto.id()).isEqualTo(100L);
        assertThat(dto.name()).isEqualTo("Bass");
        assertThat(dto.category()).isEqualTo("BASS");
        assertThat(dto.brand()).isEqualTo("Fender");
        assertThat(dto.dailyPrice()).isEqualByComparingTo("18.00");
        assertThat(dto.active()).isTrue();

        // verify owner was set
        verify(gearRepository).save(any(Gear.class));
    }

    @Test
    void create_whenOwnerNotFound_throwsNotFound() {
        String ownerEmail = owner.getEmail();

        CreateInstrumentRequest request = new CreateInstrumentRequest(
                "Bass",
                "4-string bass",
                "BASS",
                "Fender",
                "A",
                new BigDecimal("18.00")
        );

        when(userRepository.findByEmail(ownerEmail)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> instrumentService.create(ownerEmail, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Owner with email " + ownerEmail + " not found");
    }

    // ---------- listByOwnerEmail ----------

    @Test
    void listByOwnerEmail_returnsMappedDtos() {
        when(gearRepository.findByOwner_Email(owner.getEmail())).thenReturn(List.of(activeGear, inactiveGear));

        List<InstrumentDto> result = instrumentService.listByOwnerEmail(owner.getEmail());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(InstrumentDto::id)
                .containsExactlyInAnyOrder(activeGear.getId(), inactiveGear.getId());
    }

    // ---------- update ----------

    @Test
    void update_whenInstrumentExists_updatesFieldsAndReturnsDto() {
        Long id = activeGear.getId();

        UpdateInstrumentRequest request = new UpdateInstrumentRequest(
                "New Name",
                "New Description",
                "KEYBOARD",
                "Korg",
                "B",
                new BigDecimal("25.00")
        );

        when(gearRepository.findById(id)).thenReturn(Optional.of(activeGear));
        when(gearRepository.save(any(Gear.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InstrumentDto dto = instrumentService.update(id, request);

        assertThat(dto.name()).isEqualTo("New Name");
        assertThat(dto.description()).isEqualTo("New Description");
        assertThat(dto.category()).isEqualTo("KEYBOARD");
        assertThat(dto.brand()).isEqualTo("Korg");
        assertThat(dto.conditionGrade()).isEqualTo("B");
        assertThat(dto.dailyPrice()).isEqualByComparingTo("25.00");
    }

    @Test
    void update_whenInstrumentNotFound_throwsNotFound() {
        Long id = 999L;

        UpdateInstrumentRequest request = new UpdateInstrumentRequest(
                "New Name",
                null,
                null,
                null,
                null,
                null
        );

        when(gearRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> instrumentService.update(id, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Instrument " + id + " not found");
    }

    // ---------- delete ----------

    @Test
    void delete_whenExists_deletesInstrument() {
        Long id = activeGear.getId();
        when(gearRepository.existsById(id)).thenReturn(true);

        instrumentService.delete(id);

        verify(gearRepository).deleteById(id);
    }

    @Test
    void delete_whenNotExists_throwsNotFound() {
        Long id = 999L;
        when(gearRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> instrumentService.delete(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Instrument " + id + " not found");
    }

    // ---------- setActive ----------

    @Test
    void setActive_whenInstrumentExists_updatesActiveFlag() {
        Long id = activeGear.getId();
        activeGear.setActive(false);

        when(gearRepository.findById(id)).thenReturn(Optional.of(activeGear));
        when(gearRepository.save(any(Gear.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InstrumentDto dto = instrumentService.setActive(id, true);

        assertThat(dto.active()).isTrue();
        assertThat(activeGear.isActive()).isTrue();
    }

    @Test
    void setActive_whenInstrumentNotFound_throwsNotFound() {
        Long id = 999L;
        when(gearRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> instrumentService.setActive(id, true))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Instrument " + id + " not found");
    }
}
