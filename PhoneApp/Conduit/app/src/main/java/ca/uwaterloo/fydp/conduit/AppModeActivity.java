package ca.uwaterloo.fydp.conduit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AppModeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_mode);

        Button one = (Button) findViewById(R.id.startMasterButton);
        one.setOnClickListener(this);
        Button two = (Button) findViewById(R.id.startSlaveButton);
        two.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.startMasterButton:
                break;
            case R.id.startSlaveButton:
                break;
        }
    }
}
