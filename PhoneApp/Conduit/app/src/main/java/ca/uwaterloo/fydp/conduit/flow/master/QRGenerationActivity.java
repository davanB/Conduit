package ca.uwaterloo.fydp.conduit.flow.master;

import com.conduit.libdatalink.ConduitGroup;
import com.conduit.libdatalink.conduitabledata.ConduitConnectionEvent;
import com.conduit.libdatalink.conduitabledata.ConduitableData;
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import ca.uwaterloo.fydp.conduit.AppConstants;
import ca.uwaterloo.fydp.conduit.Contents;
import ca.uwaterloo.fydp.conduit.MainActivity;
import ca.uwaterloo.fydp.conduit.R;
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitLedger;
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitManager;
import ca.uwaterloo.fydp.conduit.puppets.BootstrappingConnectionEventsIncoming;
import ca.uwaterloo.fydp.conduit.puppets.PuppetMaster;
import ca.uwaterloo.fydp.conduit.puppets.PuppetShow;
import ca.uwaterloo.fydp.conduit.qr.QRCodeEncoder;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;


/*
    This was also copy pasted from some random tutorial
    it needs to be mofdified but maybe we need to give credit to the guy?
*/

public class QRGenerationActivity extends Activity{

    GroupData groupData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrgeneration_avtivity);

        Intent intent = getIntent();
        String groupName = intent.getStringExtra(AppConstants.GROUP_NAME_KEY);
        String userName = intent.getStringExtra(AppConstants.USER_NAME_KEY);
        String password = intent.getStringExtra(AppConstants.PASSWORD_KEY);

        groupData = new GroupData(groupName, password);

        // TODO: baseaddress needs to be the common lower 3 bytes of all addresses... might need to double check that this consistent with what Davan did in this class
        // clientId is 0 since we are the master
        ConduitManager.setLedger(new ConduitLedger(groupData.getBaseAddress(), groupName, 6, 0, userName));

        ConduitGroup conduitGroup = ConduitManager.getConduitGroup(ConduitManager.getLedger());

        // Add self to ledger
        ConduitManager.getLedger().addGroupMember(0, userName);

        // As new users join, build up the ledger
        conduitGroup.addConduitableDataListener(ConduitableDataTypes.CONNECTION_EVENT, new Function1<ConduitableData, Unit>() {
            @Override
            public Unit invoke(final ConduitableData conduitableData) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ConduitConnectionEvent newUserConnectionEvent = (ConduitConnectionEvent) conduitableData;
                        String newUserName = newUserConnectionEvent.getConnectedClientName();
                        int newUserId = newUserConnectionEvent.getConnectedClientId();

                        if(groupData.getCurrentAddress() != newUserId) {
                            throw new IllegalStateException("Conduit connection from wrong userId");
                        }

                        ConduitManager.getLedger().addGroupMember(newUserId, newUserName);
                        nextCode();
                    }
                });
                return null;
            }
        });

        // TODO: The following code is being used for debug purposes
        // Fire off the events using puppetmaster to simulate users joining the room
        PuppetMaster puppetMaster = new PuppetMaster();
        PuppetShow simulateConduitConnectionEvents = new BootstrappingConnectionEventsIncoming(conduitGroup);
        puppetMaster.startShow(simulateConduitConnectionEvents);

        // display the first code
        nextCode();
    }

    private void nextCode() {
        String qrInputText = groupData.generateHandShakeData().toString();
        if(groupData.isFinishedHandshakes()) {
            // if we're about to display the QR code for master, stop. We're done.
            Intent intent = new Intent(this, DistributeGroupDataActivity.class);
            startActivity(intent);
        }

        //Find screen size
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3/4;

        //Encode with a QR Code image
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrInputText,
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(),
                smallerDimension);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            ImageView myImage = (ImageView) findViewById(R.id.QRView);
            myImage.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

}
