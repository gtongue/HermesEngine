package com.gtongue.hermes.di.injection;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Slf4j
public class InjectableDefinition {
    Class<?> injectableClass;
    InjectableClass injectableClassAnnotation;
    Constructor<?> constructor;
    Set<InjectableMethodDefinition> injectableMethodDefinitions;
    Class<?>[] parameterTypes;

    //    TODO: Can probably remove a lot of this it's just for "cleaner" code.
    public static InjectableDefinition createInjectableClassDefinition(Class<?> injectableClass) {
        InjectableDefinition returnInjectableClass = new InjectableDefinition();
        returnInjectableClass.setInjectableClass(injectableClass);
        Constructor<?>[] constructors = injectableClass.getDeclaredConstructors();
        if (constructors.length > 1) {
            log.warn("Class {} has more then 1 constructor. Using first found constructor for injection", injectableClass.getName());
        }
        returnInjectableClass.setConstructor(constructors[0]);
        returnInjectableClass.setInjectableClassAnnotation(injectableClass.getAnnotation(InjectableClass.class));
        returnInjectableClass.setParameterTypes(returnInjectableClass.getConstructor().getParameterTypes());
        returnInjectableClass.setInjectableMethodDefinitions(findInjectableMethods(injectableClass));
        return returnInjectableClass;
    }

    private static Set<InjectableMethodDefinition> findInjectableMethods(Class<?> injectableClass) {
        return new HashSet<>(Arrays.asList(injectableClass.getDeclaredMethods())).stream().filter(method ->
                method.isAnnotationPresent(InjectableObject.class)
        ).map(method -> InjectableMethodDefinition.builder()
                .method(method)
                .parameterTypes(method.getParameterTypes())
                .providesType(method.getReturnType())
                .build()).collect(Collectors.toSet());
    }

}
