package com.demo.tierService.dao;

import com.demo.tierService.entity.Tier;

public interface TierDao {
	public Tier getTierByAmount(Double amount);
	public Tier getTierByTierId(String tierId);
}
