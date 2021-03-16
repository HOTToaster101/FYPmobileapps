package com.example.fyptest;

import android.graphics.Bitmap;

public class Sticker {

    private int id;
    private Bitmap image;

    public Sticker(int id, Bitmap image){
        this.id = id;
        this.image = image;
    }

    public Bitmap getImage(){
        return image;
    }

    public int getId() {
        return id;
    }

}
