package ca.uwaterloo.fydp.conduit.flow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import ca.uwaterloo.fydp.conduit.R;
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitManager;

public class ConduitConnectionActivity extends AppCompatActivity {

    private final int PERMISSIONS_REQUEST_READ_STORAGE = 200; // write is also given
    private final int PERMISSIONS_REQUEST_FINE_LOCATION = 201; // course is also given

    private final int PERMISSIONS_READ_AND_GPS = 401;

    private AnimationDrawable connectionAnimation;

    private final String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conduit_connection);

        if (!requestUserPermissions(PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_READ_AND_GPS);
        }

        // TODO: switch this back otherwise we're not going to actually connect to the hardware!!!!
        //ConduitManager.initialize(this);
        ConduitManager.initializeMock();
        if(ConduitManager.getDriver().isConnected()) {
            Intent intent = new Intent(this, AppModeActivity.class);
            startActivity(intent);
        }

        ImageView connectionImage = findViewById(R.id.conduit_connect_image);
        connectionImage.setBackgroundResource(R.drawable.connect_animation);
        connectionAnimation = (AnimationDrawable) connectionImage.getBackground();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        connectionAnimation.start();
    }

    private boolean requestUserPermissions(String[] Permissions) {
        for (String permission : Permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
