package com.example.fyptest.ui.Sticker;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.fyptest.FragmentSticker;
import com.example.fyptest.R;
import com.example.fyptest.Sticker;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FragmentChangeInfo extends Fragment {

    Button change;
    Button submit;
    ImageView imv;
    TextInputLayout textIn;
    TextView name;
    FragmentManager manager;

    Sticker sticker;

    public FragmentChangeInfo(Sticker sticker){
        this.sticker = sticker;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_sticker_info, null);
        manager = getActivity().getSupportFragmentManager();

        textIn = view.findViewById(R.id.text_input_info);
        name = view.findViewById(R.id.text_sticker_name);
        submit = view.findViewById(R.id.b_submit_change);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(textIn.getEditText().getText().toString().equals("") || textIn.getEditText().getText().toString().equals(null))){
                    addSticker();
                    System.out.println("the entered text is " + textIn.getEditText().getText().toString());
                    FragmentSticker f = new FragmentSticker();
                    manager.beginTransaction().setCustomAnimations(R.animator.enter_from_left, R.animator.exit_from_left).replace(R.id.fragment_homepage, f, null).commit();
                }else
                    System.out.println("Nothing is inputted");
            }
        });
        submit.setClickable(false);

        change = view.findViewById(R.id.b_change_name);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name.setVisibility(View.INVISIBLE);
                textIn.setVisibility(View.VISIBLE);
                submit.setClickable(true);
            }
        });

        imv = view.findViewById(R.id.imv_sticker_info);
        imv.setImageBitmap(sticker.getImage());

        name.setText(sticker.getName() + System.lineSeparator() + sticker.getDate());

        return view;
    }

    public void addSticker(){
        String state = Environment.getExternalStorageState();
        String info = textIn.getEditText().getText().toString();
        String time = sticker.getDate();
        byte[] ls = System.getProperty("line.separator").getBytes();
        if(state.equals(Environment.MEDIA_MOUNTED)) {
            String path = getActivity().getExternalFilesDir(null) + File.separator + Integer.toString(sticker.getId()) + "grabcut.txt";
            File file = new File(path);
            file.delete();
                try {
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(Integer.toString(sticker.getId()).getBytes());
                    fos.write(ls);
                    fos.write(info.getBytes());
                    fos.write(ls);
                    fos.write(time.getBytes());
                    fos.close();
                    //Toast.makeText(this, "Save successfully! path = " + Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "grabcut.webp", Toast.LENGTH_LONG).show();
                    System.out.println("Save successfully! path = " + Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + sticker.getId() + "grabcut.png");
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }

        }
        System.out.println("the current count is " + sticker.getId() + ", " + info + time);
    }

}
