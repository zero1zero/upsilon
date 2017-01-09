package com.vevo.upsilon.task.load;

import com.vevo.upsilon.except.UpsilonInitializationException;

import java.io.InputStream;

public class ClasspathTasksLoader implements TasksLoader {

    private final String relativeLocation;

    public static ClasspathTasksLoader from(String relativeLocation) {
        return new ClasspathTasksLoader(relativeLocation);
    }

    private ClasspathTasksLoader(String relativeLocation) {
        this.relativeLocation = relativeLocation;
    }

    @Override
    public InputStream load() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(relativeLocation);
        if (stream == null) {
            throw new UpsilonInitializationException("Tasks classpath resource '" + relativeLocation + "' could not be found");
        }

        return stream;
    }
}
