package com.test.mvcframework.annotations;

import java.lang.annotation.*;

/**
 * @author: terwer
 * @date: 2021/12/21 22:43
 * @description:
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomController {
    String value() default "";
}
