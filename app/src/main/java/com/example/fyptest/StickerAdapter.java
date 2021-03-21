package com.example.fyptest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.fyptest.ui.Sticker.FragmentChangeInfo;

import java.util.ArrayList;

public class StickerAdapter extends BaseAdapter {

    ArrayList<Sticker> stickerList;
    Context context;
    LayoutInflater inflater;

    public StickerAdapter(Context context, ArrayList<Sticker> stickerList) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.stickerList = stickerList;
    }


    @Override
    public int getCount() {
        return stickerList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.list_sticker, null);
        ImageView imv = view.findViewById(R.id.im_sticker);
        imv.setImageBitmap(stickerList.get(position).getImage());
        FragmentChangeInfo fragment = new FragmentChangeInfo(stickerList.get(position));
        FragmentManager manager = ((AppCompatActivity)view.getContext()).getSupportFragmentManager();
        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Navigation.findNavController(v).navigate(R.id.nav_host_fragment);
                manager.beginTransaction()
                        .setCustomAnimations(
                                R.anim.fragment_fade_enter,
                                R.anim.fragment_fade_exit
                        )
                        .replace(R.id.fragment_homepage, fragment, null)
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit();
            }
        });
        return view;
    }
}
