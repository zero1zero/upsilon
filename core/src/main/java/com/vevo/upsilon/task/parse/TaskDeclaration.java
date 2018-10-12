package com.vevo.upsilon.task.parse;

import com.google.common.collect.Maps;
import lombok.ToString;
import org.parboiled.support.Var;

import java.util.Map;

@ToString
public class TaskDeclaration {

    private String implClass;
    private Map<String, String> params = Maps.newHashMapWithExpectedSize(0);

    static Var<TaskDeclaration> createVar() {
        return new Var<>(new TaskDeclaration());
    }

    public TaskDeclaration() {}

    public TaskDeclaration(String implClass) {
        this.implClass = implClass;
    }

    public TaskDeclaration addParam(String name, String value) {
        params.put(name, value.replaceAll("\\\\", ""));

        return this;
    }

    public TaskDeclaration setImplClass(String clazz) {
        this.implClass = clazz;

        return this;
    }

    public String getImplClass() {
        return implClass;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
