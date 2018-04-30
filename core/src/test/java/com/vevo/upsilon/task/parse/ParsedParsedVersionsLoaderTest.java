package com.vevo.upsilon.task.parse;

import com.vevo.upsilon.except.UpsilonInitializationException;
import com.vevo.upsilon.task.load.ClasspathTasksLoader;
import org.testng.annotations.Test;

public class ParsedParsedVersionsLoaderTest {

    /**
     * Lets make sure that our parse issues throw a user friendly message
     */
    @Test(expectedExceptions = UpsilonInitializationException.class, expectedExceptionsMessageRegExp = ".*Invalid input 'i'.*")
    public void reportParseErrors() {
        ParsedVersionsLoader.load(ClasspathTasksLoader.from("bad_tasks.up"));
    }

}