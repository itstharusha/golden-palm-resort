package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.model.Room;
import com.sliit.goldenpalmresort.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {
    
    @Autowired
    private RoomRepository roomRepository;
    
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomRepository.findByIsActiveTrue();
        return ResponseEntity.ok(rooms);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<Room>> getAvailableRooms() {
        List<Room> rooms = roomRepository.findByStatus(Room.RoomStatus.AVAILABLE);
        return ResponseEntity.ok(rooms);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/type/{roomType}")
    public ResponseEntity<List<Room>> getRoomsByType(@PathVariable String roomType) {
        List<Room> rooms = roomRepository.findByRoomType(roomType);
        return ResponseEntity.ok(rooms);
    }
} 