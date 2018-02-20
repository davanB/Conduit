package ca.uwaterloo.fydp.conduit.flow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ca.uwaterloo.fydp.conduit.R;
import ca.uwaterloo.fydp.conduit.flow.master.GroupCreationActivity;
import ca.uwaterloo.fydp.conduit.flow.master.QRGenerationActivity;

public class AppModeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_mode);

        Button master = (Button) findViewById(R.id.startMasterButton);
        master.setOnClickListener(this);
        Button slave = (Button) findViewById(R.id.startSlaveButton);
        slave.setOnClickListener(this);
    }

    private void startMasterFlow() {
        Intent myIntent = new Intent(this, GroupCreationActivity.class);
        startActivity(myIntent);
    }

    private void startSlaveFlow() {
        // TODO: Change the activity that is started here
        Intent myIntent = new Intent(this, QRGenerationActivity.class);
        startActivity(myIntent);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.startMasterButton:
                startMasterFlow();
                break;
            case R.id.startSlaveButton:
                startSlaveFlow();
                break;
        }
    }
}
