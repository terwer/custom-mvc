package com.test.mvcframework.annotations;

import java.lang.annotation.*;

/**
 * @author: terwer
 * @date: 2021/12/21 22:45
 * @description:
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomAutoWired {
    String value() default "";
}
