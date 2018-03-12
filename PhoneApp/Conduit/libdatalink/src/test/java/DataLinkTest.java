import com.conduit.libdatalink.DataLink;
import com.conduit.libdatalink.DataLinkListener;

import mock.EchoBackMockUsbDriver;
import org.junit.Test;
import mock.FiniteBufferMockUsbDriver;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
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

        DataLinkListener listener = new DataLinkListener() {
            @Override
            public void OnReceiveData(int originAddress, byte payloadType, ByteBuffer payload) {
                receivedPayloadType[0] = payloadType;

                // Get String from ByteBuffer
                byte[] networkPayload = new byte[payload.remaining()];
                payload.get(networkPayload);
                receivedPayload[0] = new String(networkPayload);
                lock.countDown();
            }

            @Override
            public void OnSerialError(byte commandId, byte[] payload) {
            }
        };
        dataLink.addReadListener(listener);

        final String DATA = "Hello World";

        dataLink.openReadingPipe((byte)0, 0xAABBCC0D);
        dataLink.write((byte) 0, DATA.getBytes());

        // Need to wait for callback to complete
        lock.await(2000, TimeUnit.MILLISECONDS);

        assertEquals((byte) 0, receivedPayloadType[0]);
        assertNotNull(receivedPayload[0]);
        assertEquals(DATA.length(), receivedPayload[0].length());
        assertEquals(DATA, receivedPayload[0]);

        dataLink.removeReadListener(listener);
    }

    @Test
    public void testPipeAddressValidation() throws InterruptedException {
        // This test ensures DataLink correctly validates pipe addresses
        EchoBackMockUsbDriver driver = new EchoBackMockUsbDriver();
        DataLink dataLink = new DataLink(driver);

        final byte DATATYPE = 1;
        final byte[] PAYLOAD = "TEST".getBytes();

        // Addresses must differ by the LSB only
        final int[] ADDRESSES = new int[] {
                0xAABBCC01,
                0xAABBCC02,
                0xAABBCC03,
                0xAABBCC04,
                0xAABBCC05,
                0xAABBCC06
        };

        final CountDownLatch lock = new CountDownLatch(ADDRESSES.length);

        // Accumulate output addresses
        final List<Integer> outputAddresses = new ArrayList<Integer>();

        DataLinkListener listener = new DataLinkListener() {
            @Override
            public void OnReceiveData(int originAddress, byte payloadType, ByteBuffer payload) {
                outputAddresses.add(originAddress);
                lock.countDown();
            }

            @Override
            public void OnSerialError(byte commandId, byte[] payload) {
                System.out.println("ERROR");
            }
        };
        dataLink.addReadListener(listener);

        // Open pipes and write data
        for (byte b = 0; b < ADDRESSES.length; b++) {
            dataLink.openReadingPipe(b, ADDRESSES[b]);
            dataLink.write(DATATYPE, "TEST".getBytes());
        }

        // Need to wait for callback to complete
        lock.await(6000, TimeUnit.MILLISECONDS);

        assertEquals(ADDRESSES.length, outputAddresses.size());

        for (int i = 0; i < ADDRESSES.length; i++) {
            System.out.println(String.format("Checking 0x%08X ?= 0x%08X", ADDRESSES[i], outputAddresses.get(i)));
            assertEquals(ADDRESSES[i], (int)outputAddresses.get(i));
        }

        dataLink.removeReadListener(listener);
    }
}
