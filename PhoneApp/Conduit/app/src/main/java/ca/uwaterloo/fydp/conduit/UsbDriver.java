package ca.uwaterloo.fydp.conduit;

import com.conduit.libdatalink.UsbDriverInterface;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.conduit.libdatalink.UsbSerialListener;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class UsbDriver implements UsbDriverInterface {

    private static final byte ERROR_INVALID_COMMAND = 1;

    private static final int TIMEOUT_DEFAULT = 4000;
    private static final int BAUD_RATE = 9600;

    private static final String TAG = "YEET";

    private UsbSerialListener usbSerialListener;
    private UsbSerialPort port;
    private UsbDeviceConnection connection;

    public UsbDriver(UsbManager manager) {
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }
        UsbSerialDriver driver = availableDrivers.get(0);
        connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }
        port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(BAUD_RATE, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            SerialInputOutputManager mSerialIoManager = new SerialInputOutputManager(port, seriaListener);
            ExecutorService mExecutor = Executors.newSingleThreadExecutor();
            mExecutor.submit(mSerialIoManager);

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    private SerialInputOutputManager.Listener seriaListener = new SerialInputOutputManager.Listener() {
        @Override
        public void onNewData(byte[] data) {
            usbSerialListener.OnReceiveData(data);
        }

        @Override
        public void onRunError(Exception e) {
            Log.e(TAG, e.toString());
        }
    };

    public void sendBuffer(byte buf[]){
        try {
            port.write(buf, TIMEOUT_DEFAULT);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public void setReadListener(UsbSerialListener usbSerialListener) {
        this.usbSerialListener = usbSerialListener;
    }

}
