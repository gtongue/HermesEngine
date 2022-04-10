package com.gtongue.hermes.di.context;

import com.gtongue.hermes.di.injection.InjectableClass;
import com.gtongue.hermes.di.injection.InjectableDefinition;
import com.gtongue.hermes.di.injection.InjectableMethodDefinition;
import com.gtongue.hermes.di.injection.InjectionNode;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class HermesApplication {

    private HermesApplication() {

    }

    private static final String AUTOCONFIGURE_SCAN_PATH = "com.gtongue.hermes";

    public static void inititalize(Class<?> baseClass, String... args) throws Exception {
        double startTime = System.currentTimeMillis() / 1000.0;
        log.info("Starting at {} ", LocalDateTime.now().toString());
        String includePath = baseClass.getPackage().getName().trim();
        log.info("Scanning base packages at -- {}", includePath);
        HermesApp hermesApp = null;
        try {
            hermesApp = baseClass.getAnnotation(HermesApp.class);
            if (Objects.isNull(hermesApp)) {
                throw new RuntimeException("Your main class must be annotated with @HermesApp.");
            }
        } catch (Exception e) {
            log.error("Your main class must be annotated with @HermesApp.");
            throw e;
        }
        Collection<URL> baseScan = ClasspathHelper.forPackage(includePath);
        Collection<URL> autoConfigureScan = ClasspathHelper.forPackage(AUTOCONFIGURE_SCAN_PATH);

        Reflections ref = new Reflections(new ConfigurationBuilder()
                .setUrls(Stream.concat(baseScan.stream(), autoConfigureScan.stream()).toArray(URL[]::new))
                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner())
                .filterInputsBy(new FilterBuilder().excludePackage(hermesApp.excludePackage())));
        ApplicationContext.reflections = ref;
        Set<Class<?>> injectableClasses = ref.getTypesAnnotatedWith(InjectableClass.class).stream().filter(cl -> !cl.isAnnotation()).collect(Collectors.toSet());
        log.info("Found {} class(es) to instantiate", injectableClasses.size());
        instantiateClasses(injectableClasses);
        log.info("Started Hermes Application in {} seconds", System.currentTimeMillis() / 1000.0 - startTime);
    }


    private static void instantiateClasses(Set<Class<?>> injectableClasses) throws Exception {
        Map<Class<?>, InjectableDefinition> injectableClassDefinitions = injectableClasses.stream()
                .collect(Collectors.toMap(Function.identity(), InjectableDefinition::createInjectableClassDefinition));
        DirectedAcyclicGraph<InjectionNode, DefaultEdge> classGraph = generateClassGraph(injectableClassDefinitions.values());
        Map<Class<?>, Object> injectedClasses = ApplicationContext.injectedClasses;
        TopologicalOrderIterator<InjectionNode, DefaultEdge> topologicalOrderIterator = new TopologicalOrderIterator<>(classGraph);
        while (topologicalOrderIterator.hasNext()) {
            InjectionNode injectionNode = topologicalOrderIterator.next();
            injectedClasses.put(injectionNode.getProvides(), injectionNode.getInstantiation().call());
        }
    }

    private static DirectedAcyclicGraph<InjectionNode, DefaultEdge> generateClassGraph
            (Collection<InjectableDefinition> injectableDefinitions) {
        DirectedAcyclicGraph<InjectionNode, DefaultEdge> classGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        for (InjectableDefinition definition : injectableDefinitions) {
            classGraph.addVertex(InjectionNode.toInjectionNodeForInjectableDefinition(definition));
            for (InjectableMethodDefinition injectableMethodDefinition : definition.getInjectableMethodDefinitions()) {
                InjectionNode methodInjectionNode = InjectionNode.toInjectionNodeForInjectableMethodDefinition(injectableMethodDefinition);
                if (classGraph.containsVertex(methodInjectionNode)) {
                    log.error("Type {} is defined in more then one @InjectableObject", injectableMethodDefinition.getProvidesType().getName());
                    throw new RuntimeException("Type defined multiple times with @InjectableObject");
                }
                classGraph.addVertex(methodInjectionNode);
            }
        }
//        TODO: Maybe sort this ahead of time so we can add to graph and add to edges at the same time!
        for (InjectableDefinition injectableDefinition : injectableDefinitions) {
            for (Class<?> parameterType : injectableDefinition.getParameterTypes()) {
                InjectionNode equalComparatorNode = InjectionNode.toInjectionNodeFromClass(parameterType);
                if (!classGraph.containsVertex(equalComparatorNode)) {
                    log.error("No class {} found for injecting on class {}", parameterType.getName(),
                            injectableDefinition.getInjectableClass().getName());
                    throw new RuntimeException("Injectable class not found!");
                }
                try {
                    classGraph.addEdge(equalComparatorNode,
                            InjectionNode.toInjectionNodeForInjectableDefinition(injectableDefinition));
                } catch (IllegalArgumentException e) {
                    log.error("Cyclical dependency between {} and {}", parameterType.getName(),
                            injectableDefinition.getInjectableClass().getName());
                    throw new RuntimeException("Cyclical Dependency found!");
                }
                for (InjectableMethodDefinition injectableMethodDefinition : injectableDefinition.getInjectableMethodDefinitions()) {
                    try {
                        InjectionNode methodInjectionNode = InjectionNode.toInjectionNodeForInjectableMethodDefinition(injectableMethodDefinition);
                        classGraph.addEdge(equalComparatorNode, methodInjectionNode);
                    } catch (IllegalArgumentException e) {
                        log.error("SOMETHING");
                    }
                }
            }
        }
        return classGraph;
    }
}
