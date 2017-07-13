package ca.uwaterloo.fydp.conduit;

import android.os.Bundle;
import android.view.Gravity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionMenu mainMenu = (FloatingActionMenu) findViewById(R.id.menu_red);
        mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        final FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        final FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.fab3);

        fab1.setLabelText("Button 1");
        fab2.setLabelText("Button 2");
        fab3.setLabelText("Button 3");


        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.fab1:
                        Toast toast = Toast.makeText(MainActivity.this,"fab1 pressed", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        break;
                    case R.id.fab2:
                        Toast toast2 = Toast.makeText(MainActivity.this,"fab2 pressed", Toast.LENGTH_LONG);
                        toast2.setGravity(Gravity.CENTER, 0, 0);
                        toast2.show();
                        break;
                    case R.id.fab3:
                        Toast toast3 = Toast.makeText(MainActivity.this,"fab3 pressed", Toast.LENGTH_LONG);
                        toast3.setGravity(Gravity.CENTER, 0, 0);
                        toast3.show();
                        break;
                }
            }
        };

        fab1.setOnClickListener(clickListener);
        fab2.setOnClickListener(clickListener);
        fab3.setOnClickListener(clickListener);
    }

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
