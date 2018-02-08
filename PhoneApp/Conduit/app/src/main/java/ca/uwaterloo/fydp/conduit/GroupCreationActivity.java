package ca.uwaterloo.fydp.conduit;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ca.uwaterloo.fydp.conduit.QRGenerationAvtivity;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

// splash screen to enter group info and start master flow
public class GroupCreationActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView mGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creation);
        // Set up the group form.
        mGroupName = (AutoCompleteTextView) findViewById(R.id.group_name);

        Button mGroupNameButton = (Button) findViewById(R.id.group_creation_start_button);
        mGroupNameButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), QRGenerationAvtivity.class);
                String message = mGroupName.toString();
                if (isGroupNameValid(message)) {
                    intent.putExtra(EXTRA_MESSAGE, message);
                    startActivity(intent);
                }
                else {
                    String text = "Make group name longer then 4 characters!";
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

}

