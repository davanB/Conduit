import com.conduit.libdatalink.DataLink;
import com.conduit.libdatalink.DataLinkListener;
import mock.EchoBackMockUsbDriver;
import org.junit.Test;
import mock.FiniteBufferMockUsbDriver;

import java.nio.ByteBuffer;
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
        dataLink.write((byte) 0, "This is a test".getBytes());
        dataLink.write((byte) 0, "This is a test".getBytes());
        dataLink.write((byte) 0, "This is a test".getBytes());

        // It is expected DataLink performs flow control to avoid loss of data
        assertFalse(driver.overflow());
    }

    @Test
    public void testPacketRoundTrip() throws InterruptedException {
        // This test ensures DataLink correctly deconstructs and reconstructs packets
        EchoBackMockUsbDriver driver = new EchoBackMockUsbDriver();
        DataLink dataLink = new DataLink(driver);

        final CountDownLatch lock = new CountDownLatch(1);
        final String[] receivedPayload = new String[1];
        final byte[] receivedPayloadType = new byte[1];

        dataLink.setReadListener(new DataLinkListener() {
            @Override
            public void OnReceiveData(int originAddress, byte payloadType, ByteBuffer payload) {
                receivedPayloadType[0] = payloadType;

                // Get String from ByteBuffer
                byte[] networkPayload = new byte[payload.remaining()];
                payload.get(networkPayload);
                receivedPayload[0] = new String(networkPayload);
                lock.countDown();
            }
        });

        final String DATA = "Hello World";

        dataLink.write((byte) 0, DATA.getBytes());

        // Need to wait for callback to complete
        lock.await(2000, TimeUnit.MILLISECONDS);

        assertEquals((byte) 0, receivedPayloadType[0]);
        assertNotNull(receivedPayload[0]);
        assertEquals(DATA.length(), receivedPayload[0].length());
        assertEquals(DATA, receivedPayload[0]);
    }
}
