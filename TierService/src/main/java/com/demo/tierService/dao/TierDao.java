package com.demo.tierService.dao;

import com.demo.tierService.entity.Tier;

public interface TierDao {
	public Tier getTierByAmountAndUnitId(Double amount,String unitId);
	public Tier getTierByTierId(String tierId);
}
