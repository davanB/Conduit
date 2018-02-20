package ca.uwaterloo.fydp.conduit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.text.Html;
import android.widget.EditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.conduit.libdatalink.ConduitGroup;
import com.conduit.libdatalink.conduitabledata.ConduitConnectionEvent;
import com.conduit.libdatalink.conduitabledata.ConduitMessage;
import com.conduit.libdatalink.conduitabledata.ConduitableData;
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes;
import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

import ca.uwaterloo.fydp.conduit.connectionutils.ConduitManager;
import ca.uwaterloo.fydp.conduit.flow.master.QRGenerationActivity;
import ca.uwaterloo.fydp.conduit.puppets.BootstrappingConnectionEventsIncoming;
import ca.uwaterloo.fydp.conduit.puppets.PassiveAggressiveConversation;
import ca.uwaterloo.fydp.conduit.puppets.PuppetMaster;
import ca.uwaterloo.fydp.conduit.puppets.PuppetShow;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    private ConduitGroup conduitGroup;

    private FloatingActionMenu mainMenu;

    private FloatingActionButton textButton;
    private FloatingActionButton mapButton;
    private FloatingActionButton mediaButton;

    private EditText userText;
    private TextView textView;
    private Button sendButton;

    private final int PICK_IMAGE = 100;
    private DataTransformation transformer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        transformer = new DataTransformation(this);

        setupFloatingActionsButtons();
        setUpTextBoxes();
        setUpSendButton();

        conduitGroup = ConduitManager.getConduitGroup(900, 2);
        conduitGroup.addConduitableDataListener(ConduitableDataTypes.MESSAGE, new Function1<ConduitableData, Unit>() {
            @Override
            public Unit invoke(ConduitableData conduitableData) {
                final ConduitMessage message = (ConduitMessage) conduitableData;
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO this needs to be built so that data can be decrypted and uncompressed
                        String newText = String.format("<b>Friend> </b>%s<br>", message.getMessage());
                        String oldText = Html.toHtml(textView.getEditableText()).toString();
                        textView.setText(Html.fromHtml(newText + oldText));
//                    byte[] decyeptedAndDecompressed = transformer.decompressAndDecrypt(data);
//                    String res = new String(decyeptedAndDecompressed);
//                    textView.append(res);
                    }
                });
                return null;
            }
        });

        conduitGroup.addConduitableDataListener(ConduitableDataTypes.CONNECTION_EVENT, new Function1<ConduitableData, Unit>() {
            @Override
            public Unit invoke(final ConduitableData conduitableData) {
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        final ConduitConnectionEvent evt = (ConduitConnectionEvent) conduitableData;
                        String oldText = Html.toHtml(textView.getEditableText()).toString();
                        String newText = "\nNew user connected!: " + evt.getConnectedClientName() + "\n";
                        textView.setText(Html.fromHtml(newText + oldText));
                    }
                });

                return null;
            }
        });


        ArrayList<PuppetShow> shows = new ArrayList<>();
        PuppetShow connectionsPuppetShow = new BootstrappingConnectionEventsIncoming(conduitGroup);
        shows.add(connectionsPuppetShow);
        PuppetShow convoPuppetShow = new PassiveAggressiveConversation(conduitGroup);
        shows.add(convoPuppetShow);

        // Create multiple PuppetMaster if you need shows to run at the same time
        // you can also do puppetMaster.startShow(show); to start a single show
        PuppetMaster puppetMaster = new PuppetMaster();
        // this will chain the two shows one after the other
        puppetMaster.chainShows(shows);

    }

    private void setUpTextBoxes() {
        userText = (EditText)findViewById(R.id.plain_text_input);
        textView = (TextView)findViewById(R.id.plain_textView);

//        userText.setOnClickListener(clickTextBoxListener);
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

    private void setUpSendButton() {
        sendButton = (Button) findViewById(R.id.send_button);

        sendButton.setOnClickListener(clickTextBoxListener);
    }

    private View.OnClickListener clickTextBoxListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String userInput = userText.getText().toString();
            if (!userInput.equals("")) {
//                byte[] compressedAndEncryptedText = transformer.compressAndEncrypt(userInput);
                ConduitMessage message = new ConduitMessage();
                message.setMessage(userInput);
                conduitGroup.send(3, message);
                String newText = String.format("<b>You> </b>%s<br>", userInput);
                String oldText = Html.toHtml(textView.getEditableText()).toString();
                textView.setText(Html.fromHtml(newText + oldText));
                userText.getText().clear();
            }
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.text:
                    userText.requestFocus();

//                    dataLink.debugEcho((byte)69);
//                    dataLink.debugEcho((byte)71);
//                    dataLink.debugEcho((byte)71);
//                    dataLink.debugEcho((byte)111);
//                    dataLink.debugEcho((byte)33);
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
        } else if (id == R.id.action_QRsetup) {
            // launch new intent for QR
            Intent intent = new Intent(this,QRGenerationActivity.class);
            startActivity(intent);
        } else if(id == R.id.action_setup) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
