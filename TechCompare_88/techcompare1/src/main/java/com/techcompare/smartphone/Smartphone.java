package com.techcompare.smartphone;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps the existing Supabase smartphones table.
 * This class describes stored data; it does not calculate scores.
 */
@Entity
@Table(name = "smartphones")
public class Smartphone {

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
    private Integer antutuGpuScore;
    private Integer ramGb;
    private Integer storageGb;
    private Integer batteryMah;
    private Integer mainCameraMp;
    private Integer hasTelephotoLens;
    private Integer videoMaxFps;
    private Integer frontCameraMp;
    private Integer screenRefreshHz;
    private Integer screenBrightnessNits;
    private Integer weightG;
    @jakarta.persistence.Column(name = "has_5g")
    private Boolean has5g;

    public Smartphone() {
        // Required by JPA.
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getImageUrl() { return imageUrl; }
    public Integer getGeekbenchMulti() { return geekbenchMulti; }
    public Integer getGeekbenchSingle() { return geekbenchSingle; }
    public Integer getAntutuGpuScore() { return antutuGpuScore; }
    public Integer getRamGb() { return ramGb; }
    public Integer getStorageGb() { return storageGb; }
    public Integer getBatteryMah() { return batteryMah; }
    public Integer getMainCameraMp() { return mainCameraMp; }
    public Integer getHasTelephotoLens() { return hasTelephotoLens; }
    public Integer getVideoMaxFps() { return videoMaxFps; }
    public Integer getFrontCameraMp() { return frontCameraMp; }
    public Integer getScreenRefreshHz() { return screenRefreshHz; }
    public Integer getScreenBrightnessNits() { return screenBrightnessNits; }
    public Integer getWeightG() { return weightG; }
    public Boolean getHas5g() { return has5g; }

    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setGeekbenchMulti(Integer geekbenchMulti) { this.geekbenchMulti = geekbenchMulti; }
    public void setGeekbenchSingle(Integer geekbenchSingle) { this.geekbenchSingle = geekbenchSingle; }
    public void setAntutuGpuScore(Integer antutuGpuScore) { this.antutuGpuScore = antutuGpuScore; }
    public void setRamGb(Integer ramGb) { this.ramGb = ramGb; }
    public void setStorageGb(Integer storageGb) { this.storageGb = storageGb; }
    public void setBatteryMah(Integer batteryMah) { this.batteryMah = batteryMah; }
    public void setMainCameraMp(Integer mainCameraMp) { this.mainCameraMp = mainCameraMp; }
    public void setHasTelephotoLens(Integer hasTelephotoLens) { this.hasTelephotoLens = hasTelephotoLens; }
    public void setVideoMaxFps(Integer videoMaxFps) { this.videoMaxFps = videoMaxFps; }
    public void setFrontCameraMp(Integer frontCameraMp) { this.frontCameraMp = frontCameraMp; }
    public void setScreenRefreshHz(Integer screenRefreshHz) { this.screenRefreshHz = screenRefreshHz; }
    public void setScreenBrightnessNits(Integer screenBrightnessNits) { this.screenBrightnessNits = screenBrightnessNits; }
    public void setWeightG(Integer weightG) { this.weightG = weightG; }
    public void setHas5g(Boolean has5g) { this.has5g = has5g; }
    public void setId(Integer id) { this.id = id; }
}
