package com.gtongue.hermes.di.context;

import lombok.Data;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;

@Data
public class ApplicationContext {

//    TODO: should these be global static variables?
    public static Map<Class<?>, Object> injectedClasses = new HashMap<>();
    public static Reflections reflections;

    private ApplicationContext() {

    }
}
