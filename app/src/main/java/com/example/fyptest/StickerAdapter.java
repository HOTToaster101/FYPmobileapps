package com.example.fyptest;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StickerAdapter extends RecyclerView.Adapter<StickerHolder>{

    ArrayList<Sticker> stickerlist;

    public StickerAdapter(ArrayList<Sticker> stickerlist) {
        this.stickerlist = stickerlist;
    }

    @NonNull
    @Override
    public StickerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflate = LayoutInflater.from(parent.getContext());
        return new StickerHolder(inflate, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull StickerHolder holder, int position) {
        Sticker s = stickerlist.get(position);
        holder.bind(s, this, position);
    }

    @Override
    public int getItemCount() {
        return stickerlist.size();
    }
}
