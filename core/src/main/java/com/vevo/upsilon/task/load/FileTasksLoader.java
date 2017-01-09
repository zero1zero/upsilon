package com.vevo.upsilon.task.load;

import com.vevo.upsilon.except.UpsilonInitializationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileTasksLoader implements TasksLoader {

    private final File file;

    public static FileTasksLoader from(File file) {
        return new FileTasksLoader(file);
    }

    public static FileTasksLoader from(String file) {
        return new FileTasksLoader(file);
    }

    private FileTasksLoader(String file) {
        this.file = new File(file);
    }

    private FileTasksLoader(File file) {
        this.file = file;
    }

    @Override
    public InputStream load() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new UpsilonInitializationException("Unable to load tasks file", e);
        }
    }
}
