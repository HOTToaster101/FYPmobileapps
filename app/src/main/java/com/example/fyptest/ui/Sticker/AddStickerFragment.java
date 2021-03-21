package com.example.fyptest.ui.Sticker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fyptest.R;
import com.google.android.material.textfield.TextInputLayout;

public class AddStickerFragment extends Fragment {

    Button add;
    TextInputLayout input;

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

        return root;
    }

    public void addSticker(){

    }

}