package com.test.demo.interceptor;

import com.test.mvcframework.annotations.CustomComponent;
import com.test.mvcframework.pojo.Handler;
import com.test.mvcframework.pojo.HandlerInterceptor;
import org.apache.commons.lang3.ArrayUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: terwer
 * @date: 2021/12/27 15:27
 * @description:
 */
@CustomComponent
public class MyInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Handler handler) {
        System.out.println("这是preHandle");
        Map<String, String[]> parameterMap = req.getParameterMap();
        // 浏览器传过来的名字
        List<String> reqNames = Arrays.asList(ArrayUtils.isEmpty(parameterMap.get("name")) ? new String[]{} : parameterMap.get("name"));
        // 注解配置的
        Set<String> securityNames = handler.getSecurityNames();

        boolean isAllowed = false;
        for (String reqname : reqNames) {
            for (String secname : securityNames) {
                if (reqname.equals(secname)) {
                    isAllowed = true;
                }
            }
        }

        // 没有权限
        if (!isAllowed) {
            resp.setContentType("text/html;charset=utf-8");
            try {
                System.out.println("校验失败");
                resp.getWriter().write("<span style='color:red;'>not allowed，请检查url</span>");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        System.out.println("校验通过");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse resp, Handler handler) {
        System.out.println("这是postHandle");
    }

    @Override
    public void afterHandle(HttpServletRequest req, HttpServletResponse resp, Exception ex) {
        System.out.println("这是afterHandle");
    }
}
