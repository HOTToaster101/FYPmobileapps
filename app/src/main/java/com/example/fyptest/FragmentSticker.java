package com.example.fyptest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FragmentSticker extends Fragment {

    RecyclerView list;
    ArrayList<Sticker> stickerlist;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_sticker, container, false);

        stickerlist = new ArrayList<>();
        //configure the recycle list to show all the stickers
        list = v.findViewById(R.id.rv_sticker);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        StickerAdapter adapter = new StickerAdapter(stickerlist);
        list.setAdapter(adapter);

        return v;
    }

}
