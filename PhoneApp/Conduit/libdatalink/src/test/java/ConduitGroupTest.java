import com.conduit.libdatalink.ConduitGroup;
import com.conduit.libdatalink.ConduitGroupHelper;
import com.conduit.libdatalink.DataLink;
import com.conduit.libdatalink.UsbDriverInterface;
import com.conduit.libdatalink.UsbSerialListener;
import com.conduit.libdatalink.conduitabledata.ConduitConnectionEvent;
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation;
import com.conduit.libdatalink.conduitabledata.ConduitGroupData;
import com.conduit.libdatalink.conduitabledata.ConduitMessage;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class ConduitGroupTest {

    @Test
    public void testAddressJoiner() {
        int fullAddress = ConduitGroupHelper.getFullAddress(0xABCDEF00, 3, 2);
        Assert.assertEquals(fullAddress, 0xABCDEF32);
    }

    @Test
    public void testReadingPipes() {
        List<Byte> pipeNumbersExpected = new ArrayList<Byte>();
        pipeNumbersExpected.add((byte) 1);
        pipeNumbersExpected.add((byte) 2);
        pipeNumbersExpected.add((byte) 3);
        pipeNumbersExpected.add((byte) 4);
        pipeNumbersExpected.add((byte) 5);


        List<Integer> addressessExpected = new ArrayList<Integer>();
        addressessExpected.add(0xABCDEF30);
        addressessExpected.add(0xABCDEF31);
        addressessExpected.add(0xABCDEF32);
        addressessExpected.add(0xABCDEF34);
        addressessExpected.add(0xABCDEF35);


        final List<Byte> pipeNumbersRecvd = new ArrayList<Byte>();
        final List<Integer> addressessRecvd = new ArrayList<Integer>();

        UsbDriverInterface mockUsbDriver = new UsbDriverInterface() {
            @Override
            public void sendBuffer(byte[] buf) {}

            @Override
            public void setReadListener(UsbSerialListener listener) {}

            @Override
            public boolean isConnected() {
                return false;
            }
        };
        DataLink mockDataLink = new DataLink(mockUsbDriver) {
            @Override
            public void openReadingPipe(byte pipeNumber, int address) {
                //super.openReadingPipe(pipeNumber, address);
                pipeNumbersRecvd.add(pipeNumber);
                addressessRecvd.add(address);
            }
        };
        ConduitGroup group = new ConduitGroup(mockDataLink, 0xABCDEF00, 3);

        Assert.assertEquals(pipeNumbersExpected, pipeNumbersRecvd);
        Assert.assertEquals(addressessExpected, addressessRecvd);
    }

}
