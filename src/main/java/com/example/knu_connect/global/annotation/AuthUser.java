package com.example.knu_connect.global.annotation;

import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.*;

@Parameter(hidden = true)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthUser {
}
