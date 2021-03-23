package com.example.fyptest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

public class ActivityCamera extends AppCompatActivity {

    View v;
    //ImageView iv;
    Button bCapture;
    Toolbar bar;
    MenuItem iSet;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        manager = getSupportFragmentManager();
        bCapture = (Button) findViewById(R.id.button_capture);
        bCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPictureTakerAction();
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
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height - 600, false);
            /**Intent grabcutintent = new Intent(this, Grabcut.class);
            grabcutintent.putExtra("pictureinput", createImageFromBitmap(imageBitmap));
            startActivity(grabcutintent);**/
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
        iSet.setEnabled(false);
    }



}
