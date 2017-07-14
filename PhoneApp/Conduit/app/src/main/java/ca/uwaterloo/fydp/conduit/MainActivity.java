package ca.uwaterloo.fydp.conduit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.EditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private FloatingActionMenu mainMenu;

    private FloatingActionButton textButton;
    private FloatingActionButton mapButton;
    private FloatingActionButton mediaButton;

    private EditText userText;

    private final int PICK_IMAGE = 100;

    private final int PERMISSIONS_REQUEST_READ_STORAGE = 200; // write is also given
    private final int PERMISSIONS_REQUEST_FINE_LOCATION = 201; // course is also given

    private final int PERMISSIONS_READ_AND_GPS = 401;

    private final String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupFloatingActionsButtons();
        setupUserInputBox();

        if (!requestUserPermissions(PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_READ_AND_GPS);
        }

    }

    private boolean requestUserPermissions(String[] Permissions) {
        for (String permission : Permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
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

    private View.OnClickListener clickTextBoxListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String userInput = userText.getText().toString();
            if (!userInput.equals("")) {
                // do something with it, then clear it
                userText.setText("");
            }
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.text:
                    userText.requestFocus();
                    break;
                case R.id.map:
                    // intent to collect GPS data
                    int accessFineGPSDataCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION);
                    if (accessFineGPSDataCheck == PackageManager.PERMISSION_GRANTED) {
                        // do something with GPS
                    }
                    break;
                case R.id.media:
                    // intent to get an image
                    int accessGalleryPermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (accessGalleryPermissionCheck == PackageManager.PERMISSION_GRANTED) {
                        launchImageIntent();
                    }
                    break;
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                Uri selectedImageUri = data.getData();
                String selectedImagePath = getPath(selectedImageUri);
                // do something with path to image
            }
        }
    }

    /**
     * helper to retrieve the path of an image URI
     */
    private String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        // this is our fallback here
        return uri.getPath();
    }

    private void launchImageIntent() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
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
