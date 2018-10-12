package com.vevo.upsilon.di;

import com.google.common.collect.Maps;
import com.vevo.upsilon.except.UpsilonInitializationException;
import com.vevo.upsilon.task.Task;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Intentionally rudimentary dependency injector
 */
public class DependencyInjector {

    private final Map<Class<?>, Object> graph = Maps.newHashMap();

    public <T extends C, C> void register(T value, Class<C> contract) {
        if (graph.containsKey(contract)) {
            throw new UpsilonInitializationException("Looks like there is already an instance of " + contract + " registered!");
        }

        graph.put(contract, value);
    }

    public <T extends Task> T instance(Class<T> task) {
        return this.instance(task, Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public <T extends Task> T instance(Class<T> task, Map<String, String> params) {

        T instance = null;

        for (Constructor<?> constructor : task.getConstructors()) {
            if (!constructor.isAnnotationPresent(Inject.class)) {
                continue;
            }

            Object[] args = new Object[constructor.getParameterCount()];

            for (int i = 0; i < constructor.getParameterCount(); i++) {
                Class<?> dep = constructor.getParameterTypes()[i];

                Parameter parameter = constructor.getParameters()[i];
                Class<?> paramClass = parameter.getType();

                String strValue = params.get(parameter.getName());

                //this param name matches something in our param map
                if (params.containsKey(parameter.getName())) {
                    Object value = strValue;

                    //convert a few primitives
                    if (paramClass.equals(int.class)) {
                        value = Integer.valueOf(strValue);
                    }

                    if (paramClass.equals(double.class)) {
                        value = Double.valueOf(strValue);
                    }

                    if (paramClass.equals(long.class)) {
                        value = Long.valueOf(strValue);
                    }

                    args[i] = value;
                } else {
                    //otherwise, lets fill with a graph dep
                    args[i] = graph.get(dep);
                }

                if (args[i] == null) {
                    String msg = "Could not find registered dependency or parameter value for constructor arg "
                            + dep + " of task " + task + ".\n\n"
                            + "Found constructor params: " + Arrays.toString(constructor.getParameters()) + "\n\n"
                            + "Given args: " + params;

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
