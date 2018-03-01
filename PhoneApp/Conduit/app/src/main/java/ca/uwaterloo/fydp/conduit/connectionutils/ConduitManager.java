package ca.uwaterloo.fydp.conduit.connectionutils;

import android.content.Context;
import android.hardware.usb.UsbManager;

import com.conduit.libdatalink.ConduitGroup;
import com.conduit.libdatalink.DataLink;
import com.conduit.libdatalink.DataLinkInterface;
import com.conduit.libdatalink.UsbDriverInterface;

import java.util.HashMap;

import ca.uwaterloo.fydp.conduit.UsbDriver;

public class ConduitManager {
    private ConduitManager() {

    }

    private static ConduitLedger ledger;
    private static UsbDriverInterface driver;
    private static DataLinkInterface dataLink;

    public static UsbDriverInterface getDriver() {
        return driver;
    }

    public static void initialize(Context context) {
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        driver = new UsbDriver(manager);
        dataLink = new DataLink(driver);
    }

    public static void initializeMock() {
        driver = new MockUsbDriver();
    }

    public static ConduitGroup getConduitGroup(int baseAddress, int clientId) {
        if (driver == null || dataLink == null) {
            throw new IllegalStateException("Driver not initialized");
        }
        return new ConduitGroup(dataLink, baseAddress, clientId);
    }

    public static ConduitGroup getConduitGroup(ConduitLedger ledger) {
        return getConduitGroup(ledger.getGroupAddress(), ledger.getCurrentUserId());
    }

    public static ConduitLedger getLedger() {
        return ledger;
    }

    public static void setLedger(ConduitLedger newLedger){
        ledger = newLedger;
    }
}
