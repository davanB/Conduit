package ca.uwaterloo.fydp.conduit.flow.slave;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.conduit.libdatalink.ConduitGroup;
import com.conduit.libdatalink.conduitabledata.ConduitConnectionEvent;
import com.conduit.libdatalink.conduitabledata.ConduitGroupData;
import com.conduit.libdatalink.conduitabledata.ConduitableData;
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes;
import com.google.zxing.Result;

import ca.uwaterloo.fydp.conduit.AppConstants;
import ca.uwaterloo.fydp.conduit.DataTransformation;
import ca.uwaterloo.fydp.conduit.MainActivity;
import ca.uwaterloo.fydp.conduit.conduitview.ConduitActivity;
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitLedger;
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitManager;
import ca.uwaterloo.fydp.conduit.puppets.BootstrappingGroupDataIncoming;
import ca.uwaterloo.fydp.conduit.puppets.BootstrappingQRCodeScanned;
import ca.uwaterloo.fydp.conduit.puppets.PuppetMaster;
import ca.uwaterloo.fydp.conduit.puppets.PuppetShow;
import ca.uwaterloo.fydp.conduit.qr.HandShakeData;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by davanb on 2018-02-08.
 */

public class QrCodeScanner extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private String currentUserName;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Programmatically initialize the scanner view
        mScannerView = new ZXingScannerView(this);
        // Set the scanner view as the content view
        setContentView(mScannerView);


        Intent intent = getIntent();
        currentUserName = intent.getStringExtra(AppConstants.USER_NAME_KEY);

        // Use this to simulate reading a QR code (it will trigger the onResult event for you with data)

        if(!AppConstants.PUPPET_MASTER_ENABLED) {
            PuppetMaster puppetMaster = new PuppetMaster();
            PuppetShow simulateQrScan = new BootstrappingQRCodeScanned(this, ConduitManager.getConduitGroup(0,0));
            puppetMaster.startShow(simulateQrScan);
            return;
        }

        mScannerView.startCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, rawResult.getText(), duration);
        toast.show();

        String result = rawResult.getText();
        String[] values = result.split(",");
        // values[0] = master address
        // values[1] = friend address
        // values[2] = group name
        // values[3] = password

        mScannerView.stopCamera();

        HandShakeData parsedHandshakeData = new HandShakeData(Integer.parseInt(values[0]),Integer.parseInt(values[1]), values[2], values[3]);
        DataTransformation.setSecretKey(values[3]);

        performConduitEvents(parsedHandshakeData);
    }

    private void performConduitEvents(HandShakeData parsedHandshakeData) {
        Log.v("YEET", "Handshake data recv'd! " + parsedHandshakeData);
        final int assignedUserId = parsedHandshakeData.mFriendAddress & 0x000000FF ;
        final int masterAddress = parsedHandshakeData.mMasterAddress;
        final String groupName = parsedHandshakeData.mGroupName;

        final ConduitGroup group = ConduitManager.getConduitGroup(masterAddress, assignedUserId);

        // Inform the master of our info
        group.send(0, new ConduitConnectionEvent(assignedUserId, currentUserName));


        Toast.makeText(this, "Informed Master. Waiting for group data payload ", Toast.LENGTH_LONG).show();

        // Wait for the package that contains all data for the group, from the master
        group.addConduitableDataListener(ConduitableDataTypes.GROUP_DATA, new Function1<ConduitableData, Unit>() {
            @Override
            public Unit invoke(final ConduitableData conduitableData) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(QrCodeScanner.this, "Group data received!", Toast.LENGTH_LONG).show();
                        ConduitGroupData groupData = (ConduitGroupData) conduitableData;

                        final ConduitLedger ledger = new ConduitLedger(masterAddress, groupName, groupData.getNumClients(), assignedUserId, currentUserName);
                        ConduitManager.setLedger(ledger);
                        // pull names into ledger
                        for(int i = 0; i < groupData.getClientNames().size(); i++) {
                            ledger.addGroupMember(i, groupData.getClientNames().get(i));
                        }

                        // let master know that we're ready to go!
                        group.send(0, new ConduitConnectionEvent(assignedUserId, currentUserName));

                        Log.v("NavTest", ledger.getGroupMemberNamesList().toString());

                        // Off to main activity now
                        Intent intent = new Intent(QrCodeScanner.this, ConduitActivity.class);
                        startActivity(intent);
                    }
                });
                return null;
            }
        });


        // User this to simulate group data event firing with data
        PuppetMaster puppetMaster = new PuppetMaster();
        PuppetShow simulateQrScan = new BootstrappingGroupDataIncoming(group);
        puppetMaster.startShow(simulateQrScan);
    }
}
