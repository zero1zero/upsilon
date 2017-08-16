package com.vevo.upsilon.task.parse;

import org.parboiled.Parboiled;
import org.parboiled.buffers.IndentDedentInputBuffer;
import org.parboiled.common.FileUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ParsedVersionsParserTest {

    private ParsingResult<ParsedVersions> run(String values) {
        char[] value = values.toCharArray();

        VersionsParser parser = Parboiled.createParser(VersionsParser.class);

        return new ReportingParseRunner<ParsedVersions>(parser.versionsAndTaskBlocks())
                .run(new IndentDedentInputBuffer(value, 2, "#", true));
    }

    @DataProvider(name = "valid_parses")
    public Object[][] validParsesD() {
        return new Object[][] {
                new String[] {"1.1\n  - ATask"},
                new String[] { "1\n  - ATask" },
                new String[] { "1.1\n  - ATask" },
                new String[] { "1.0\n  - ATask" },
                new String[] { "0.1\n  - ATask" },
                new String[] { "0.1a\n  - ATask" },
                new String[] { "20.1\n  - ATask" },
                new String[] { "20a\n  - ATask" },
                new String[] { "a1.0\n  - ATask" },
                new String[] { "a\n  - ATask" },
                new String[] { "alongversion\n  - ATask" }
        };
    }

    @DataProvider(name = "invalid_parses")
    public Object[][] invalidParsesD() {
        return new Object[][] {
                new String[] {"justaversion"},
                new String[] {"justaversion - ATask?"},
                new String[] {"justaversion\n"},
                new String[] {"\n-JustATask"},
                new String[] { "a\n-com.thing.MyTask" },
                new String[] { "a\n-com.thing.MyTask" },
                new String[] { "a\n-com@:#KSLFK" },
                new String[] { "a\n- " },
        };
    }

    @Test(dataProvider = "valid_parses")
    public void validParses(String value) {
        assertFalse(run(value).hasErrors());
    }

    @Test(dataProvider = "invalid_parses")
    public void invalidParses(String value) {
        assertTrue(run(value).hasErrors());
    }

    @Test
    public void multiSimple() {
        String block =
                "1.1\n" +
                "  - MyTask\n" +
                "  - MyTask1\n" +
                "1.2\n" +
                "  - MyTask2\n" +
                "  - MyTask3";

        ParsedVersions versions = run(block).resultValue;

        ParsedVersion version1 = versions.getVersions().get(0);
        assertEquals(version1.getVersion(), "1.1");
        assertEquals(version1.getTasks().get(0), "MyTask");
        assertEquals(version1.getTasks().get(1), "MyTask1");

        ParsedVersion version = versions.getVersions().get(1);
        assertEquals(version.getVersion(), "1.2");
        assertEquals(version.getTasks().get(0), "MyTask2");
        assertEquals(version.getTasks().get(1), "MyTask3");
    }

    @Test
    public void singleVersion() {
        String block =
                "1.1\n" +
                "  - MyTask\n" +
                "  - MyTask1\n";

        ParsedVersions versions = run(block).resultValue;

        ParsedVersion version = versions.getVersions().get(0);
        assertEquals(version.getVersion(), "1.1");
        assertEquals(version.getTasks().get(0), "MyTask");
        assertEquals(version.getTasks().get(1), "MyTask1");
    }

    @Test
    public void sampleFile() {
        String file = FileUtils.readAllTextFromResource("tasks.up");

        ParsedVersions versions = run(file).resultValue;

        assertEquals(versions.getVersions().size(), 3);
    }
}