package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class MainAppTest {

    @Test
    public void init_defaultDataFile_isMediConnectFile() throws Exception {
        MainApp app = new MainApp();
        app.init();
        assertEquals(Paths.get("data", "mediconnect.json"), app.storage.getAddressBookFilePath());
    }
}
