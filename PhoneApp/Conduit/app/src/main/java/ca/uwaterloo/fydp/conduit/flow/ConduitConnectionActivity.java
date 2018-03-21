package ca.uwaterloo.fydp.conduit.flow;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import ca.uwaterloo.fydp.conduit.AppConstants;
import ca.uwaterloo.fydp.conduit.R;
import ca.uwaterloo.fydp.conduit.connectionutils.ConduitManager;
import ca.uwaterloo.fydp.conduit.mapping.MapViewActivity;

public class ConduitConnectionActivity extends AppCompatActivity {

    private final int PERMISSIONS_REQUEST_READ_STORAGE = 200; // write is also given
    private final int PERMISSIONS_REQUEST_FINE_LOCATION = 201; // course is also given

    private final int PERMISSIONS_READ_AND_GPS = 401;

   // private AnimationDrawable connectionAnimation;

    private final String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};

    private boolean advanceFlag = false;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setExitTransition(null);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conduit_connection);


//        Intent intent = new Intent(this, MapViewActivity.class);
//        startActivity(intent);

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        AppConstants.PUPPET_MASTER_ENABLED = prefs.getBoolean("isPuppetMaster", false);
        AppConstants.USE_REAL_HARDWARE = !AppConstants.PUPPET_MASTER_ENABLED;
        if(AppConstants.PUPPET_MASTER_ENABLED) {
            Toast.makeText(ConduitConnectionActivity.this, "Puppet Master enabled", Toast.LENGTH_SHORT).show();
        }

        findViewById(R.id.conduit_connect_image).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                prefs.edit().putBoolean("isPuppetMaster", !prefs.getBoolean("isPuppetMaster", false)).commit();
                finish();
                return true;
            }
        });

        if (!requestUserPermissions(PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_READ_AND_GPS);
        }

        // TODO: switch this back otherwise we're not going to actually connect to the hardware!!!!

        if (AppConstants.USE_REAL_HARDWARE) {
            ConduitManager.initialize(this);
        } else {
            ConduitManager.initializeMock();
        }
        if(ConduitManager.getDriver().isConnected()) {

            if(!AppConstants.USE_REAL_HARDWARE){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // lmfao why deal with concurrency anyways
                                advanceFlag = true;
                            }
                        });
                    }
                }).start();
            } else {
                advanceFlag = true;
            }


        }

//        ImageView connectionImage = findViewById(R.id.conduit_connect_image);
//        connectionImage.setBackgroundResource(R.drawable.connect_animation);
        //connectionAnimation = (AnimationDrawable) connectionImage.getBackground();

        ImageView logoImageView = findViewById(R.id.conduit_connect_image);
        final AnimatedVectorDrawable logoDrawable = (AnimatedVectorDrawable) logoImageView.getDrawable();
        logoDrawable.registerAnimationCallback(new Animatable2.AnimationCallback() {
            @Override
            public void onAnimationStart(Drawable drawable) {
                super.onAnimationStart(drawable);
            }

            @Override
            public void onAnimationEnd(Drawable drawable) {
                super.onAnimationEnd(drawable);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(advanceFlag) {
                            nextActivity();
                        } else {
                            logoDrawable.start();
                        }
                    }
                });
            }
        });
        logoDrawable.start();
    }

    private void nextActivity() {

        Intent intent = new Intent(this, AppModeActivity.class);

        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, findViewById(R.id.conduit_connect_image), "logo");

        startActivity(intent, options.toBundle());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //connectionAnimation.start();
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
