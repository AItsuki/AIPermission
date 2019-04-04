package com.aitsuki.aipermission.annotation;

import com.aitsuki.aipermission.strategy.DefaultStrategy;
import com.aitsuki.aipermission.strategy.Strategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.StringRes;

/**
 * Create by AItsuki on 2019/3/26.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface RequirePermissions {

    String[] permissions();

    String rationale() default "";

    @StringRes int rationaleId() default -1;

    Class<? extends Strategy> strategy() default DefaultStrategy.class;
}
