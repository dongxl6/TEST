package com.dongxl.springmvc.service.impl;

import com.dongxl.springmvc.annotation.Service;
import com.dongxl.springmvc.service.QueryService;

@Service("myQueryService")
public class QueryServiceImpl implements QueryService {

	@Override
	public String search(String name) {
		return "invoke search name = " + name;
	}

}
