package com.pinyougou.cart.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 页面显示登录名
 */
@RestController
@RequestMapping("/user")
public class LoginController {

    @RequestMapping("/loginName")
    public Map loginName(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        Map map = new HashMap();
        map.put("loginName",name);
        return map;
    }
}
