package com.test.demo.service.impl;

import com.test.demo.service.IDemoService;
import com.test.mvcframework.annotations.CustomService;

/**
 * @author: terwer
 * @date: 2021/12/21 23:15
 * @description:
 */
@CustomService
public class DemoServiceImpl implements IDemoService {
    @Override
    public String get(String name) {
        System.out.println("实现了IDemoService");
        return name;
    }
}
