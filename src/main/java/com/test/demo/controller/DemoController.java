package com.test.demo.controller;

import com.test.demo.service.IDemoService;
import com.test.mvcframework.annotations.CustomAutoWired;
import com.test.mvcframework.annotations.CustomController;
import com.test.mvcframework.annotations.CustomRequestMapping;
import com.test.mvcframework.annotations.Security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: terwer
 * @date: 2021/12/21 23:16
 * @description:
 */
@CustomController
@CustomRequestMapping("/demo")
public class DemoController {
    @CustomAutoWired
    private IDemoService demoService;

    /**
     * /demo/query
     * @param request
     * @param response
     * @param name
     * @return
     */
    @CustomRequestMapping("/query")
    public String query(HttpServletRequest request, HttpServletResponse response, String name){
      return demoService.get(name);
    }

    @Security({"zhangsan"})
    @CustomRequestMapping("/handle01")
    public String handle01(HttpServletRequest request, HttpServletResponse response, String name){
        return demoService.get(name);
    }

    @Security({"lisi"})
    @CustomRequestMapping("/handle02")
    public String handle02(HttpServletRequest request, HttpServletResponse response, String name){
        return demoService.get(name);
    }

    @CustomRequestMapping("/handle03")
    public String handle03(HttpServletRequest request, HttpServletResponse response, String name){
        return demoService.get(name);
    }
}
