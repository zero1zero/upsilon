import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;
import org.testng.annotations.Test;

public class ParseTest {

    public static class TParser extends BaseParser<Object> {

        // h(h)?:mm(:ss)?
        Rule Time_HH_MM_SS() {
            return Sequence(
                    OneOrTwoDigits(), ':',
                    TwoDigits(),
                    FirstOf(Sequence(':', TwoDigits()), push(0)),
                    EOI,
                    swap3() && push(convertToTime(popAsInt(), popAsInt(), popAsInt()))
            );
        }

        Rule OneOrTwoDigits() {
            return FirstOf(TwoDigits(), OneDigit());
        }

        Rule OneDigit() {
            return Sequence(Digit(), push(Integer.parseInt(matchOrDefault("0"))));
        }

        Rule TwoDigits() {
            return Sequence(Sequence(Digit(), Digit()), push(Integer.parseInt(matchOrDefault("0"))));
        }

        Rule Digit() {
            return CharRange('0', '9');
        }

        public Integer popAsInt() {
            return (Integer) pop();
        }

        public String convertToTime(Integer hours, Integer minutes, Integer seconds) {
            return String.format("%s h, %s min, %s s",
                    hours != null ? hours : 0,
                    minutes != null ? minutes : 0,
                    seconds != null ? seconds : 0);
        }
    }

    @Test
    public void parse() {
        TParser parser = Parboiled.createParser(TParser.class);

        String time = "12:20:16";

        ParsingResult<?> result = new RecoveringParseRunner(parser.Time_HH_MM_SS()).run(time);

        System.out.println(result.hasErrors());
    }
}
