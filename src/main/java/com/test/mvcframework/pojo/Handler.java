package com.test.mvcframework.pojo;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Handler {
    private Object Controller;
    private Method method;
    private Pattern pattern;// 正则匹配
    private Map<String,Integer> paramIndexMapping;// 参数顺序
    private Set<String> securityNames;

    public Handler(Object controller, Method method, Pattern pattern, Map<String, Integer> paramIndexMapping) {
        Controller = controller;
        this.method = method;
        this.pattern = pattern;
        this.paramIndexMapping = paramIndexMapping;
    }

    public Object getController() {
        return Controller;
    }

    public void setController(Object controller) {
        Controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Map<String, Integer> getParamIndexMapping() {
        return paramIndexMapping;
    }

    public void setParamIndexMapping(Map<String, Integer> paramIndexMapping) {
        this.paramIndexMapping = paramIndexMapping;
    }

    public Set<String> getSecurityNames() {
        return securityNames;
    }

    public void setSecurityNames(Set<String> securityNames) {
        this.securityNames = securityNames;
    }
}
