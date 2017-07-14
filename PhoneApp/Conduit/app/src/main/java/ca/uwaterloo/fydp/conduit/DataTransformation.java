package ca.uwaterloo.fydp.conduit;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;

import id.zelory.compressor.Compressor;

/**
 * Created by davanb on 2017-07-14.
 */

/*
    Class DataTransformation
    Wraps Shoco small string compression library
    Wraps Encryption stuff
    Takes in user data (text, images, GPS data) and preforms the following operations:
        Compresses and encrypts data,
        converts data in a way that is consumable by Conduit device
 */
public class DataTransformation {

    private Context context;

    public DataTransformation(Context context){
        this.context = context;
    }

    /*
        Methods to for small string compression/decompression (Shoco)
        Implementations defined in native-lib.cpp
     */
    private native String compressSmallString(String uncompressedString);
    private native String decompressCompressedString(String compressedString);

    /*
        Compressor image compression library methods
        Decompression is not needed, it simply reduces the size of the image
     */
    private Bitmap compressImage(File imageFile, int maxWidth, int maxHeight, int quality) {
        Bitmap compressor = null;
        try {
            compressor = new Compressor(context)
                    .setMaxWidth(maxWidth)
                    .setMaxHeight(maxHeight)
                    .setQuality(quality)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).getAbsolutePath())
                    .compressToBitmap(imageFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
        return compressor;
    }

    /*
        Public interface which will blackbox this class functionality
        Will use overloaded functions and accept different arguments
        Will output data in a format consumable by Conduit device
        (this output format needs to be decided, the interface will need to be developed with Conduit group)
     */
    public int compressData(String uncompressedData) {
        return 0;
    }

    public int decompressData(String compressedString) {
        return 0;
    }

    public int comppressData(File imageToCompress) {
        return 0;
    }


}
