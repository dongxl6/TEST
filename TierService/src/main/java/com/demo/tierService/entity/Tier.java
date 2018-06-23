package com.demo.tierService.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Tier")
public class Tier {
	@Id
	private String id;
	private String tierId;
	private double min;
	private double max;
    private String previousTier;
    private String nextTier;
    private String unitId;
    private String description;
    private String image;
    private String mobileImage;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTierId() {
		return tierId;
	}
	public void setTierId(String tierId) {
		this.tierId = tierId;
	}
	public double getMin() {
		return min;
	}
	public void setMin(double min) {
		this.min = min;
	}
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	public String getPreviousTier() {
		return previousTier;
	}
	public void setPreviousTier(String previousTier) {
		this.previousTier = previousTier;
	}
	public String getNextTier() {
		return nextTier;
	}
	public void setNextTier(String nextTier) {
		this.nextTier = nextTier;
	}
	public String getUnitId() {
		return unitId;
	}
	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getMobileImage() {
		return mobileImage;
	}
	public void setMobileImage(String mobileImage) {
		this.mobileImage = mobileImage;
	}
}
