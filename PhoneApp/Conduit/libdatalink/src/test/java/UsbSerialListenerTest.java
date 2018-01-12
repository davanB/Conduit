import com.conduit.libdatalink.CommandResultHolder;
import com.conduit.libdatalink.DataLink;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.conduit.libdatalink.DataLink.*;
import static org.junit.Assert.*;

public class UsbSerialListenerTest {

    @Test
    public void testSingleCommandPacketParse() {

        List<Byte> data = new ArrayList<Byte>();
        Collections.addAll(data, new Byte[] {1, 34, 100, 69, 4});

        // Sanity check
        assertTrue(data.contains(CONTROL_START_OF_HEADING));
        assertTrue(data.contains(CONTROL_END_OF_TRANSMISSION));

        // Expect to consume data when parsing
        int prevSize = data.size();
        CommandResultHolder result = DataLink.parseCommand(data);
        assertNotEquals(prevSize, data.size());
        assertEquals(data.size(), 0);

        // Validate fields
        assertEquals(result.COMMAND_ID, COMMAND_DEBUG_ECHO);
        assertEquals(result.STATUS, STATUS_SUCCESS);
        assertArrayEquals(result.PAYLOAD, new byte[] {69});

        // There should not be any other commands parsed out
        assertNull(DataLink.parseCommand(data));
    }

    @Test
    public void testMultipleCommandPacketParse() {

        List<Byte> data = new ArrayList<Byte>();
        Collections.addAll(data, new Byte[] {1, 34, 100, 69, 4, 1, 40, 100, 4, 1, 41, 100, 4});

        for (int i = 0; i < 3; i++) {
            // Sanity check
            assertTrue(data.contains(CONTROL_START_OF_HEADING));
            assertTrue(data.contains(CONTROL_END_OF_TRANSMISSION));

            // Expect to consume data when parsing
            int prevSize = data.size();
            CommandResultHolder result = DataLink.parseCommand(data);
            assertNotEquals(prevSize, data.size());

            if (i == 0) assertEquals(result.COMMAND_ID, COMMAND_DEBUG_ECHO);
            if (i == 1) assertEquals(result.COMMAND_ID, COMMAND_OPEN_WRITING_PIPE);
            if (i == 2) assertEquals(result.COMMAND_ID, COMMAND_OPEN_READING_PIPE);

            assertEquals(result.STATUS, STATUS_SUCCESS);

            if (i == 0) assertArrayEquals(result.PAYLOAD, new byte[] {69});
            if (i == 1 || i == 2) assertEquals(result.PAYLOAD.length, 0);
        }

    }
}