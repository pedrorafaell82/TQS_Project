package tqs.soundshop.service;

import org.springframework.stereotype.Service;
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

@Service
public class InstrumentService {

    private final GearRepository gearRepository;
    private final UserRepository userRepository;

    public InstrumentService(GearRepository gearRepository, UserRepository userRepository) {
        this.gearRepository = gearRepository;
        this.userRepository = userRepository;
    }

    // renter-facing: list all ACTIVE instruments
    public List<InstrumentDto> listAll() {
        return gearRepository.findAll().stream()
                .filter(Gear::isActive)
                .map(this::toDto)
                .toList();
    }

    // renter-facing: search over ACTIVE instruments
    public List<InstrumentDto> search(String category,
                                      String brand,
                                      BigDecimal minPrice,
                                      BigDecimal maxPrice) {
        return gearRepository.findAll().stream()
                .filter(Gear::isActive)
                .filter(g -> category == null || category.equalsIgnoreCase(g.getCategory()))
                .filter(g -> brand == null || brand.equalsIgnoreCase(g.getBrand()))
                .filter(g -> minPrice == null || g.getDailyPrice().compareTo(minPrice) >= 0)
                .filter(g -> maxPrice == null || g.getDailyPrice().compareTo(maxPrice) <= 0)
                .map(this::toDto)
                .toList();
    }

    public InstrumentDto getById(Long id) {
        Gear gear = gearRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Instrument " + id + " not found"));
        return toDto(gear);
    }

    // owner-facing: create instrument for the authenticated owner (email)
    public InstrumentDto create(String ownerEmail, CreateInstrumentRequest req) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new NotFoundException("Owner with email " + ownerEmail + " not found"));

        Gear gear = new Gear();
        gear.setName(req.name());
        gear.setDescription(req.description());
        gear.setCategory(req.category());
        gear.setBrand(req.brand());
        gear.setConditionGrade(req.conditionGrade());
        gear.setDailyPrice(req.dailyPrice());
        gear.setOwner(owner);
        gear.setActive(true);
        gear.setCreatedAt(Instant.now());

        Gear saved = gearRepository.save(gear);
        return toDto(saved);
    }

    // owner-facing: list instruments for current owner
    public List<InstrumentDto> listByOwnerEmail(String ownerEmail) {
        return gearRepository.findByOwner_Email(ownerEmail).stream()
                .map(this::toDto)
                .toList();
    }

    public InstrumentDto update(Long id, UpdateInstrumentRequest req) {
        Gear gear = gearRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Instrument " + id + " not found"));

        if (req.name() != null) {
            gear.setName(req.name());
        }
        if (req.description() != null) {
            gear.setDescription(req.description());
        }
        if (req.category() != null) {
            gear.setCategory(req.category());
        }
        if (req.brand() != null) {
            gear.setBrand(req.brand());
        }
        if (req.conditionGrade() != null) {
            gear.setConditionGrade(req.conditionGrade());
        }
        if (req.dailyPrice() != null) {
            gear.setDailyPrice(req.dailyPrice());
        }

        Gear saved = gearRepository.save(gear);
        return toDto(saved);
    }

    public void delete(Long id) {
        if (!gearRepository.existsById(id)) {
            throw new NotFoundException("Instrument " + id + " not found");
        }
        gearRepository.deleteById(id);
    }

    // owner-facing: activate/deactivate listing
    public InstrumentDto setActive(Long id, boolean active) {
        Gear gear = gearRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Instrument " + id + " not found"));
        gear.setActive(active);
        Gear saved = gearRepository.save(gear);
        return toDto(saved);
    }

    private InstrumentDto toDto(Gear gear) {
        return new InstrumentDto(
                gear.getId(),
                gear.getName(),
                gear.getDescription(),
                gear.getCategory(),
                gear.getBrand(),
                gear.getConditionGrade(),
                gear.getDailyPrice(),
                gear.isActive()
        );
    }
}
