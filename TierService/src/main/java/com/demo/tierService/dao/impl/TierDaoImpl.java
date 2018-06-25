package com.demo.tierService.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.demo.tierService.dao.TierDao;
import com.demo.tierService.entity.CustomerGroup;
import com.demo.tierService.entity.Tier;

@Component
public class TierDaoImpl implements TierDao {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public Tier getTierByAmountAndUnitId(Double amount,String unitId) {
		Query query=new Query(Criteria.where("min").lte(amount)).addCriteria(Criteria.where("max").gte(amount)).addCriteria(Criteria.where("unitId").is(unitId));
		Tier tier=mongoTemplate.findOne(query, Tier.class);
		return tier;
	}

	@Override
	public Tier getTierByTierId(String tierId) {
		Query query=new Query(Criteria.where("tierId").is(tierId));
		Tier tier=mongoTemplate.findOne(query, Tier.class);
		return tier;
	}

}
