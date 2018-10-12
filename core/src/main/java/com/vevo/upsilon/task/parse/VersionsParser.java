package com.vevo.upsilon.task.parse;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.Var;

class VersionsParser extends BaseParser<Object> {

    Rule versionsAndTaskBlocks() {
        Var<ParsedVersions> versions = ParsedVersions.createVar();
        return Sequence(
                push(versions.get()), //final result value (head of stack)
                OneOrMore(
                        Sequence(
                                tasksBlock(versions),
                                newlines()
                        )
                )
        );
    }

    Rule tasksBlock(Var<ParsedVersions> versions) {
        Var<ParsedVersion> version = ParsedVersion.createVar();
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
                versionName(),
                poke(version.get().setVersion(match()))
        )
        .label("version");
    }

    Rule versionName() {
        return OneOrMore(
                TestNot(newline()),
                FirstOf(
                        CharRange('a', 'z'),
                        CharRange('A', 'Z'),
                        CharRange('0', '9'),
                        AnyOf(new char[] {'.', '-'})
                )
        )
        .label("version name");
    }

    Rule taskLine(Var<ParsedVersion> version) {
        Var<TaskDeclaration> task = TaskDeclaration.createVar();
        return Sequence(
                push(task.set(new TaskDeclaration())),
                String('-', ' '),
                classname(),
                poke(task.get().setImplClass(match())), //set impl class and replace top
                Optional(taskParameters(task)),
                poke(version.get().addTask((TaskDeclaration) pop()))
        )
        .label("task");
    }

    Rule classname() {
        return OneOrMore(
                TestNot(newline()),
                FirstOf(
                        CharRange('a', 'z'),
                        CharRange('A', 'Z'),
                        CharRange('0', '9'),
                        AnyOf(new char[] {'.', '$'})
                )
        )
        .label("task classname");
    }

    Rule taskParameters(Var<TaskDeclaration> task) {
        return Sequence(
                Ch('('),
                FirstOf(
                        inlineParams(task),
                        multilineParams(task)
                ),
                Ch(')')
        )
        .label("task parameters");
    }

    Rule inlineParams(Var<TaskDeclaration> task) {
        return OneOrMore(
                Sequence(
                        paramName(),
                        push(match()), //push param name
                        Ch(':'),
                        ZeroOrMore(' '),
                        paramValue(),
                        poke(task.get().addParam((String) pop(1), (String) pop(0))), //we pushed the name, then value to the top of the stack
                        Optional(',', ' ')
                )
        );
    }

    Rule multilineParams(Var<TaskDeclaration> task) {
        return Sequence(
                newline(),
                INDENT,
                OneOrMore(
                        Sequence(
                                paramName(),
                                push(match()), //push param name
                                Ch(':'),
                                ZeroOrMore(' '),
                                paramValue(),
                                poke(task.get().addParam((String) pop(1), (String) pop(0))), //we pushed the name, then value to the top of the stack
                                Optional(','),
                                newline()
                        )
                ),
                DEDENT
        );
    }

    Rule paramName() {
        return OneOrMore(
                TestNot(newline()),
                FirstOf(
                        CharRange('a', 'z'),
                        CharRange('A', 'Z'),
                        CharRange('0', '9')
                ))
                .label("param name");
    }

    Rule paramValue() {
        return FirstOf(
                Sequence(
                        OneOrMore(
                                TestNot(newline()),
                                CharRange('0', '9')
                        ),
                        push(match())),
                Sequence(
                        Ch('\''),
                        OneOrMore(
                                TestNot(newline()),
                                FirstOf(
                                        Sequence(Ch('\\'), Ch( '\'')),
                                        NoneOf("'"))),

                        push(match()),
                        Ch('\''))
        )
                .label("param value");
    }

    Rule newlines() {
        return ZeroOrMore(newline());
    }

    Rule newline() {
        return FirstOf('\n', Sequence('\r', Optional('\n')));
    }
}
