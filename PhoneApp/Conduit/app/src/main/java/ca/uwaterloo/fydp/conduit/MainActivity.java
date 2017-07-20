package ca.uwaterloo.fydp.conduit;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.usb.UsbManager;
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

import com.conduit.libdatalink.DataLink;
import com.conduit.libdatalink.DataLinkListener;
import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionButton;

import java.io.File;

import ca.uwaterloo.fydp.conduit.DataTransformation;

public class MainActivity extends AppCompatActivity {

    UsbManager manager;
    DataLink dataLink;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

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

    private DataTransformation transformer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        transformer = new DataTransformation(this);

        setupFloatingActionsButtons();
        setupUserInputBox();

        if (!requestUserPermissions(PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_READ_AND_GPS);
        }

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        dataLink = new DataLink(new UsbDriver(manager));
        dataLink.setReadListener(new DataLinkListener() {
            @Override
            public void OnReceiveData(final String data) {
                userText.post(new Runnable() {
                    @Override
                    public void run() {
                        userText.append(data);
                    }
                });
            }
        });

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
        mainMenu.setClosedOnTouchOutside(true);
    }

    private View.OnClickListener clickTextBoxListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String userInput = userText.getText().toString();
            if (!userInput.equals("")) {
                byte[] compressedAndEncryptedText = transformer.compressAndEncrypt(userInput);
                userText.setText("");
                byte[] decyeptedAndDecompressed = transformer.decompressAndDecrypt(compressedAndEncryptedText);
                String res = new String(decyeptedAndDecompressed);
                userText.setText(res);
        }
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.text:
                    userText.requestFocus();

                    dataLink.debugEcho((byte)69);
                    dataLink.debugEcho((byte)71);
                    dataLink.debugEcho((byte)71);
                    dataLink.debugEcho((byte)111);
                    dataLink.debugEcho((byte)33);
                    break;
                case R.id.map:
                    // intent to collect GPS data
                    int accessFineGPSDataCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION);
                    if (accessFineGPSDataCheck == PackageManager.PERMISSION_GRANTED) {
                        // TODO do something with GPS
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
                File selectedImagePath = getPath(selectedImageUri);
                if (selectedImagePath != null) {
                    // TODO do something with path to image
                } else {
                    // TODO error handling
                }
            }
        }
    }

    /**
     * helper to retrieve the path of an image URI
     */
    private File getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(
                uri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        File file = new File(filePath);
        return file;
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
        } else if(id == R.id.action_setup) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick a user");
            builder.setItems(new CharSequence[] {"Friend #1", "Friend #2"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int addrA = 0xCDABCD71;
                    int addrB = 0xCDABCD69;
                    int me = addrA;
                    int you = addrB;
                    if(which == 0){
                        me = addrB;
                        you = addrA;
                    }

                    dataLink.openWritingPipe(me);
                    dataLink.openReadingPipe((byte)1, you);
                }
            });
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
