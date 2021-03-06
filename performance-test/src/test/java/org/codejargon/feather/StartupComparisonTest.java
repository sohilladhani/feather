package org.codejargon.feather;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dagger.Module;
import dagger.ObjectGraph;
import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 Measures bootstrap cost of different DI tools.
 An iteration includes creating an injector and instantiating the dependency graph.
 */
public class StartupComparisonTest {
    private static final int warmup = 200;
    private static final int iterations = 20000;

    @Test
    public void startupTime() {
        benchmarkExplanation();
        for (int i = 0; i < warmup; ++i) {
            Feather.with().instance(A.class);
            Guice.createInjector().getInstance(A.class);
            pico().getComponent(A.class);
            dagger().get(A.class);
            spring().getBean(A.class);
        }
        StopWatch.millis("Guice", () -> {
            for (int i = 0; i < iterations; ++i) {
                Injector injector = Guice.createInjector();
                injector.getInstance(A.class);
            }
        });
        StopWatch.millis("Feather", () -> {
            for (int i = 0; i < iterations; ++i) {
                Feather feather = Feather.with();
                feather.instance(A.class);
            }
        });
        StopWatch.millis("Dagger", () -> {
            for (int i = 0; i < iterations; ++i) {
                ObjectGraph dagger = dagger();
                dagger.get(A.class);
            }
        });
        StopWatch.millis("PicoContainer", () -> {
            for (int i = 0; i < iterations; ++i) {
                MutablePicoContainer pico = pico();
                pico.getComponent(A.class);
            }
        });
        StopWatch.millis("Spring", () -> {
            for (int i = 0; i < iterations; ++i) {
                ApplicationContext applicationContext = spring();
                applicationContext.getBean(A.class);
            }
        });
    }

    private void benchmarkExplanation() {
        System.out.println(
                String.format(
                        "Starting up DI containers & instantiating a dependency graph %s times." +
                        "Comparison includes: [Guice, Feather, Dagger, PicoContainer, Spring]",
                        iterations)
        );
    }


    public static MutablePicoContainer pico() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(A.class);
        pico.addComponent(B.class);
        pico.addComponent(C.class);
        pico.addComponent(D1.class);
        pico.addComponent(D2.class);
        pico.addComponent(E.class);
        return pico;
    }

    public static ApplicationContext spring() {
        return new AnnotationConfigApplicationContext("org.codejargon.feather");
    }

    public static ObjectGraph dagger() {
        return ObjectGraph.create(new DaggerModule());
    }

    @Module(injects = {A.class})
    public static class DaggerModule {
        @dagger.Provides
        E e() {
            return new E();
        }
    }
}
