package ca.uwaterloo.fydp.conduit;

import android.annotation.TargetApi;
import android.media.MediaDataSource;
import android.os.Build;
import java.io.IOException;


@TargetApi(Build.VERSION_CODES.M)
public class ByteArrayMediaDataSource extends MediaDataSource {

    private final byte[] data;

    public ByteArrayMediaDataSource(byte []data) {
        assert data != null;
        this.data = data;
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        int length = data.length;
        if (position >= length) {
            return -1; // -1 indicates EOF
        }
        if (position + size > length) {
            size -= (position + size) - length;
        }
        System.arraycopy(data, (int)position, buffer, offset, size);
        return size;
    }

    @Override
    public long getSize() throws IOException {
        return data.length;
    }

    @Override
    public void close() throws IOException {
        // Nothing to do here
    }

    public byte[] getData() {
        return data;
    }
}