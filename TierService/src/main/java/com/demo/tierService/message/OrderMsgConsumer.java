package com.demo.tierService.message;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.demo.tierService.bean.OrderInfo;
import com.demo.tierService.service.TierEvaluateService;

@Component
public class OrderMsgConsumer {
	public static final String MQ_TOPIC="orderInfo";
	
	@Autowired
	TierEvaluateService tierEvaluateService;
	
	 @KafkaListener(topics = {MQ_TOPIC})
	 public void listen(ConsumerRecord<?, ?> record) {
	     System.out.printf("offset = %d,key =%s,value=%s\n", record.offset(), record.key(), record.value());
	     OrderInfo order=new OrderInfo(record.value().toString());
	     tierEvaluateService.evaluateTier(order);
	 }
}
