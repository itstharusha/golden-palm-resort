package com.sliit.goldenpalmresort.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventSpaceUpdateRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    private Integer capacity;
    
    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;
    
    private String setupTypes;
    
    private String amenities;
    
    @PositiveOrZero(message = "Floor number must be 0 or positive")
    private Integer floorNumber;
    
    private String dimensions;
    
    private Boolean cateringAvailable = false;
    
    private Boolean audioVisualEquipment = false;
    
    private Boolean parkingAvailable = false;
    
    private String status;
    
    private String imageUrls;
    
    // Getters and Setters
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
    
    public Boolean getCateringAvailable() { return cateringAvailable; }
    public void setCateringAvailable(Boolean cateringAvailable) { this.cateringAvailable = cateringAvailable; }
    
    public Boolean getAudioVisualEquipment() { return audioVisualEquipment; }
    public void setAudioVisualEquipment(Boolean audioVisualEquipment) { this.audioVisualEquipment = audioVisualEquipment; }
    
    public Boolean getParkingAvailable() { return parkingAvailable; }
    public void setParkingAvailable(Boolean parkingAvailable) { this.parkingAvailable = parkingAvailable; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }
}