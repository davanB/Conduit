package ca.uwaterloo.fydp.conduit.flow.master;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ca.uwaterloo.fydp.conduit.AppConstants;
import ca.uwaterloo.fydp.conduit.DataTransformation;
import ca.uwaterloo.fydp.conduit.R;
import ca.uwaterloo.fydp.conduit.flow.master.QRGenerationActivity;

// splash screen to enter group info and start master flow
public class GroupCreationActivity extends AppCompatActivity {

    // UI references.
    private TextInputEditText mGroupName;
    private TextInputEditText mUserName;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creation);
        // Set up the group form.
        mGroupName = (TextInputEditText) findViewById(R.id.group_name);
        mUserName = (TextInputEditText) findViewById(R.id.user_name);
        Random rand = new Random();
        Long randomNum = rand.nextLong();
        mPassword = randomNum.toString();
        DataTransformation.setSecretKey(mPassword);
        final SeekBar groupSizeSlider = findViewById(R.id.group_size_slider);
        final TextView groupSizeReadout = findViewById(R.id.group_size_readout);

        groupSizeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                groupSizeReadout.setText("group size: " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });


        Button mGroupNameButton = (Button) findViewById(R.id.group_creation_start_button);
        mGroupNameButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), QRGenerationActivity.class);
                if (isGroupNameValid(mGroupName.getText().toString()) &&
                        isInputValid(mUserName.getText().toString()) ) {
                    Toast.makeText(GroupCreationActivity.this, groupSizeSlider.getProgress() + "", Toast.LENGTH_LONG).show();
                    intent.putExtra(AppConstants.GROUP_NAME_KEY, mGroupName.getText().toString());
                    intent.putExtra(AppConstants.USER_NAME_KEY, mUserName.getText().toString());
                    intent.putExtra(AppConstants.PASSWORD_KEY, mPassword);
                    intent.putExtra(AppConstants.GROUP_SIZE, groupSizeSlider.getProgress());
                    startActivity(intent);
                }
                else {
                    String text = "Make group name longer then 4 characters and dont leave name empty!";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(view.getContext(), text, duration);
                    toast.show();
                }
            }
        });
    }

    private boolean isGroupNameValid(String groupName) {
        return groupName.length() > 4;
    }
    private boolean isInputValid(String input) { return input.length() > 0; }

}

