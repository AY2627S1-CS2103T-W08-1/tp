package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SampleDataUtilTest {

    @Test
    public void getSampleAddressBook_returnsAllSamplePatients() {
        assertEquals(SampleDataUtil.getSamplePatients().length,
                SampleDataUtil.getSampleAddressBook().getPatientList().size());
    }
}
