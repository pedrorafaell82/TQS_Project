package tqs.soundshop.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tqs.soundshop.dto.CreateInstrumentRequest;
import tqs.soundshop.dto.InstrumentDto;
import tqs.soundshop.dto.UpdateInstrumentRequest;
import tqs.soundshop.service.InstrumentService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    // renter-facing: search/discover active instruments
    @GetMapping
    public List<InstrumentDto> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        if (category == null && brand == null && minPrice == null && maxPrice == null) {
            return instrumentService.listAll();
        }
        return instrumentService.search(category, brand, minPrice, maxPrice);
    }

    @GetMapping("/{id}")
    public InstrumentDto getById(@PathVariable Long id) {
        return instrumentService.getById(id);
    }

    // owner-facing: create new instrument for current user
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<InstrumentDto> create(Authentication authentication,
                                                @Valid @RequestBody CreateInstrumentRequest request) {
        String ownerEmail = authentication.getName();
        InstrumentDto created = instrumentService.create(ownerEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // owner-facing: update an instrument (ownership enforcement can be added later)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public InstrumentDto update(@PathVariable Long id,
                                @Valid @RequestBody UpdateInstrumentRequest request) {
        return instrumentService.update(id, request);
    }

    // owner-facing: list all instruments for the current owner
    @GetMapping("/owner/me")
    @PreAuthorize("hasRole('OWNER')")
    public List<InstrumentDto> listMyInstruments(Authentication authentication) {
        String ownerEmail = authentication.getName();
        return instrumentService.listByOwnerEmail(ownerEmail);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        instrumentService.delete(id);
    }

    // owner-facing: activate/deactivate listings
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('OWNER')")
    public InstrumentDto activate(@PathVariable Long id) {
        return instrumentService.setActive(id, true);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('OWNER')")
    public InstrumentDto deactivate(@PathVariable Long id) {
        return instrumentService.setActive(id, false);
    }
}
