package ca.uwaterloo.fydp.conduit;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TextView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private FloatingActionMenu mainMenu;

    private FloatingActionButton textButton;
    private FloatingActionButton mapButton;
    private FloatingActionButton mediaButton;

    private EditText userText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupFloatingActionsButtons();
        setupUserInputBox();

    }

    private void setupUserInputBox() {
        userText   = (EditText)findViewById(R.id.plain_text_input);

        userText.setOnClickListener(clickTextBoxListener);
    }


    private void setupFloatingActionsButtons() {
        mainMenu = (FloatingActionMenu) findViewById(R.id.main_menu);
        textButton = (FloatingActionButton) findViewById(R.id.text);
        mapButton = (FloatingActionButton) findViewById(R.id.map);
        mediaButton = (FloatingActionButton) findViewById(R.id.media);

        textButton.setLabelText("Compose Text");
        mapButton.setLabelText("Send Location");
        mediaButton.setLabelText("Media");

        textButton.setOnClickListener(clickListener);
        mapButton.setOnClickListener(clickListener);
        mediaButton.setOnClickListener(clickListener);
    }

    View.OnClickListener clickTextBoxListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String userInput = userText.getText().toString();
            if (!userInput.equals("")) {
                // do something with it, then clear it
                userText.setText("");
            }
        }
    };

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.text:
                    userText.requestFocus();
                    break;
                case R.id.map:
                    // intent to collect GPS data
                    break;
                case R.id.media:
                    // intent to get an image
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
