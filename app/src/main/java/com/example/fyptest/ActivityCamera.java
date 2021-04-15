package com.example.fyptest;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.FileProvider;

import org.opencv.dnn.SegmentationModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityCamera extends AppCompatActivity {

    View v;
    //ImageView iv;
    Button bCapture, bVideo;
    Toolbar bar;
    MenuItem iSet;
    int height, width;
    VideoSegmentation segModel;

    static final int REQUEST_IMAGE_CAPTURE = 3;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int FILE_SELECT_CODE = 0;
    private static final int FILE_CROP_CODE = 2;
    FragmentManager manager;
    private Uri fileUri;
    private String filePath;
    private TestFragment f;
    private Uri vfileUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        manager = getSupportFragmentManager();
       /* DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;*/
        bCapture = (Button) findViewById(R.id.button_capture);
        bCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPictureTakerAction();
                //showFileChooser();
            }
        });
        bCapture.setClickable(true);
        bVideo = (Button) findViewById(R.id.button_vcapture);
        bVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchVideoTakerAction();
            }
        });
        bCapture.setClickable(true);
        bar = (Toolbar) findViewById(R.id.topAppBar);
        setSupportActionBar(bar);
    }

    public void dispatchPictureTakerAction(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }else{
            Toast.makeText(this, "Camera cannot be opened", Toast.LENGTH_LONG).show();
        }
        try{
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }catch(Exception e){
            System.out.println(e.getStackTrace());
        }
    }

    public void dispatchVideoTakerAction(){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File f = null;
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
        //    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);

        try{
            f = setUpVideoFile();
            //if (f != null){
            //    Log.d("CameraSample", "not null");
           // }
            vfileUrl = Uri.fromFile(f);
            //takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(ActivityCamera.this, BuildConfig.APPLICATION_ID + ".provider", f));
            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 2);   //limit the video time
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }catch(IOException e){
            System.out.println(e.getStackTrace());
        }
        }else{
            Toast.makeText(this, "Camera cannot be opened", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String createImageFromBitmap(Bitmap bitmap) {
        String fileName = "myImage";//no .png or .jpg needed
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            // remember close file output
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, width, width, false);
            hideButton();
            Grabcut f = new Grabcut(imageBitmap);
            manager.beginTransaction()
                    .setCustomAnimations(
                            R.anim.fragment_fade_enter,
                            R.anim.fragment_fade_exit
                    )
                    .replace(R.id.fl_camera, f, null)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();

            //openFragment(selectedBitmap);
        }else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
                if(vfileUrl!=null){
                    try {segModel = new VideoSegmentation(ActivityCamera.this);}
                    catch (Exception e) {e.printStackTrace();}
                    Uri selectedclip = vfileUrl;
                    segModel.segmentImage(selectedclip);
                    //Intent videosegintent = new Intent(this, VideoSegmentation.class);
                    //videosegintent.putExtra("pictureinput", createImageFromBitmap(imageBitmap));
                    //startActivity(videosegintent);
                }
            //Bundle bundle = new Bundle();
            //bundle.putString("url", bitMapsFilePath.get(index));
           // myIntent.putExtras(bundle);
                    //data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            //ENTERVIDEOCAPTURE
            //hideButton();
           // VideoSegmentation v = new VideoSegmentation(imageBitmap);
           /* manager.beginTransaction()
                    .setCustomAnimations(
                            R.anim.fragment_fade_enter,
                            R.anim.fragment_fade_exit
                    )
                    .replace(R.id.fl_camera, f, null)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();*/

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private File setUpVideoFile() throws IOException {

        File videoFile = null;
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = String.format("Video_%s", timeStamp);

        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {

            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }
            videoFile = File.createTempFile(imageFileName + "_",
                    ".mp4", storageDir);
            Log.d("CameraSample", "successfully create directory");

        } else {
            Log.v(getString(R.string.app_name),
                    "External storage is not mounted READ/WRITE.");
        }

        return videoFile;
    }

    private void openFragment(){
        Bitmap b = null;
        Bitmap result = null;
        try{
            InputStream in =  getContentResolver().openInputStream(fileUri);
            //byte[] buf = new byte[1024];
            System.out.println("sending bitmap to grabcut.java");
            b = BitmapFactory.decodeStream(in);
            //b = Bitmap.createScaledBitmap(b, width, height - 600, false);

            System.out.println(b.getWidth() + ", " + b.getHeight());
            result = cropImage(f.getW(), f.getH(), f.getoffX(), f.getoffY(), b);
        }catch(Exception e){
            e.printStackTrace();
        }
        hideButton();
        Grabcut f1 = new Grabcut(result);
        manager.beginTransaction()
                .setCustomAnimations(
                        R.anim.fragment_fade_enter,
                        R.anim.fragment_fade_exit
                )
                .replace(R.id.fl_camera, f1, null)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }

    private Bitmap cropImage(int width, int height, int offsetx, int offsety, Bitmap b){
        /**int width = b.getWidth() - 300;
        int height = b.getHeight() - 300;
        int offsetx = 300;
        int offsety = 300;**/
        int pixel[] = new int[height * width];
        for(int y = 0; y < height - 1; y++){
            for(int x = 0; x < width - 1; x++){
                pixel[y * width + x] = b.getPixel(x + offsetx, y + offsety);
            }
        }
        System.out.println(pixel.length + ", " + b.getByteCount());
        //b.getPixels(pixel, 0, b.getWidth(), 50, 50, b.getWidth() - 100, b.getHeight() - 100);

        Bitmap result = Bitmap.createBitmap(pixel, width, height, Bitmap.Config.ARGB_8888);
        return result;
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void performCrop(Uri picUri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            System.out.println("starting the cropping process");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties here
            cropIntent.putExtra("crop", true);
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 1);
            cropIntent.putExtra("outputY", 1);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);

            File f = new File(getExternalFilesDir(null) + "/testcrop.jpg");
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, FILE_CROP_CODE);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public String getRealPathFromURI_API19(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(uri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            System.out.println(cursor.getString(column_index));
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.camera_tool_bar, menu);
        iSet = menu.findItem(R.id.item_settings);
        iSet.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.item_settings:
                SettingFragment f = new SettingFragment();
                manager.beginTransaction().replace(R.id.fl_camera, f, null).commit();
                return true;
            case R.id.item_home:
                System.out.println("starting home intent");
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void hideButton(){
        bCapture.animate().scaleY(0);
        bCapture.setClickable(false);
        bVideo.animate().scaleY(0);
        bVideo.setClickable(false);
        iSet.setEnabled(false);
    }



}
