package com.demo.orderService.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderServiceController {
	@Value("${spring.kafka.topic}")
	public  String MQ_TOPIC;
	@Autowired
	DiscoveryClient discoveryClient;
	@Autowired
	KafkaTemplate<String, String> producer;
	
	@RequestMapping(value="/addOrderConsume",method=RequestMethod.GET)
	public String addOrderConsume(String orderNumber,String userId,String customerGroup,String consumeAmount,String createTime) {
		System.out.println("Order "+orderNumber+" consume "+consumeAmount);
		producer.send(MQ_TOPIC, orderNumber+"|"+userId+"|"+customerGroup+"|"+consumeAmount+"|"+createTime);
		return "SUCCESS";	
	}
}
