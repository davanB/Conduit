import com.conduit.libdatalink.conduitabledata.ConduitConnectionEvent;
import com.conduit.libdatalink.conduitabledata.ConduitGpsLocation;
import com.conduit.libdatalink.conduitabledata.ConduitGroupData;
import com.conduit.libdatalink.conduitabledata.ConduitMessage;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


public class ConduitableDataTest {
    private ByteBuffer simulateTransfer(ByteBuffer in) {
        ByteBuffer out = in.duplicate();
        out.rewind();
        return out;
    }

    @Test
    public void testConduitMessage() {
        String text = "oh hai";
        ConduitMessage messageOut = new ConduitMessage();
        messageOut.setMessage(text);

        ByteBuffer payloadRecv = simulateTransfer(messageOut.getPayload());

        ConduitMessage messageIn = new ConduitMessage();
        messageIn.populateFromPayload(payloadRecv);

        assertEquals(text, messageIn.getMessage());
    }

    @Test
    public void testConduitGpsLocation() {
        double latitude = 43.653226;
        double longitude = -79.383184;
        ConduitGpsLocation messageOut = new ConduitGpsLocation();
        messageOut.setLatitude(latitude);
        messageOut.setLongitude(longitude);

        ByteBuffer payloadRecv = simulateTransfer(messageOut.getPayload());

        ConduitGpsLocation messageIn = new ConduitGpsLocation();
        messageIn.populateFromPayload(payloadRecv);

        assertEquals(latitude, messageIn.getLatitude(), 0.1);
        assertEquals(longitude, messageIn.getLongitude(), 0.1);
    }

    @Test
    public void testConduitConnectionEvent() {
        int clientId = 3;
        String name = "Nevboi";
        ConduitConnectionEvent messageOut = new ConduitConnectionEvent();
        messageOut.setConnectedClientId(clientId);
        messageOut.setConnectedClientName(name);

        ByteBuffer payloadRecv = simulateTransfer(messageOut.getPayload());

        ConduitConnectionEvent messageIn = new ConduitConnectionEvent();
        messageIn.populateFromPayload(payloadRecv);

        assertEquals(clientId, messageIn.getConnectedClientId());
        assertEquals(name, messageIn.getConnectedClientName());
    }
    @Test
    public void testConduitGroupData() {
        String groupName = "Cool Friends";
        ArrayList<String> names = new ArrayList<String>();
        names.add("Bob1");
        names.add("Bob2");
        names.add("Bob3");
        names.add("Bobbby");
        names.add("Jim Halpert");

        ConduitGroupData messageOut = new ConduitGroupData();
        messageOut.setNumClients(5);
        messageOut.setClientNames(names);
        messageOut.setGroupName(groupName);

        ByteBuffer payloadRecv = simulateTransfer(messageOut.getPayload());

        ConduitGroupData messageIn = new ConduitGroupData();
        messageIn.populateFromPayload(payloadRecv);

        assertEquals(5, messageIn.getNumClients());
        assertEquals(names, messageIn.getClientNames());
    }
}
