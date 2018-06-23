package com.demo.tierService.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "CustomerGroup")
public class CustomerGroup {
	@Id
	private String id;
	private String customerGroupId;
	private Double amount;
	private Double lifeAmount;
	private String tierId;
	private Date promotionTime;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCustomerGroupId() {
		return customerGroupId;
	}
	public void setCustomerGroupId(String customerGroupId) {
		this.customerGroupId = customerGroupId;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public Double getLifeAmount() {
		return lifeAmount;
	}
	public void setLifeAmount(Double lifeAmount) {
		this.lifeAmount = lifeAmount;
	}
	public String getTierId() {
		return tierId;
	}
	public void setTierId(String tierId) {
		this.tierId = tierId;
	}
	public Date getPromotionTime() {
		return promotionTime;
	}
	public void setPromotionTime(Date promotionTime) {
		this.promotionTime = promotionTime;
	}
}
