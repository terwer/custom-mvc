package com.test.mvcframework.pojo;

import com.test.demo.interceptor.MyInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: terwer
 * @date: 2021/12/27 14:08
 * @description:
 */
public interface HandlerInterceptor {
    /**
     * 请求之前拦截
     *
     * @param req
     * @param resp
     * @param handler
     */
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Handler handler);

    /**
     * 请求完成尚未跳转页面
     *
     * @param req
     * @param resp
     * @param handler
     * @return
     */
    public void postHandle(HttpServletRequest req, HttpServletResponse resp, Handler handler);

    /**
     * 请求完成之后
     *
     * @param req
     * @param resp
     * @param ex
     */
    public void afterHandle(HttpServletRequest req, HttpServletResponse resp, Exception ex);
}
