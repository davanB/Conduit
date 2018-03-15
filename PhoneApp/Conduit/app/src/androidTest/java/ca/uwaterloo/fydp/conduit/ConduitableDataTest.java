package ca.uwaterloo.fydp.conduit;

import com.conduit.libdatalink.conduitabledata.ConduitConnectionEvent;
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation;
import com.conduit.libdatalink.conduitabledata.ConduitGroupData;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import ca.uwaterloo.fydp.conduit.ConduitMessage;

import static org.junit.Assert.assertEquals;


public class ConduitableDataTest {


    static {
        System.loadLibrary("native-lib");
    }

    private ByteBuffer simulateTransfer(ByteBuffer in) {
        ByteBuffer out = in.duplicate();
        out.rewind();
        return out;
    }

    @Test
    public void testConduitMessage() {
        DataTransformation.setSecretKey("dawj90iadwji0");
        String text = "oh hai";
        ConduitMessage messageOut = new ConduitMessage();
        messageOut.setMessage(text);

        ByteBuffer payloadRecv = simulateTransfer(messageOut.getPayload());

        ConduitMessage messageIn = new ConduitMessage();
        messageIn.populateFromPayload(payloadRecv);

        assertEquals(text, messageIn.getMessage());
    }

}
