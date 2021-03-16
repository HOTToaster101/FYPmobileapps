package com.example.fyptest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

public class ActivityVideoCamera extends AppCompatActivity {

    static final int REQUEST_VIDEO_CAPTURE = 1;
    View v;
    ImageView iv;
    Button bCapture;
    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //v = inflater.inflate(R.layout.activity_camera, container, false);
        setContentView(R.layout.activity_camera);
        iv = (ImageView) findViewById(R.id.im_camera);
        bCapture = (Button) findViewById(R.id.button_capture);
        bCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchVideoTakerAction();
                //s.setClickable(true);
            }
        });
        bCapture.setClickable(true);
    }

    public void dispatchVideoTakerAction(){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }else{
            Toast.makeText(this, "Camera cannot be opened", Toast.LENGTH_LONG).show();
        }
        try{
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
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
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            //createImageFromBitmap(imageBitmap);
            //Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            //ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            //byte[] byteArray = stream.toByteArray();


            Intent grabcutintent = new Intent(this, Grabcut.class);
            grabcutintent.putExtra("pictureinput", createImageFromBitmap(imageBitmap));
            startActivity(grabcutintent);

            // iv.setImageBitmap(imageBitmap);
            //imageBitmap.recycle();
            /**if (!database.finishPicture(byteArray)) {
             Toast.makeText(this, "Photo cannot be registered", Toast.LENGTH_LONG).show();
             } else {
             Toast.makeText(this, "You can start ride now", Toast.LENGTH_LONG).show();
             }**/
            //byte[] bimage = database.getPic(0);
            //Bitmap bitmap = BitmapFactory.decodeByteArray(bimage, 0, bimage.length);
            //iv.setImageBitmap(bitmap);
        }
    }



}
