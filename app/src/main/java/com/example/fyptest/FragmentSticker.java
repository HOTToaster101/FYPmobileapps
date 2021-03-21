package com.example.fyptest;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FragmentSticker extends Fragment {

    GridView list;
    ArrayList<Sticker> stickerlist;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_sticker, container, false);

        stickerlist = new ArrayList<>();
        //configure the recycle list to show all the stickers
        list = v.findViewById(R.id.gv_sticker);
        for(int i = 0; i < 10; i++){
            stickerlist.add(new Sticker(i, BitmapFactory.decodeResource(getResources(), R.drawable.tab_background)));
        }
        StickerAdapter adapter = new StickerAdapter(this.getContext(), stickerlist);
        list.setAdapter(adapter);

        return v;
    }

}
