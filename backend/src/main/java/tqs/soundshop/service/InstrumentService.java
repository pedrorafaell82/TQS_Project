package tqs.soundshop.service;

import org.springframework.stereotype.Service;
import tqs.soundshop.dto.CreateInstrumentRequest;
import tqs.soundshop.dto.InstrumentDto;
import tqs.soundshop.entities.Gear;
import tqs.soundshop.entities.GearRepository;
import tqs.soundshop.entities.User;
import tqs.soundshop.entities.UserRepository;
import tqs.soundshop.exception.NotFoundException;

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

    public List<InstrumentDto> listAll() {
        return gearRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public InstrumentDto getById(Long id) {
        Gear gear = gearRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Instrument " + id + " not found"));
        return toDto(gear);
    }

    public InstrumentDto create(CreateInstrumentRequest req) {
        User owner = userRepository.findById(req.ownerId())
                .orElseThrow(() -> new NotFoundException("Owner " + req.ownerId() + " not found"));

        Gear gear = new Gear();
        gear.setName(req.name());
        gear.setDescription(req.description());
        gear.setCategory(req.category());
        gear.setBrand(req.brand());
        gear.setConditionGrade(req.conditionGrade());
        gear.setDailyPrice(req.dailyPrice());
        gear.setOwner(owner);
        gear.setCreatedAt(Instant.now());

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
                gear.getDailyPrice()
        );
    }
}
