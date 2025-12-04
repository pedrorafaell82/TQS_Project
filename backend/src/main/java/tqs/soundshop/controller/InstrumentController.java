package tqs.soundshop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.soundshop.dto.CreateInstrumentRequest;
import tqs.soundshop.dto.InstrumentDto;
import tqs.soundshop.service.InstrumentService;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping
    public List<InstrumentDto> listAll() {
        return instrumentService.listAll();
    }

    @GetMapping("/{id}")
    public InstrumentDto getById(@PathVariable Long id) {
        return instrumentService.getById(id);
    }

    @PostMapping
    public ResponseEntity<InstrumentDto> create(@RequestBody CreateInstrumentRequest request) {
        InstrumentDto created = instrumentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // later: PUT /{id}, DELETE /{id}, filters, etc.
}
