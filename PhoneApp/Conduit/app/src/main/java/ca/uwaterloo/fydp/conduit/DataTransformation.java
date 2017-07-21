package ca.uwaterloo.fydp.conduit;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import org.encryptor4j.Encryptor;
import org.encryptor4j.factory.KeyFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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

    // context of activity instantiating this class
    private Context context;

    // key used for encrypting messages
    private Key secretKey;

    public DataTransformation(Context context){
        this.context = context;
        char[] password = "123VConduit!".toCharArray();
//        this.secretKey = KeyFactory.DES.keyFromPassword(password);

        try {
            byte[] key = ("123VConduit").getBytes("UTF8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // use only first 128 bit

            this.secretKey = new SecretKeySpec(key, "AES");
        } catch (UnsupportedEncodingException e){
            System.out.println("ohshit");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("wheres the algo");
        }
    }

    /*
        Methods to for small string compression/decompression (Shoco)
        Implementations defined in native-lib.cpp
     */
    private native byte[] compressSmallString(String uncompressedString);
    private native byte[] decompressCompressedString(byte[] compressedString);

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
    public byte[] compressData(String uncompressedData) {
        byte[] compressedString = compressSmallString(uncompressedData);
        return compressedString;
    }

    // TODO the paramater might have to change depending on how data is retrieved from Conduit
    public byte[] decompressData(byte[] compressedString) {
        byte[] uncompressedString = decompressCompressedString(compressedString);
        return uncompressedString;
    }

    // TODO tune these parameters or make them configurable?
    public Bitmap compressData(File imageToCompress) {
        Bitmap compressedBitmap = compressImage(imageToCompress,640,640,75);
        return compressedBitmap;
    }

    /*
        Cryptographic functions
        PKCS5Padding is actually PKCS7Padding (prob a type)
        PKCS5Padding is for 8byte block ciphers (3DES) not 16byte ciphers like AES
     */
    //TODO possibly convert byte[] to string?
    public byte[] encryptMessage(byte[] message) {
        try {
            Encryptor encryptor = new Encryptor(secretKey, "AES/CBC/PKCS5Padding", 16);
            byte[] encrypted = encryptor.encrypt(message);
            return encrypted;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            System.out.println(e);
        }
        return null;
    }

    public byte[] decryptMessage(byte[] encryptedData) {
        try {
            Encryptor encryptor = new Encryptor(secretKey, "AES/CBC/PKCS5Padding", 16);
            byte[] decrypted = encryptor.decrypt(encryptedData);
            return decrypted;
        } catch (GeneralSecurityException e) {
            System.out.println(e);
        }
        return null;
    }

    public byte[] compressAndEncrypt(String uncompressed) {
        byte[] compressed = compressData(uncompressed);
        byte[] encrypted = encryptMessage(compressed);
        return encrypted;
    }

    public byte[] decompressAndDecrypt(byte[] encryped) {
        byte[] decrypted = decryptMessage(encryped);
        byte[] uncompressed = decompressData(decrypted);
        return uncompressed;
    }

    // TODO consider encrypting images too?
}
