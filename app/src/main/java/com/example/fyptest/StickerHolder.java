package com.example.fyptest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StickerHolder extends RecyclerView.ViewHolder {

    ImageView imv1;
    ImageView imv2;
    ImageView imv3;
    ArrayList<ImageView> imvs;

    public StickerHolder(LayoutInflater inflater, ViewGroup vg){
        super(inflater.inflate(R.layout.list_sticker, vg, false));
        imv1 = itemView.findViewById(R.id.im_sticker);
        imv2 = itemView.findViewById(R.id.im_sticker2);
        //imv3 = itemView.findViewById(R.id.im_sticker3);
        imvs = new ArrayList<>();
        imvs.add(imv1);imvs.add(imv2);imvs.add(imv3);
    }

    public void bind(final ArrayList<Sticker> sticker, final StickerAdapter adapter, final int i) {
        for(int count = 0; count < sticker.size(); count++){
            imvs.get(count).setImageBitmap(sticker.get(count).getImage());
        }
        //imv1.setImageBitmap(sticker.get(0).getImage());
        //imv2.setImageBitmap(sticker.get(1).getImage());
        //imv3.setImageBitmap(sticker.get(2).getImage());
        /**imv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v){

        }
        });**/
    }

}
