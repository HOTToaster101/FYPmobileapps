package com.example.fyptest;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;

public class SettingFragment extends Fragment {

    boolean pref;
    Button bconfirm;
    RadioGroup gr;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_setting, container, false);
        pref = false;
        gr = v.findViewById(R.id.radiogroup);
        bconfirm = (Button) v.findViewById(R.id.b_confirm);
        bconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pref = (gr.getCheckedRadioButtonId() == R.id.rb_sticker) ? false : true;
                System.out.println(gr.getCheckedRadioButtonId());
                writePreference();
            }
        });
        return v;
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.rb_sticker:
                if (checked)
                    pref = false;
                    System.out.println("emoji is selected");
                    break;
            case R.id.rb_emoji:
                if (checked)
                    pref = true;
                    System.out.println("emoji is selected");
                    break;
        }
    }

    public void writePreference(){
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)) {
            String path = getActivity().getExternalFilesDir(null) + File.separator + "pref.txt";
            File file = new File(path);
            FileOutputStream fos = null;
            String s = pref? "emoji" : "sticker";
            System.out.println(s + pref);
            try{
                if (!file.exists()) {
                    Log.d("path", file.toString());
                    file.createNewFile();
                    System.out.println("Creating new file " + path);
                }
                fos = new FileOutputStream(file);
                fos.write(s.getBytes());
                fos.flush();
                fos.close();
                Toast.makeText(getActivity(), "Save successfully! path = " + path, Toast.LENGTH_LONG).show();
                System.out.println("Save successfully! path = " + path);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
