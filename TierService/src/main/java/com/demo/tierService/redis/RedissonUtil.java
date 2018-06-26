package com.demo.tierService.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonUtil {
	@Value("${spring.redis.host}")
	private String host;
	@Value("${spring.redis.port}")
	private String port;
	@Value("${spring.redis.password}")
	private String password;
	@Value("${spring.redis.database}")
	private String database;
	
	@Bean
	public RedissonClient getRedission() {
		Config config=new Config();
		config.useSingleServer().setAddress("redis://"+host+":"+port);//.setPassword(password);
		return Redisson.create(config);
	}
}
