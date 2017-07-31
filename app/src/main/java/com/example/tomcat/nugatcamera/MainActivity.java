package com.example.tomcat.nugatcamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();
            static final int REQUEST_IMAGE_CAPTURE = 1;

    ImageView   mImageView;
    ImageButton mImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    private void initView()
    {
        mImageView = ()
    }

    private void initControl()
    {

    }

    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


}
