package com.gtongue.hermes.di.injection;

import com.gtongue.hermes.di.context.ApplicationContext;
import lombok.Data;

import java.util.Objects;
import java.util.concurrent.Callable;

@Data
public class InjectionNode {
    Class<?> provides;
    InjectableDefinition injectableDefinition;
    Callable<Object> instantiation;

    //    TODO: This whole process is so memory inneficient need to completely refctor
    public static InjectionNode toInjectionNodeForInjectableDefinition(InjectableDefinition injectableDefinition) {
        InjectionNode injectionNode = new InjectionNode();
        injectionNode.setProvides(injectableDefinition.getInjectableClass());
        injectionNode.setInjectableDefinition(injectableDefinition);
        injectionNode.setInstantiation(() -> {
            int parameterCount = injectableDefinition.getParameterTypes().length;
            Object[] parameters = new Object[parameterCount];
            for (int i = 0; i < parameterCount; i++) {
                parameters[i] = ApplicationContext.injectedClasses.get(injectableDefinition.getParameterTypes()[i]);
            }
            return injectableDefinition.getConstructor().newInstance(parameters);
        });
        return injectionNode;
    }

    public static InjectionNode toInjectionNodeForInjectableMethodDefinition(InjectableMethodDefinition injectableMethodDefinition) {
        InjectionNode injectionNode = new InjectionNode();
        injectionNode.setProvides(injectableMethodDefinition.getProvidesType());
        injectionNode.setInstantiation(() -> {
            int parameterCount = injectableMethodDefinition.getParameterTypes().length;
            Object[] parameters = new Object[parameterCount];
            for (int i = 0; i < parameterCount; i++) {
                parameters[i] = ApplicationContext.injectedClasses.get(injectableMethodDefinition.getParameterTypes()[i]);
            }
            Object injectedClass = ApplicationContext.injectedClasses.get(injectableMethodDefinition.getMethod().getDeclaringClass());
            return injectableMethodDefinition.getMethod().invoke(injectedClass, parameters);
        });
        return injectionNode;
    }

    public static InjectionNode toInjectionNodeFromClass(Class<?> providedClass) {
        InjectionNode injectionNode = new InjectionNode();
        injectionNode.setProvides(providedClass);
        return injectionNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InjectionNode that = (InjectionNode) o;
        return Objects.equals(provides, that.provides);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provides);
    }


}
