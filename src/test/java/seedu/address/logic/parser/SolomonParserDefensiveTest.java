package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class SolomonParserDefensiveTest {

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        List<Parser<?>> parsers = List.of(new AddCareCommandParser(), new EditCareCommandParser(),
                new DeleteCareCommandParser(), new DeleteAllergyCommandParser(), new ArchiveCommandParser(),
                new UnarchiveCommandParser());

        for (Parser<?> parser : parsers) {
            assertThrows(NullPointerException.class, () -> parser.parse(null));
        }
    }
}
