package com.example.fyptest.ui.Sticker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fyptest.R;
import com.example.fyptest.Sticker;
import com.google.android.material.textfield.TextInputLayout;

public class FragmentChangeInfo extends Fragment {

    Button change;
    Button submit;
    ImageView imv;
    TextInputLayout textIn;
    TextView name;

    Sticker sticker;

    public FragmentChangeInfo(Sticker sticker){
        this.sticker = sticker;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_sticker_info, null);

        textIn = view.findViewById(R.id.text_input_info);
        name = view.findViewById(R.id.text_sticker_name);
        submit = view.findViewById(R.id.b_submit_change);

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

        name.setText(sticker.getName());

        return view;
    }

}
