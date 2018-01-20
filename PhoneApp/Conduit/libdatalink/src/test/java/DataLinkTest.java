import com.conduit.libdatalink.DataLink;
import com.conduit.libdatalink.DataLinkListener;
import mock.EchoBackMockUsbDriver;
import org.junit.Test;
import mock.FiniteBufferMockUsbDriver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    @Test
    public void testPacketRoundTrip() throws InterruptedException {
        // This test ensures DataLink correctly deconstructs and reconstructs packets
        EchoBackMockUsbDriver driver = new EchoBackMockUsbDriver();
        DataLink dataLink = new DataLink(driver);

        final CountDownLatch lock = new CountDownLatch(1);
        final String[] receivedData = new String[1];

        dataLink.setReadListener(new DataLinkListener() {
            @Override
            public void OnReceiveData(String data) {
                receivedData[0] = data;
                lock.countDown();
            }
        });

        dataLink.write("Hello World".getBytes());

        // Need to wait for callback to complete
        lock.await(2000, TimeUnit.MILLISECONDS);

        assertNotNull(receivedData[0]);
        assertEquals("Hello World", receivedData[0]);
    }
}
