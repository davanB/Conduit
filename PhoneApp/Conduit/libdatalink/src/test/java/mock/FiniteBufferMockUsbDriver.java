package mock;

import com.conduit.libdatalink.UsbDriverInterface;
import com.conduit.libdatalink.UsbSerialListener;

import java.util.LinkedList;

/**
 * FiniteBufferMockUsbDriver mocks the Arduino Serial's finite Rx buffer
 */
public class FiniteBufferMockUsbDriver implements UsbDriverInterface {

    private static final int SIZE = 32;

    boolean overflow = false;

    private LinkedList<Byte> finiteBuffer = new LinkedList<Byte>(){
        private static final long serialVersionUID = -6707803882461262867L;

        public boolean add(Byte object) {
        boolean result;
        if(this.size() < SIZE) {
            result = super.add(object);
        } else {
            overflow = true;
            super.removeFirst();
            result = super.add(object);
        }
        return result;
        }
    };

    @Override
    public void sendBuffer(byte[] buf) {
        for (int i = 0; i < buf.length; i++) finiteBuffer.add(buf[i]);
    }

    @Override
    public void setReadListener(UsbSerialListener listener) {

    }

    public boolean overflow() {
        return overflow;
    }
}