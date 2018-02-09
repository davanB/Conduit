package ca.uwaterloo.fydp.conduit;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import ca.uwaterloo.fydp.conduit.GroupData;


/*
    This was also copy pasted from some random tutorial
    it needs to be mofdified but maybe we need to give credit to the guy?
*/

public class QRGenerationActivity extends Activity implements OnClickListener{

    GroupData groupData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrgeneration_avtivity);

        Intent intent = getIntent();
        String groupName = intent.getStringExtra("GroupName");
        String userName = intent.getStringExtra("UserName");

        groupData = new GroupData(groupName, userName);

        // TODO: Remove this since the last screen should auto generate QR
        Button button1 = (Button) findViewById(R.id.generateButton);
        button1.setOnClickListener(this);

    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.generateButton:
                String qrInputText = groupData.generateHandShakeData().toString();

                //Find screen size
                WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int width = point.x;
                int height = point.y;
                int smallerDimension = width < height ? width : height;
                smallerDimension = smallerDimension * 3/4;

                //Encode with a QR Code image
                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrInputText,
                        null,
                        Contents.Type.TEXT,
                        BarcodeFormat.QR_CODE.toString(),
                        smallerDimension);
                try {
                    Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                    ImageView myImage = (ImageView) findViewById(R.id.QRView);
                    myImage.setImageBitmap(bitmap);

                } catch (WriterException e) {
                    e.printStackTrace();
                }


                break;

            // More buttons go here (if any) ...

        }
    }

}
