package com.aitsuki.aipermission.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Create by AItsuki on 2019/4/10.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface UseStrategy {
    Class value();
}
