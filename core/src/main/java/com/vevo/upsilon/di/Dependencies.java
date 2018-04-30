package com.vevo.upsilon.di;

import com.google.common.collect.Maps;

import java.util.Map;

public abstract class Dependencies {

    private Map<Class<?>, Object> binds = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public <T> void bind(T instance) {
        bind(instance, (Class<T>) instance.getClass());
    }

    public <T> void bind(T instance, Class<T> contract) {
        this.binds.put(contract, instance);
    }

    public abstract void configure();

    public Map<Class<?>, Object> getBinds() {
        return binds;
    }
}
