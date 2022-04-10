package com.gtongue.hermes.di.injection;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

@Data
@Builder
public class InjectableMethodDefinition {
    Method method;
    Class<?>[] parameterTypes;
    Class<?> providesType;
}
