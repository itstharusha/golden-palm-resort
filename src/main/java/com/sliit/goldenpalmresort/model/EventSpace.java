package com.sliit.goldenpalmresort.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "event_spaces")
public class EventSpace {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "capacity", nullable = false)
    private Integer capacity;
    
    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;
    
    @Column(name = "setup_types")
    private String setupTypes; // e.g., "Wedding,Conference,Meeting,Banquet"
    
    @Column(name = "amenities", columnDefinition = "TEXT")
    private String amenities;
    
    @Column(name = "floor_number")
    private Integer floorNumber;
    
    @Column(name = "dimensions")
    private String dimensions; // e.g., "20m x 15m"
    
    @Column(name = "catering_available")
    private boolean cateringAvailable = false;
    
    @Column(name = "audio_visual_equipment")
    private boolean audioVisualEquipment = false;
    
    @Column(name = "parking_available")
    private boolean parkingAvailable = false;
    
    @Enumerated(EnumType.STRING)
    private EventSpaceStatus status = EventSpaceStatus.AVAILABLE;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls; // Comma-separated URLs
    
    @OneToMany(mappedBy = "eventSpace", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Photo> photos = new ArrayList<>();
    
    public enum EventSpaceStatus {
        AVAILABLE, BOOKED, MAINTENANCE, BLOCKED
    }
    
    // Constructors
    public EventSpace() {}
    
    public EventSpace(String name, String description, Integer capacity, BigDecimal basePrice) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.basePrice = basePrice;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    
    public String getSetupTypes() { return setupTypes; }
    public void setSetupTypes(String setupTypes) { this.setupTypes = setupTypes; }
    
    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
    
    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
    
    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
    
    public boolean isCateringAvailable() { return cateringAvailable; }
    public void setCateringAvailable(boolean cateringAvailable) { this.cateringAvailable = cateringAvailable; }
    
    public boolean isAudioVisualEquipment() { return audioVisualEquipment; }
    public void setAudioVisualEquipment(boolean audioVisualEquipment) { this.audioVisualEquipment = audioVisualEquipment; }
    
    public boolean isParkingAvailable() { return parkingAvailable; }
    public void setParkingAvailable(boolean parkingAvailable) { this.parkingAvailable = parkingAvailable; }
    
    public EventSpaceStatus getStatus() { return status; }
    public void setStatus(EventSpaceStatus status) { this.status = status; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }
    
    public List<Photo> getPhotos() { return photos; }
    public void setPhotos(List<Photo> photos) { this.photos = photos; }
} 