package com.vevo.upsilon.task.parse;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.Maps;
import org.parboiled.Parboiled;
import org.parboiled.buffers.IndentDedentInputBuffer;
import org.parboiled.common.FileUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    @Test
    public void sampleFile() {
        String file = FileUtils.readAllTextFromResource("classpathloadertest.up");

        ParsedVersions versions = run(file).resultValue;

        assertEquals(versions.getVersions().size(), 3);
    }

    private JsonNode loadTasksCases() {
        StringReader f = new StringReader(FileUtils.readAllTextFromResource("tasks.yaml"));

        DumperOptions options = new DumperOptions();
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.LITERAL);

        Object value = new Yaml(options).load(f);

        return new YAMLMapper().convertValue(value, JsonNode.class);
    }

    @DataProvider(name = "valid")
    public Object[][] validParams() {
        JsonNode root = loadTasksCases();

        Object[][] args = new Object[root.get("valid").size()][];

        int i = 0;
        //iterate each expected block
        for (JsonNode testCase : root.get("valid")) {

            //all the versions for this test case
            List<ExpectedVersion> expectedBlocks = Lists.newArrayList();

            String file = testCase.get("file").asText();

            //all version blocks
            for (JsonNode versions : testCase.withArray("expected")) {
                ExpectedVersion expectedVersion = new ExpectedVersion();
                expectedVersion.version = versions.get("version").asText();

                for (JsonNode task : versions.get("tasks")) {
                    ExpectedTask expectedTask = new ExpectedTask();
                    expectedTask.clazz = task.get("class").asText();

                    if (task.has("params")) {
                        for (Iterator<Map.Entry<String, JsonNode>> it = task.get("params").fields(); it.hasNext(); ) {
                            Map.Entry<String, JsonNode> elt = it.next();

                            expectedTask.params.put(elt.getKey(), elt.getValue().asText());
                        }
                    }

                    expectedVersion.tasks.add(expectedTask);
                }

                expectedBlocks.add(expectedVersion);
            }

            args[i] = new Object[]{file, expectedBlocks};

            i++;
        }

        return args;
    }

    @DataProvider(name = "invalid")
    public Object[][] invalidParams() {
        JsonNode root = loadTasksCases();

        Object[][] args = new Object[root.get("invalid").size()][];

        int i = 0;
        for (JsonNode testCase : root.get("valid")) {

            args[i] = new Object[]{testCase.asText()};

            i++;
        }

        return args;
    }

    private class ExpectedVersion {
        private String version;
        private List<ExpectedTask> tasks = Lists.newArrayList();
    }

    private class ExpectedTask {
        private String clazz;
        private Map<String, String> params = Maps.newHashMap();
    }

    @Test(dataProvider = "valid")
    public void valid(String block, List<ExpectedVersion> expectedVersions) {
        ParsingResult<ParsedVersions> run = run(block);

        assertFalse(run.hasErrors());

        ParsedVersions versions = run.resultValue;

        assertEquals(versions.getVersions().size(), expectedVersions.size());

        int i = 0;
        for(ExpectedVersion expectedVersion : expectedVersions) {
            ParsedVersion version = versions.getVersions().get(i);

            assertEquals(version.getVersion(), expectedVersion.version);

            int j = 0;
            for (ExpectedTask expectedTask : expectedVersion.tasks) {
                TaskDeclaration task = version.getTasks().get(j);

                assertEquals(task.getImplClass(), expectedTask.clazz);
                assertEquals(task.getParams().size(), expectedTask.params.size());

                for (Map.Entry<String, String> expectedParam : expectedTask.params.entrySet()) {
                    assertEquals(expectedParam.getValue(), task.getParams().get(expectedParam.getKey()));
                }

                j++;
            }

            i++;
        }
    }
    @Test(dataProvider = "invalid")
    public void invalid(String versionString) {

        ParsingResult<ParsedVersions> result = run(versionString);

        assertTrue(result.hasErrors(), String.valueOf(result.parseErrors));
    }

}