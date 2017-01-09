package com.vevo.upsilon.task.parse;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.Var;

public class VersionsParser extends BaseParser<Object> {

    Rule versionsAndTaskBlocks() {
        Var<ParsedVersions> versions = ParsedVersions.createVar();
        return Sequence(
                push(versions.get()), //final result value (head of stack)
                OneOrMore(
                        Sequence(
                                tasksBlock(versions),
                                Optional(newline())
                        )
                )
        );
    }

    Rule tasksBlock(Var<ParsedVersions> versions) {
        Var<ParsedVersion> version = new Var<>();
        return Sequence(
                push(version.set(new ParsedVersion())),
                versionLine(version),
                newline(),
                INDENT,
                OneOrMore(
                        Sequence(
                                taskLine(version),
                                newline()
                        )
                ),
                poke(versions.get().add((ParsedVersion) pop())), //replace the result value with most recent versions value
                DEDENT
        )
        .label("version tasks");
    }

    Rule versionLine(Var<ParsedVersion> version) {
        return Sequence(
                OneOrMore(
                        TestNot(newline()),
                        FirstOf(
                                CharRange('a', 'z'),
                                CharRange('A', 'Z'),
                                CharRange('0', '9'),
                                AnyOf(new char[] {'.', '-'})
                        )
                ),
                poke(version.get().setVersion(match())))
                .label("version");
    }

    Rule taskLine(Var<ParsedVersion> version) {
        return Sequence(
                String('-', ' '),
                OneOrMore(
                        TestNot(newline()),
                        FirstOf(
                                CharRange('a', 'z'),
                                CharRange('A', 'Z'),
                                CharRange('0', '9'),
                                Ch('.')
                        )
                ),
                poke(version.get().addTask(match()))
        )
        .label("task");
    }

    Rule newline() {
        return FirstOf('\n', Sequence('\r', Optional('\n')));
    }
}
