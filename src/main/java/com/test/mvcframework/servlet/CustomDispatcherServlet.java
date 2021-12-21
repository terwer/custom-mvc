package com.test.mvcframework.servlet;

import com.test.mvcframework.annotations.CustomAutoWired;
import com.test.mvcframework.annotations.CustomController;
import com.test.mvcframework.annotations.CustomRequestMapping;
import com.test.mvcframework.annotations.CustomService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

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

    // url与方法的映射关系
    private Map<String, Method> handlerMapping = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1、加载配置文件 springnvc.properties
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        doLoadConfig(contextConfigLocation);

        // 2、扫描相关类，找到注解
        doScan("com.test.demo");

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

            if (!aClass.isAnnotationPresent(CustomController.class)) {
                continue;
            }

            String baseUrl = "";
            if (aClass.isAnnotationPresent(CustomRequestMapping.class)) {
                CustomRequestMapping annotation = aClass.getAnnotation(CustomRequestMapping.class);
                baseUrl = annotation.value();// demo
            }

            // 获取方法中的注解
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (int i = 0; i < declaredMethods.length; i++) {
                Method method = declaredMethods[i];

                // 未标识注解，不处理
                if (!method.isAnnotationPresent(CustomRequestMapping.class)) {
                    continue;
                }

                CustomRequestMapping annotation = method.getAnnotation(CustomRequestMapping.class);
                String methodUrl = annotation.value(); // /query

                String url = baseUrl + methodUrl;

                // 建立url与method的映射关系
                handlerMapping.put(url, method);
            }

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
    }
}