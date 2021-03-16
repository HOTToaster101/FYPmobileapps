package com.example.fyptest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

public class StickerHolder extends RecyclerView.ViewHolder {

    ImageView imv;

    public StickerHolder(LayoutInflater inflater, ViewGroup vg){
        super(inflater.inflate(R.layout.list_sticker, vg, false));
        imv = itemView.findViewById(R.id.im_sticker);
    }

    public void bind(final Sticker sticker, final StickerAdapter adapter, final int i) {
        imv.setImageBitmap(sticker.getImage());
        /**imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

            }
        });**/
    }

}
