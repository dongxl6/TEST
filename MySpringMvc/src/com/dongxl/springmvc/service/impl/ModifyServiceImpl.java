package com.dongxl.springmvc.service.impl;

import com.dongxl.springmvc.annotation.Service;
import com.dongxl.springmvc.service.ModifyService;

@Service
public class ModifyServiceImpl implements ModifyService {

	@Override
	public String add(String name, String addr) {
        return "invoke add name = " + name + " addr = " + addr;  
	}

	@Override
	public String remove(Integer id) {
		return "remove id = " + id;
	}

}
