package com.demo.tierService.dao;

import com.demo.tierService.entity.CustomerGroup;

public interface CustomerGroupDao {
	public CustomerGroup getCustomerGroup(String customerGroupId);
	public void updateCustomerGroup(CustomerGroup customerGroup);
}
