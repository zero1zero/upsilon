package com.vevo.upsilon.task.parse;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.vevo.upsilon.except.UpsilonInitializationException;
import com.vevo.upsilon.task.load.TasksLoader;
import org.parboiled.Parboiled;
import org.parboiled.buffers.IndentDedentInputBuffer;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ParsedVersionsLoader {

    public static ParsedVersions load(TasksLoader loader) {

        byte[] tasks;
        try {
            tasks = ByteStreams.toByteArray(loader.load());
        } catch (IOException e) {
            throw new UpsilonInitializationException("Unable load read input stream from tasks file", e);
        }

        //converting the byte[] to char[]
        char[] charized = Charset.forName(Charsets.UTF_8.name())
                .decode(ByteBuffer.wrap(tasks))
                .array();

        VersionsParser parser = Parboiled.createParser(VersionsParser.class);

        ParsingResult<ParsedVersions> result = new ReportingParseRunner<ParsedVersions>(parser.versionsAndTaskBlocks())
                .run(new IndentDedentInputBuffer(charized, 2, "#", true));

        if (result.hasErrors()) {
            throw new UpsilonInitializationException(ErrorUtils.printParseErrors(result));
        }

        return result.resultValue;
    }
}
