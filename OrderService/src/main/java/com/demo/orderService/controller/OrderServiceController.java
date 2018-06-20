package com.demo.orderService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderServiceController {
	@Autowired
	DiscoveryClient discoveryClient;
	@RequestMapping(value="/addOrderConsume",method=RequestMethod.GET)
	public String addOrderConsume(String orderNumber,String userId,String customerGroup,String consumeAmount) {
		System.out.println("Order "+orderNumber+" consume "+consumeAmount);
		return "SUCCESS";	
	}
}
