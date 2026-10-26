package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class UsageMessageTest {

    @Test
    public void messageUsage_indexCommandsReportedInIssue_useParametersLabel() {
        List<String> usageMessages = List.of(ArchiveCommand.MESSAGE_USAGE, UnarchiveCommand.MESSAGE_USAGE,
                EditCommand.MESSAGE_USAGE, EditCareCommand.MESSAGE_USAGE, DeleteCareCommand.MESSAGE_USAGE,
                DeleteAllergyCommand.MESSAGE_USAGE);

        for (String usageMessage : usageMessages) {
            assertTrue(usageMessage.contains("\nParameters: "));
            assertFalse(usageMessage.contains("\nFormat: "));
        }
    }
}
