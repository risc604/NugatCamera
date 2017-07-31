package com.example.tomcat.nugatcamera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

//http://www.voidcn.com/blog/Hacker_ZhiDian/article/p-6580116.html


public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int TAKE_PHOTO_PERMISSION_REQUEST_CODE = 0;
    private static final int WRITE_SDCARD_PERMISSION_REQUEST_CODE = 1;

    private static final int TAKE_PHOTO_REQUEST_CODE = 3;
    private static final int CHOICE_ALBUM_REQUEST_CODE = 4;
    private static final int CORP_PHOTO_REQUEST_CODE = 5;

    private Uri photoUri = null;
    private Uri outputUri = null;

    ImageView   mImageView;
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initControl();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_IMAGE_CAPTURE:
                if (requestCode == RESULT_OK)
                {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmp = (Bitmap) extras.get("data");
                    mImageView.setImageBitmap(imageBitmp);
                }
                break;

            default:
                break;
        }
    }


    public void ImgBtnOnClick(View view)
    {
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
        mImageView = (ImageView) findViewById(R.id.imageView);
        mButton = (Button) findViewById(R.id.btnCapture);
    }

    private void initControl()
    {
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

        if (Build.VERSION.SDK_INT >= 24)
        {
            photoUri = FileProvider.getUriForFile(this, "com.example.tomcat.nugatcamera", file);
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
        Intent choiceAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        choiceAlbumIntent.setType("image/*");
        startActivityForResult(choiceAlbumIntent, CHOICE_ALBUM_REQUEST_CODE);
    }

    private void cropPhoto(Uri inputUri)
    {
        Intent cropPhotoIntent = new Intent("com.android.camera.action.CROP");
        cropPhotoIntent.setDataAndType(inputUri, "image/*");
        cropPhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri = Uri.parse("file:////sdcard/image_output.png"));
        startActivityForResult(cropPhotoIntent, CORP_PHOTO_REQUEST_CODE);

    }



}
