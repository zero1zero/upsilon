package com.vevo.upsilon.di;

import com.google.common.collect.Maps;
import com.vevo.upsilon.except.UpsilonInitializationException;
import com.vevo.upsilon.task.Task;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

/**
 * Incredibly/intentionally rudimentary dependency injector
 */
public class DependencyInjector {

    private final Map<Class<?>, Object> graph = Maps.newHashMap();

    public <T extends C, C> void register(T value, Class<C> contract) {
        if (graph.containsKey(contract)) {
            throw new UpsilonInitializationException("Looks like there is already an instance of " + contract + " registered!");
        }

        graph.put(contract, value);
    }

    @SuppressWarnings("unchecked")
    public <T extends Task> T instance(Class<T> task) {

        T instance = null;

        for (Constructor<?> constructor : task.getConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {

                Object[] args = new Object[constructor.getParameterCount()];

                for (int i = 0; i < constructor.getParameterCount(); i++) {
                    Class<?> dep = constructor.getParameterTypes()[i];

                    args[i] = graph.get(dep);

                    if (args[i] == null) {
                        String msg = "Could not find registered dependency for constructor parameter "
                                + dep + " of task " + task + ".\n\n"
                                + "Found constructor params: " + Arrays.toString(constructor.getParameterTypes());

                        throw new UpsilonInitializationException(msg);
                    }
                }

                try {
                    instance = (T) constructor.newInstance(args);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new UpsilonInitializationException("Error instantiating your task class!", e);
                }

                break;
            }
        }

        if (instance == null) {
            try {
                instance = task.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new UpsilonInitializationException("Could not instantiate new instance of task " + task + ". Do you need to add @Inject to your suitable constructor?");
            }
        }

        return instance;
    }
}
