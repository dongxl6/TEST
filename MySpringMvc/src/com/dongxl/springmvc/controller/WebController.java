package com.dongxl.springmvc.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dongxl.springmvc.annotation.Autowired;
import com.dongxl.springmvc.annotation.Controller;
import com.dongxl.springmvc.annotation.RequestMapping;
import com.dongxl.springmvc.annotation.RequestParam;
import com.dongxl.springmvc.service.ModifyService;
import com.dongxl.springmvc.service.QueryService;

@Controller
@RequestMapping("/web")
public class WebController {
	@Autowired("myQueryService")
	private QueryService queryService;  
    @Autowired  
    private ModifyService modifyService;
    
    @RequestMapping("/search")  
    public void search(@RequestParam("name") String name, HttpServletRequest request, HttpServletResponse response) {  
        String result = queryService.search(name);  
        out(response, result);  
    }  
  
    @RequestMapping("/add")  
    public void add(@RequestParam("name") String name,  
                    @RequestParam("addr") String addr,  
                    HttpServletRequest request, HttpServletResponse response) {  
        String result = modifyService.add(name, addr);  
        out(response, result);  
    }  
  
    @RequestMapping("/remove")  
    public void remove(@RequestParam("name") Integer id,  
                       HttpServletRequest request, HttpServletResponse response) {  
        String result = modifyService.remove(id);  
        out(response, result);  
    }  
  
    private void out(HttpServletResponse response, String str) {  
        try {  
            response.setContentType("application/json;charset=utf-8");  
            response.getWriter().print(str);  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
}
