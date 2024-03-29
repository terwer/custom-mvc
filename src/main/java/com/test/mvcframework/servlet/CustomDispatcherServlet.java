package com.test.mvcframework.servlet;

import com.test.mvcframework.annotations.*;
import com.test.mvcframework.pojo.Handler;
import com.test.mvcframework.pojo.HandlerExecuteChain;
import com.test.mvcframework.pojo.HandlerInterceptor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: terwer
 * @date: 2021/12/21 22:26
 * @description:
 */
public class CustomDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();
    /**
     * 缓存扫描到的类名
     */
    private List<String> classNames = new ArrayList<>();

    // IOC容器
    private Map<String, Object> ioc = new HashMap<>();

    // 存储所有实现了HandleInterceptor接口的拦截器
    private List<HandlerInterceptor> adaptedInterceptors = new ArrayList<>();

    // url与方法的映射关系
    // private Map<String, Method> handlerMapping = new HashMap<>();
    // private List<Handler> handlerList = new ArrayList<>();
    private List<HandlerExecuteChain> handlerChainList = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1、加载配置文件 springmvc.properties
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        doLoadConfig(contextConfigLocation);

        // 2、扫描相关类，找到注解
        doScan(properties.getProperty("scanPackage"));

        // 3、初始化bean对象（实现ioc容器）
        doInstance();

        // 4、实现依赖注入
        doAutoWired();

        // 5、构造一个HandlerMapping，建立url与method的映射关系
        initHandlerMapping();

        System.out.println("mvc初始化完成");

        // 等待请求
    }

    /**
     * 构造一个HandlerMapping（MVC的核心）
     * 目的：将url与method建立关联
     */
    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 获取IOC容器中当前对象的class类型
            Class<?> aClass = entry.getValue().getClass();

            // 找到所有的拦截器
            // 加上key的判断是因为容器中有两个类型相同的实例，取一个即可，防止后面拦截器重复触发
            if (entry.getKey().equals(HandlerInterceptor.class.getName())
                    && HandlerInterceptor.class.isAssignableFrom(aClass)) {
                HandlerInterceptor interceptor = (HandlerInterceptor) entry.getValue();
                adaptedInterceptors.add(interceptor);
            }

            if (!aClass.isAnnotationPresent(CustomController.class)) {
                continue;
            }

            String baseUrl = "";
            if (aClass.isAnnotationPresent(CustomRequestMapping.class)) {
                CustomRequestMapping annotation = aClass.getAnnotation(CustomRequestMapping.class);
                baseUrl = annotation.value();// demo
            }

            String[] baseNames = null;
            if (aClass.isAnnotationPresent(Security.class)) {
                Security annotation = aClass.getAnnotation(Security.class);
                baseNames = annotation.value();
            }

            // 获取方法中的注解
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (int i = 0; i < declaredMethods.length; i++) {

                Method method = declaredMethods[i];

                // 未标识注解，不处理
                if (!method.isAnnotationPresent(CustomRequestMapping.class)) {
                    continue;
                }

                String[] methodNames = null;
                if (method.isAnnotationPresent(Security.class)) {
                    Security annotation = method.getAnnotation(Security.class);
                    methodNames = annotation.value();
                }

                CustomRequestMapping annotation = method.getAnnotation(CustomRequestMapping.class);
                String methodUrl = annotation.value(); // /query

                String url = baseUrl + methodUrl; // /demo/query

                // 建立url与method的映射关系
                // handlerMapping.put(url, method);

                // 处理参数及其顺序
                Map<String, Integer> paramIndexMapping = new HashMap<>();
                Parameter[] parameters = method.getParameters();
                for (int j = 0; j < parameters.length; j++) {
                    Parameter parameter = parameters[j];
                    if (parameter.getType() == HttpServletRequest.class || parameter.getType() == HttpServletResponse.class) {
                        paramIndexMapping.put(parameter.getType().getSimpleName(), j);
                    } else {
                        paramIndexMapping.put(parameter.getName(), j);
                    }
                }

                Set<String> names = new HashSet<>();
                if (baseNames != null && baseNames.length > 0) {
                    names.addAll(Arrays.asList(baseNames));
                }
                if (methodNames != null && methodNames.length > 0) {
                    names.addAll(Arrays.asList(methodNames));
                }

                Handler handler = new Handler(entry.getValue(), method, Pattern.compile(url), paramIndexMapping);
                handler.setSecurityNames(names);

                HandlerExecuteChain chain = new HandlerExecuteChain();
                chain.setHandler(handler);
                handlerChainList.add(chain);
            }

        }

        if (null == this.adaptedInterceptors || this.adaptedInterceptors.size() == 0) {
            System.out.println("未找到拦截器");
        } else {
            System.out.println("找到" + this.adaptedInterceptors.size() + "个拦截器");
        }
    }

    /**
     * 实现依赖注入
     */
    private void doAutoWired() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                Field declaredField = declaredFields[i];// @CustomAutoWired

                if (!declaredField.isAnnotationPresent(CustomAutoWired.class)) {
                    continue;
                }

                // 有注解的处理
                CustomAutoWired annotation = declaredField.getAnnotation(CustomAutoWired.class);
                String beanName = annotation.value();// beanId
                if ("".equals(beanName.trim())) {
                    // 没有配置beanId，根据类型注入 IDemoService
                    beanName = declaredField.getType().getName();
                }

                declaredField.setAccessible(true);
                try {
                    declaredField.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 初始化bean
     */
    private void doInstance() {
        try {
            if (classNames.size() == 0) {
                return;
            }

            for (int i = 0; i < classNames.size(); i++) {
                String className = classNames.get(i);

                Class<?> aClass = Class.forName(className);
                if (aClass.isAnnotationPresent(CustomController.class)) {
                    // controller处理
                    String simpleName = aClass.getSimpleName();// DemoController
                    String lowerFirstSimpleName = lowerFirst(simpleName);// demoController

                    Object o = aClass.newInstance();
                    ioc.put(lowerFirstSimpleName, o);
                } else if (aClass.isAnnotationPresent(CustomService.class)) {
                    CustomService annotation = aClass.getAnnotation(CustomService.class);
                    String beanName = annotation.value();

                    if (!"".equals(beanName.trim())) {
                        ioc.put(beanName, aClass.newInstance());
                    } else {
                        beanName = lowerFirst(aClass.getSimpleName());// demoService
                        ioc.put(beanName, aClass.newInstance());
                    }

                    // 如果service实现了接口，需要放一份接口，用于注接口入
                    Class<?>[] interfaces = aClass.getInterfaces();
                    for (int j = 0; j < interfaces.length; j++) {
                        Class<?> inrerface = interfaces[j];

                        ioc.put(inrerface.getName(), aClass.newInstance());
                    }

                } else if (aClass.isAnnotationPresent(CustomComponent.class)) {
                    CustomComponent annotation = aClass.getAnnotation(CustomComponent.class);
                    String beanName = annotation.value();

                    if (!"".equals(beanName.trim())) {
                        ioc.put(beanName, aClass.newInstance());
                    } else {
                        beanName = lowerFirst(aClass.getSimpleName());// demoHandlerInterceptor
                        ioc.put(beanName, aClass.newInstance());
                    }

                    // 如果component实现了接口，需要放一份接口，用于注接口入
                    Class<?>[] interfaces = aClass.getInterfaces();
                    for (int j = 0; j < interfaces.length; j++) {
                        Class<?> inrerface = interfaces[j];

                        ioc.put(inrerface.getName(), aClass.newInstance());
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String lowerFirst(String str) {
        char[] chars = str.toCharArray();
        if ('A' <= chars[0] && chars[0] <= 'Z') {
            chars[0] += 32;
        }
        return String.valueOf(chars);
    }

    /**
     * 扫描类，解析注解
     *
     * @param scanPackage
     */
    private void doScan(String scanPackage) {
        String scanPackagePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + scanPackage.replaceAll("\\.", "/");

        File pack = new File(scanPackagePath);

        File[] files = pack.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                doScan(scanPackage + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = scanPackage + "." + file.getName().replaceAll(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * 记载配置文件
     *
     * @param contextConfigLocation
     */
    private void doLoadConfig(String contextConfigLocation) {

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);

        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 处理请求
        System.out.println("收到请求");

        HandlerExecuteChain handlerChain = getHandlerChain(req);

        if (handlerChain == null) {
            resp.getWriter().write("<h1>404 Not Found</h1>");
            return;
        }

        Handler handler = handlerChain.getHandler();

        // 前置拦截
        if (!handlerChain.applyPreHandle(req, resp, handler)) {
            return;
        }

        Map<String, Integer> paramIndexMapping = handler.getParamIndexMapping();

        Method method = handler.getMethod();

        // 参数类型数组
        Class<?>[] parameterTypes = method.getParameterTypes();

        // 参数数组(按顺序存储进去)
        Object[] paramValues = new Object[parameterTypes.length];

        Map<String, String[]> parameterMap = req.getParameterMap();
        for (Map.Entry param : parameterMap.entrySet()) {
            // String value = StringUtils.join(param.getValue(), ",");
            String value = null;
            if (param.getValue() instanceof String[]) {
                String[] pvalues = (String[]) param.getValue();
                value = StringUtils.join(Arrays.asList(pvalues), ","); // aa,bb
            } else {
                value = param.getValue().toString();
            }
            if (!paramIndexMapping.containsKey(param.getKey())) {
                continue;
            }

            // 找到参数
            Integer idx = paramIndexMapping.get(param.getKey());

            paramValues[idx] = value;
        }

        Integer reqIdx = handler.getParamIndexMapping().get(HttpServletRequest.class.getSimpleName());
        paramValues[reqIdx] = req;

        Integer respIdx = handler.getParamIndexMapping().get(HttpServletResponse.class.getSimpleName());
        paramValues[respIdx] = resp;

        try {
            // 核心处理方法
            Object result = method.invoke(handler.getController(), paramValues);
            System.out.println("service实现类中的name参数是：" + result);

            // 后置拦截
            handlerChain.applyPostHandle(req, resp, handler);

            // /demo/handle01
            String viewName = req.getRequestURI().substring(req.getRequestURI().lastIndexOf("/") + 1);// handle01
            String path = properties.get("prefix") + viewName + properties.get("surfix");
            System.out.println("path = " + path);

            RequestDispatcher dispatcher = req.getServletContext().getRequestDispatcher(path);
            dispatcher.forward(req, resp);
        } catch (Exception e) {
            // 完成拦截
            handlerChain.trigerCompletion(req, resp, e);

            e.printStackTrace();
        }
    }

    private HandlerExecuteChain getHandlerChain(HttpServletRequest req) {
        if (handlerChainList.size() == 0) {
            return null;
        }

        // 根据url找打对应的方法并调用
        String requestURI = req.getRequestURI();
        for (HandlerExecuteChain chain : handlerChainList) {
            Handler handler = chain.getHandler();

            Matcher matcher = handler.getPattern().matcher(requestURI);
            if (!matcher.matches()) {
                continue;
            }

            // 注入拦截器
            for (HandlerInterceptor interceptor : adaptedInterceptors) {
                chain.addIntercetor(interceptor);
            }

            return chain;
        }

        return null;
    }
}
