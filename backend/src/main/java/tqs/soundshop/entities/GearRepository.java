package tqs.soundshop.entities;

import org.springframework.data.jpa.repository.JpaRepository;

import tqs.soundshop.entities.Gear;

public interface GearRepository extends JpaRepository<Gear, Long> {
    // later: custom queries (filter by category, price, etc.)
}