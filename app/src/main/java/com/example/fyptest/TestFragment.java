package com.example.fyptest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TestFragment extends Fragment {

    EditText y;
    EditText x;
    EditText inx;
    EditText iny;
    Button b;

    int w = 216, h = 216, offx = 114, offy = 36;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_croptest, null);

        b = v.findViewById(R.id.b_test);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!x.getText().toString().equals("") && y.getText() != null && inx.getText() != null  && iny.getText() != null ){
                    w = Integer.parseInt(x.getText().toString());
                    h = Integer.parseInt(y.getText().toString());
                    offx = Integer.parseInt(inx.getText().toString());
                    offy = Integer.parseInt(iny.getText().toString());
                }
                System.out.println((w + h) + ", " + (offx + offy));
            }
        });
        x = v.findViewById(R.id.textbox_input_testx);
        x.setText(null);
        y = v.findViewById(R.id.textbox_input_testy);
        y.setText(null);
        inx = v.findViewById(R.id.textbox_input_offx);
        inx.setText(null);
        iny = v.findViewById(R.id.textbox_input_offy);
        iny.setText(null);

        return v;
    }

    public int getW(){return w;}
    public int getH(){return h;}
    public int getoffX(){return offx;}
    public int getoffY(){return offy;}

}
