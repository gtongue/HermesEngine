package com.gtongue.hermes.di.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HermesApp {
//    TODO: Exclude package should be changed to an array
    String excludePackage() default "!";
}
