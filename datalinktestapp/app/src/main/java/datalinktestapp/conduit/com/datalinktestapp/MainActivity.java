package datalinktestapp.conduit.com.datalinktestapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    DataLink l;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        l = new DataLink(manager);
        findViewById(R.id.doot).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                l.openWritingPipe((byte) 'b');
                //l.openReadingPipe((byte)1, (byte)'a');
                l.debugEcho((byte) 'a');
                l.write("hello class".getBytes());
                l.debugEcho((byte)'b');
            }
        });

        final TextView outputText = (TextView) findViewById(R.id.output);

        l.setDataLinkListener(new DataLinkListener() {
            @Override
            public void OnReceiveData(final String data) {
                outputText.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
                        outputText.append("\n");
                        outputText.append(data);
                    }
                });
            }
        });

    }

}
