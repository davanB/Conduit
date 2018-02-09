package ca.uwaterloo.fydp.conduit.connectionutils;

import android.content.Context;
import android.hardware.usb.UsbManager;

import com.conduit.libdatalink.ConduitGroup;
import com.conduit.libdatalink.DataLink;
import com.conduit.libdatalink.UsbDriverInterface;

import ca.uwaterloo.fydp.conduit.UsbDriver;

public class ConduitManager {
    private ConduitManager() {

    }

    private static UsbDriverInterface driver;

    public static UsbDriverInterface getDriver() {
        return driver;
    }

    public static void initialize(Context context) {
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        driver = new UsbDriver(manager);
    }

    public static void initializeMock() {
        driver = new MockUsbDriver();
    }

    public static ConduitGroup getConduitGroup(int baseAddress, int clientId) {
        if (driver == null) {
            throw new IllegalStateException("Driver not initialized");
        }
        DataLink dataLink = new DataLink(driver);
        return new ConduitGroup(dataLink, baseAddress, clientId);
    }
}
