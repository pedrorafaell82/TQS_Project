package tqs.soundshop.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GearRepository extends JpaRepository<Gear, Long> {

    List<Gear> findByOwner_Email(String email);

}
