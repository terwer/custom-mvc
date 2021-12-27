package com.test.mvcframework.pojo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: terwer
 * @date: 2021/12/27 14:05
 * @description:
 */
public class HandlerExecuteChain {
    private List<HandlerInterceptor> handlerInterceptorList = new ArrayList<>();
    private Handler handler;

    public List<HandlerInterceptor> getIntercetors() {
        return handlerInterceptorList;
    }

    public void addIntercetor(HandlerInterceptor handlerInterceptor) {
        this.handlerInterceptorList.add(handlerInterceptor);
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    // 前置拦截实现
    public boolean applyPreHandle(HttpServletRequest req, HttpServletResponse resp, Handler handler) {
        for (HandlerInterceptor interceptor : this.handlerInterceptorList) {
            if (!interceptor.preHandle(req, resp, handler)) {
                trigerCompletion(req, resp, null);
                return false;
            }
        }
        return true;
    }

    // 后置拦截实现
    public void applyPostHandle(HttpServletRequest req, HttpServletResponse resp, Handler handler) {
        List<HandlerInterceptor> interceptorList = this.handlerInterceptorList;
        for (int i = interceptorList.size() - 1; i >= 0; --i) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            interceptor.postHandle(req, resp, handler);
        }
    }

    // 完成拦截
    public void trigerCompletion(HttpServletRequest req, HttpServletResponse resp, Exception ex) {
        List<HandlerInterceptor> interceptorList = this.handlerInterceptorList;
        for (int i = interceptorList.size() - 1; i >= 0; --i) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            interceptor.afterHandle(req, resp, ex);
        }
    }
}
