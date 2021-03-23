package com.example.fyptest;

import android.graphics.Bitmap;

public class Sticker {

    private int id;
    private Bitmap image;
    private String name;
    private String date;

    public Sticker(int id, String date, Bitmap image){
        this.id = id;
        this.image = image;
        this.date = date;
        this.name = "" + id;
    }

    public void setName(String name){
        this.name = name;
    }

    public Bitmap getImage(){
        return image;
    }

    public int getId() {
        return id;
    }

    public String getName(){
        return name;
    }

    public String getDate(){return date;}

}
