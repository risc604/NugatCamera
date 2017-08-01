package com.example.tomcat.nugatcamera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

//http://www.voidcn.com/blog/Hacker_ZhiDian/article/p-6580116.html


public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int TAKE_PHOTO_PERMISSION_REQUEST_CODE = 0;
    private static final int WRITE_SDCARD_PERMISSION_REQUEST_CODE = 1;

    private static final int TAKE_PHOTO_REQUEST_CODE = 3;
    private static final int CHOICE_ALBUM_REQUEST_CODE = 4;
    private static final int CORP_PHOTO_REQUEST_CODE = 5;
    private static final String PROVIDER_PATH = "com.example.tomcat.nugatcamera.provider";

    //private enum InnerRequestCode
    //{
    //    TAKE_PHOTO_PERMISSION_REQUEST_CODE,
    //    WRITE_SDCARD_PERMISSION_REQUEST_CODE,
    //
    //    TAKE_PHOTO_REQUEST_CODE,
    //    CHOICE_ALBUM_REQUEST_CODE,
    //    CORP_PHOTO_REQUEST_CODE
    //
    //}

    private Uri photoUri = null;
    private Uri outputUri = null;

    ImageView   mImageView;
    Button      mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate(), savedInstanceState: " + savedInstanceState +
        ", PROVIDER_PATH: " + PROVIDER_PATH);

        initView();
        initControl();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        Log.i(TAG, "onRequestPermissionsResult(),  requestCode: " + requestCode +
                ", permissions: " + Arrays.toString(permissions) +
                ", grantResults: " + Arrays.toString(grantResults));

        switch (requestCode)
        {
            case TAKE_PHOTO_PERMISSION_REQUEST_CODE:
                if ((grantResults.length > 0) &&
                    (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {
                    startCamera();
                }
                else
                {
                    Toast.makeText(this, "Camera permission decline.", Toast.LENGTH_SHORT).show();
                }
                break;

            case WRITE_SDCARD_PERMISSION_REQUEST_CODE:
                if ((grantResults.length > 0) &&
                    (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {
                }
                else
                {
                    Toast.makeText(this, "SD card write permission decline.", Toast.LENGTH_SHORT).show();
                }

            default:
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.i(TAG, "onActivityResult(),  requestCode: " + requestCode +
                    ", resultCode: " + resultCode);
        if (data!= null)
            Log.i(TAG, "data: " + Arrays.toString(data.getStringArrayExtra(data.getDataString())));
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case TAKE_PHOTO_REQUEST_CODE:
                //if (requestCode == RESULT_OK)
                //{
                //    Bundle extras = data.getExtras();
                //    Bitmap imageBitmp = (Bitmap) extras.get("data");
                //    mImageView.setImageBitmap(imageBitmp);
                //}
                cropPhoto(photoUri);
                break;

            case CHOICE_ALBUM_REQUEST_CODE:
                cropPhoto(data.getData());
                break;

            case CORP_PHOTO_REQUEST_CODE:
                File file = new File(photoUri.getPath());
                if (file.exists())
                {
                    Bitmap bitmap = BitmapFactory.decodeFile(photoUri.getPath());
                    mImageView.setImageBitmap(bitmap);
                }
                else
                {
                    Toast.makeText(this, "NOT found image file.", Toast.LENGTH_SHORT).show();
                }

            default:
                break;
        }
    }


    public void ImgBtnOnClick(View view)
    {
        Log.i(TAG, "ImgBtnOnClick(),  view: " + view.toString());
        if (view == mButton)
        {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},
                        TAKE_PHOTO_PERMISSION_REQUEST_CODE);
            }
            else
            {
                startCamera();
            }
        }
        else
        {
            choiceAlbum();
        }

    }

    private void initView()
    {
        Log.i(TAG, "initView() ... ");
        mImageView = (ImageView) findViewById(R.id.imageView);
        mButton = (Button) findViewById(R.id.btnCapture);
    }

    private void initControl()
    {
        Log.i(TAG, "initControl() ... ");
        //----- permission check -----
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_SDCARD_PERMISSION_REQUEST_CODE);
        }
    }

    private void startCamera()
    {
        Log.i(TAG, "startCamera() ... ");
        File file = new File(getExternalCacheDir(), "image.png");
        try
        {
            if (file.exists())
            {
                file.delete();
            }
            file.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "file: " + file.getPath());
        if (Build.VERSION.SDK_INT >= 24)
        {
            photoUri = FileProvider.getUriForFile(this, PROVIDER_PATH, file);
        }
        else
        {
            photoUri = Uri.fromFile(file);
        }

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST_CODE);
    }

    private void choiceAlbum()
    {
        Log.i(TAG, "choiceAlbum() ...");
        Intent choiceAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        choiceAlbumIntent.setType("image/*");
        startActivityForResult(choiceAlbumIntent, CHOICE_ALBUM_REQUEST_CODE);
    }

    private void cropPhoto(Uri inputUri)
    {
        Log.i(TAG, "cropPhoto(), inputUri: " + inputUri.toString());
        Intent cropPhotoIntent = new Intent("com.android.camera.action.CROP");
        cropPhotoIntent.setDataAndType(inputUri, "image/*");
        cropPhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri = Uri.parse("file:////sdcard/image_output.png"));
        startActivityForResult(cropPhotoIntent, CORP_PHOTO_REQUEST_CODE);

    }



}
