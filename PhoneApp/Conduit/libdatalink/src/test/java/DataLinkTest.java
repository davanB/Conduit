import com.conduit.libdatalink.DataLink;
import org.junit.Test;
import mock.FiniteBufferMockUsbDriver;

import static org.junit.Assert.*;

public class DataLinkTest {

    @Test
    public void testFiniteSerialBuffer() {
        // This test ensures DataLink handles a finite receiving serial buffer
        FiniteBufferMockUsbDriver driver = new FiniteBufferMockUsbDriver();
        DataLink dataLink = new DataLink(driver);

        // Pretend Arduino is taking too long to service rapid incoming requests
        dataLink.debugLEDBlink((byte)20);
        dataLink.write("This is a test".getBytes());
        dataLink.write("This is a test".getBytes());
        dataLink.write("This is a test".getBytes());

        // It is expected DataLink performs flow control to avoid loss of data
        assertFalse(driver.overflow());
    }
}
