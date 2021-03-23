package com.example.fyptest.ui.Sticker;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fyptest.R;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddStickerFragment extends Fragment {

    Button add;
    TextInputLayout input;
    ImageView im;
    Bitmap b;
    int id;

    public AddStickerFragment(Bitmap b, int id){
        this.b = b;
        this.id = id;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_stciker, container, false);
        input = (TextInputLayout) root.findViewById(R.id.text_input);
        add = (Button) root.findViewById(R.id.b_add_name);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSticker();
            }
        });
        im = (ImageView) root.findViewById(R.id.im_sticker2);
        im.setImageBitmap(b);

        return root;
    }

    public void addSticker(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
        formatter.format(now);
        String state = Environment.getExternalStorageState();
        String info = input.getEditText().getText().toString();
        String time = formatter.format(now);
        byte[] ls = System.getProperty("line.separator").getBytes();
        if(state.equals(Environment.MEDIA_MOUNTED)) {
            String path = getActivity().getExternalFilesDir(null) + File.separator + Integer.toString(id) + "grabcut.txt";
            File file = new File(path);
            //file.delete();
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(Integer.toString(id).getBytes());
                    fos.write(ls);
                    fos.write(info.getBytes());
                    fos.write(ls);
                    fos.write(time.getBytes());
                    fos.close();
                    //Toast.makeText(this, "Save successfully! path = " + Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "grabcut.webp", Toast.LENGTH_LONG).show();
                    //System.out.println("Save successfully! path = " + Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "grabcut.png");
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("the current count is " + id + ", " + info + time);
    }

}