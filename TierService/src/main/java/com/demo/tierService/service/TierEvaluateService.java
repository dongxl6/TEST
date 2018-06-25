package com.demo.tierService.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demo.tierService.bean.OrderInfo;
import com.demo.tierService.dao.CustomerGroupDao;
import com.demo.tierService.dao.TierDao;
import com.demo.tierService.entity.CustomerGroup;
import com.demo.tierService.entity.Tier;

@Service
public class TierEvaluateService {
	@Autowired
	TierDao tierDao;
	@Autowired
	CustomerGroupDao customerGroupDao;
	
	//!!WARNING:this method is not thread safe, so be carefully use it.
	public synchronized void evaluateTier(OrderInfo order) {
		CustomerGroup customerGroup=customerGroupDao.getCustomerGroup(order.getCustomerGroupId());
		
		Double newAmount=customerGroup.getAmount()+order.getAmount();
		Double newLifeAmount=customerGroup.getLifeAmount()+order.getAmount();

		customerGroup.setAmount(newAmount);
		customerGroup.setLifeAmount(newLifeAmount);
		Tier tier=tierDao.getTierByAmountAndUnitId(newAmount,customerGroup.getUnitId());
		
		if(tier==null) {
			tier=tierDao.getTierByTierId(customerGroup.getTierId());
		}
		if(!tier.getTierId().equals(customerGroup.getTierId())) {
			customerGroup.setTierId(tier.getTierId());
			customerGroup.setPromotionTime(new Date());
		}
		
		customerGroupDao.updateCustomerGroup(customerGroup);
	}
}
