package com.demo.tierService.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.demo.tierService.dao.CustomerGroupDao;
import com.demo.tierService.entity.CustomerGroup;

@Component
public class CustomerGroupDaoImpl implements CustomerGroupDao {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public CustomerGroup getCustomerGroup(String customerGroupId) {
		Query query=new Query(Criteria.where("customerGroupId").is(customerGroupId));
		CustomerGroup customerGroup=mongoTemplate.findOne(query, CustomerGroup.class);
		return customerGroup;
	}

	@Override
	public void updateCustomerGroup(CustomerGroup customerGroup) {
		Query query=new Query(Criteria.where("customerGroupId").is(customerGroup.getCustomerGroupId()));
        Update update= new Update().set("amount", customerGroup.getAmount()).set("lifeAmount", customerGroup.getLifeAmount()).set("tierId", customerGroup.getTierId()).set("promotionTime", customerGroup.getPromotionTime());
        mongoTemplate.updateFirst(query,update,CustomerGroup.class);
	}

}
