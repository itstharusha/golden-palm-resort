package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.model.EventSpace;
import com.sliit.goldenpalmresort.repository.EventSpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event-spaces")
@CrossOrigin(origins = "*")
public class EventSpaceController {
    
    @Autowired
    private EventSpaceRepository eventSpaceRepository;
    
    @GetMapping
    public ResponseEntity<List<EventSpace>> getAllEventSpaces() {
        List<EventSpace> eventSpaces = eventSpaceRepository.findByIsActiveTrue();
        return ResponseEntity.ok(eventSpaces);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<EventSpace>> getAvailableEventSpaces() {
        List<EventSpace> eventSpaces = eventSpaceRepository.findByStatus(EventSpace.EventSpaceStatus.AVAILABLE);
        return ResponseEntity.ok(eventSpaces);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EventSpace> getEventSpaceById(@PathVariable Long id) {
        return eventSpaceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<EventSpace>> getEventSpacesByEventType(@PathVariable String eventType) {
        List<EventSpace> eventSpaces = eventSpaceRepository.findByEventType(eventType);
        return ResponseEntity.ok(eventSpaces);
    }
    
    @GetMapping("/catering")
    public ResponseEntity<List<EventSpace>> getEventSpacesWithCatering() {
        List<EventSpace> eventSpaces = eventSpaceRepository.findEventSpacesWithCatering();
        return ResponseEntity.ok(eventSpaces);
    }
    
    @GetMapping("/audio-visual")
    public ResponseEntity<List<EventSpace>> getEventSpacesWithAudioVisual() {
        List<EventSpace> eventSpaces = eventSpaceRepository.findEventSpacesWithAudioVisual();
        return ResponseEntity.ok(eventSpaces);
    }
    
    @GetMapping("/capacity")
    public ResponseEntity<List<EventSpace>> getEventSpacesByCapacity(
            @RequestParam Integer minCapacity,
            @RequestParam Integer maxCapacity) {
        List<EventSpace> eventSpaces = eventSpaceRepository.findByCapacityRange(minCapacity, maxCapacity);
        return ResponseEntity.ok(eventSpaces);
    }
} 