package com.demo.tierService.bean;

public class OrderInfo {
	private String orderNumber;
	private String userId;
	private Double amount;
	private String customerGroupId;
	private String createTime;
	public OrderInfo(String orderInfoStr) {
		String[] fields=orderInfoStr.split("\\|");
		this.orderNumber=fields[0];
		this.userId=fields[1];
		this.customerGroupId=fields[2];
		this.amount=new Double(fields[3]);
		this.createTime=fields[4];
	}
	public String getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public String getCustomerGroupId() {
		return customerGroupId;
	}
	public void setCustomerGroupId(String customerGroupId) {
		this.customerGroupId = customerGroupId;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}	
}
