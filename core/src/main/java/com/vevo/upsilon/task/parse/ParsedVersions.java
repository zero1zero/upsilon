package com.vevo.upsilon.task.parse;

import org.parboiled.support.Var;

import java.util.ArrayList;
import java.util.List;

public class ParsedVersions {

    private List<ParsedVersion> versions = new ArrayList<>();

    static Var<ParsedVersions> createVar() {
        return new Var<>(new ParsedVersions());
    }

    private ParsedVersions() {}

    ParsedVersions add(ParsedVersion version) {
        this.versions.add(version);

        return this;
    }

    public List<ParsedVersion> getVersions() {
        return versions;
    }
}
