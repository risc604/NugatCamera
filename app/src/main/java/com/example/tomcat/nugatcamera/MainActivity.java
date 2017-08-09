package com.example.tomcat.nugatcamera;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
                { }
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
                Log.i(TAG, "photoUri: " + photoUri);
                cropPhoto(photoUri);
                break;

            case CHOICE_ALBUM_REQUEST_CODE:
                cropPhoto(data.getData());
                break;

            case CORP_PHOTO_REQUEST_CODE:
                 //= getOriententionBitmap(photoUri.getPath());
                if (file!=null && file.exists())
                    file.delete();
                File gFile = new File(photoUri.getPath());
                if (gFile.exists())
                {
                    Bitmap bitmap = BitmapFactory.decodeFile(photoUri.getPath());
                    //Bitmap bitmap = getOriententionBitmap(photoUri.getPath());
                    try
                    {
                        bitmap = rotateImageIfRequired(bitmap, this, photoUri);
                        //int degree = getOrientention(photoUri.getPath());
                        //if (degree > 0)
                        //{
                        //    bitmap = rotateImage(bitmap, degree);
                        //}
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    bitmap = resize(bitmap, 1920, 1080);
                    mImageView.setImageBitmap(bitmap);

                    try {
                        String bmpFilePath = Environment.getExternalStorageDirectory() +
                                "/mt24hr/" + System.currentTimeMillis() + ".jpeg";

                        FileOutputStream fos = new FileOutputStream(new File(bmpFilePath));
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                        Log.i(TAG, "bmpFilePath: " + bmpFilePath +
                                ", size: " + ((float)fos.getChannel().size())/1024 + " Kbytes");
                        fos.close();
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "Error accessing file: " + e.getMessage());
                    }
                    //Log.i(TAG, "file: " + file.getAbsolutePath() );
                    gFile.delete();
                    //file.delete();
                }
                else
                {
                    Toast.makeText(this, "NOT found image file.", Toast.LENGTH_SHORT).show();
                }

            default:
                break;
        }
    }


    public void ImgBtnOnClick(final View view)
    {
        Log.i(TAG, "ImgBtnOnClick(),  view: " + view.toString());

        CharSequence[] items = {"相 簿", "相 機"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("選 取 照 片");
        builder.setItems(items, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int which)
            {
                switch (which)
                {
                    case 0:     // Album
                        choiceAlbum();
                        break;

                    case 1:     // Camera
                        //startCamera();
                        showTakePicture();
                        break;

                    default:
                        Toast.makeText(view.getContext(), "Error !!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        builder.show();
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

    private void showTakePicture()
    {
        Log.i(TAG, "showTakePicture()...");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    TAKE_PHOTO_PERMISSION_REQUEST_CODE);
        }
        else
        {
            startCamera();
        }
    }


    File file = null;
    private void startCamera()
    {
        Log.i(TAG, "startCamera() ... ");
        //File file = new File(getExternalCacheDir(), "image.jpg");
        file = new File(getExternalCacheDir(), "image.jpg");
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
        cropPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri = Uri.parse("file:////sdcard/image_output.jpg"));
        startActivityForResult(cropPhotoIntent, CORP_PHOTO_REQUEST_CODE);

    }

    //public static Bitmap rotateImage(Bitmap img, int degree)
    private Bitmap rotateImage(Bitmap img, int degree)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        return rotatedImg;
    }

    //public static int getOrientention(String filePath)
    private int getOrientention(String filePath)
    {
        File f = new File(filePath);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(f.getPath());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Log.i(TAG, "orientation: " + orientation + ", exif: " + exif.toString());

            int angle = 0;

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;

                default:
                    Log.e(TAG, "Error !! orientation: " + orientation);
                    break;
            }

            return angle;
        }
        else
        {
            Log.e(TAG, "Error !! exif is null, no Orientention info, degree set 0 !!");
            return 0;
        }
    }

    //public static Bitmap rotateImageIfRequired(Bitmap img, Context context, Uri selectedImage) throws IOException
    private Bitmap rotateImageIfRequired(Bitmap img, Context context, Uri selectedImage) throws IOException
    {
        Log.i(TAG, "rotateImageIfRequired(), selectedImage: " + selectedImage);
        if (selectedImage.getScheme().equals("content") )
        {
            String[] projection = { MediaStore.Images.ImageColumns.ORIENTATION };
            Cursor c = context.getContentResolver().query(selectedImage, projection, null, null, null);
            if (c.moveToFirst())
            {
                final int rotation = c.getInt(0);
                Log.w(TAG, "rotation: " + rotation);

                c.close();
                return rotateImage(img, rotation);
            }
            return img;
        }
        else if (selectedImage.getScheme().equals("file"))
        {
            int degree = getOrientention(selectedImage.getPath());
            if (degree > 0)
                img = rotateImage(img, degree);

            Log.i(TAG, "file path: " + selectedImage.getPath() + ", degree: " + degree);

            //String[] projection = { MediaStore.Images.Media.DATA };
            //Cursor c = context.getContentResolver().query(selectedImage, projection, null, null, null);

            //int column_index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            //c.moveToFirst();
            //String tmpFileName = c.getString(column_index);
            //Log.i(TAG, "column_index: " + column_index + ", tmpFileName: " + tmpFileName);

            //if (c.moveToFirst())
            //{
            //    final int rotation = c.getInt(0);
            //    Log.w(TAG, "rotation: " + rotation);
            //
            //    c.close();
            //    return rotateImage(img, rotation);
            //}
            return img;
        }
        else
        {
            ExifInterface ei = new ExifInterface(selectedImage.getPath());
            int orientation = ei.getAttributeInt(   ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            //Timber.d("orientation: %s", orientation);
            Log.d(TAG, "orientation: " + orientation);

            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(img, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(img, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(img, 270);
                default:
                    return img;
            }
        }
    }

    private Bitmap resize(Bitmap image, int maxWidth, int maxHeight)
    {
        if (maxHeight > 0 && maxWidth > 0)
        {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1)
            {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            }
            else
            {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        }
        else
        {
            return image;
        }
    }



    ////public static Bitmap getOriententionBitmap(String filePath)
    //private Bitmap getOriententionBitmap(String filePath)
    //{
    //    Bitmap myBitmap = null;
    //    //Bitmap myBitmap = decodeFile(new File(filePath));
    //    try
    //    {
    //        File f = new File(filePath);
    //
    //        ExifInterface exif = new ExifInterface(f.getPath());
    //        int orientation = exif.getAttributeInt( ExifInterface.TAG_ORIENTATION,
    //                ExifInterface.ORIENTATION_NORMAL);
    //
    //        Log.d(TAG, "orientation: " + orientation);
    //        int angle = 0;
    //        if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
    //        {
    //            angle = 90;
    //        }
    //        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
    //        {
    //            angle = 180;
    //        }
    //        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
    //        {
    //            angle = 270;
    //        }
    //
    //        Matrix mat = new Matrix();
    //        mat.postRotate(angle);
    //
    //        Bitmap bmp1 = BitmapFactory.decodeStream(new FileInputStream(f), null, null);
    //        myBitmap = Bitmap.createBitmap( bmp1, 0, 0, bmp1.getWidth(),
    //                bmp1.getHeight(), mat, true);
    //    }
    //    catch (IOException e)
    //    {
    //        Log.w("TAG", "-- Error in setting image");
    //    }
    //    catch(OutOfMemoryError oom)
    //    {
    //        Log.w("TAG", "-- OOM Error in setting image");
    //    }
    //    return myBitmap;
    //}



}
