package com.techcompare.laptop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Maps the existing Supabase laptops table.
 * This class describes stored data; it does not calculate scores.
 */
@Entity
@Table(name = "laptops")
public class Laptop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String brand;

    @jakarta.persistence.Column(name = "image_url")
    private String imageUrl;

    private Integer geekbenchMulti;
    private Integer geekbenchSingle;
    private Integer gpuBenchmarkScore;
    private Integer ramGb;
    private Integer storageGb;
    private Integer screenResolutionPixels;
    private BigDecimal screenSizeInch;
    private String panelType;
    private Boolean hasTouchscreen;
    private BigDecimal weightKg;
    private Integer batteryLifeHours;

    public Laptop() {
        // Required by JPA.
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getImageUrl() { return imageUrl; }
    public Integer getGeekbenchMulti() { return geekbenchMulti; }
    public Integer getGeekbenchSingle() { return geekbenchSingle; }
    public Integer getGpuBenchmarkScore() { return gpuBenchmarkScore; }
    public Integer getRamGb() { return ramGb; }
    public Integer getStorageGb() { return storageGb; }
    public Integer getScreenResolutionPixels() { return screenResolutionPixels; }
    public BigDecimal getScreenSizeInch() { return screenSizeInch; }
    public String getPanelType() { return panelType; }
    public Boolean getHasTouchscreen() { return hasTouchscreen; }
    public BigDecimal getWeightKg() { return weightKg; }
    public Integer getBatteryLifeHours() { return batteryLifeHours; }

    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setGeekbenchMulti(Integer geekbenchMulti) { this.geekbenchMulti = geekbenchMulti; }
    public void setGeekbenchSingle(Integer geekbenchSingle) { this.geekbenchSingle = geekbenchSingle; }
    public void setGpuBenchmarkScore(Integer gpuBenchmarkScore) { this.gpuBenchmarkScore = gpuBenchmarkScore; }
    public void setRamGb(Integer ramGb) { this.ramGb = ramGb; }
    public void setStorageGb(Integer storageGb) { this.storageGb = storageGb; }
    public void setScreenResolutionPixels(Integer screenResolutionPixels) { this.screenResolutionPixels = screenResolutionPixels; }
    public void setScreenSizeInch(BigDecimal screenSizeInch) { this.screenSizeInch = screenSizeInch; }
    public void setPanelType(String panelType) { this.panelType = panelType; }
    public void setHasTouchscreen(Boolean hasTouchscreen) { this.hasTouchscreen = hasTouchscreen; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public void setBatteryLifeHours(Integer batteryLifeHours) { this.batteryLifeHours = batteryLifeHours; }
    public void setId(Integer id) { this.id = id; }
}
